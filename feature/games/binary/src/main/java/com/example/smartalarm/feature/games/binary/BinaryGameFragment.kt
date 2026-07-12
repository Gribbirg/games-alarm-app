package com.example.smartalarm.feature.games.binary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.example.smartalarm.core.alarm.AlarmMediaPlayer
import com.example.smartalarm.feature.games.binary.R
import com.example.smartalarm.feature.games.binary.databinding.FragmentBinaryGameBinding

/**
 * Экран мини-игры «Двоичный код» (game id = 27).
 *
 * Игроку показывается число в двоичной или десятичной записи (направление
 * каждого вопроса случайно: «101₂ = ?» или «13 = ?₂»), и он выбирает его
 * перевод из четырёх вариантов. Для победы нужно N верных ответов (3/4/5
 * по сложности); сложность также задаёт диапазон чисел (1..15 / 1..63 /
 * 1..255). Ошибка стоит 10 очков и приводит к новому вопросу.
 *
 * Соблюдает общий контракт экрана игры (см. `.claude/rules/games.md`):
 * аргументы «alarm id», «start time», «test», «difficulty», «music path»;
 * снятие уведомления в onResume; перезапуск будильника в onPause;
 * повторное включение мелодии каждые две минуты; возврат к выбору игр
 * по системной «назад» в пробном режиме; передача «time», «score»,
 * «game id» на экран результата при победе.
 */
class BinaryGameFragment : Fragment() {

    private lateinit var binding: FragmentBinaryGameBinding
    private lateinit var viewModel: BinaryGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test")) {
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_binaryGameFragment2_to_gameChoiceFragment,
                                requireArguments(),
                                NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                            )
                    }
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBinaryGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[BinaryGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.binaryTimeTextView.text = it
            with(it.split(".")) {
                if (this[0].toInt() % 2 == 0 &&
                    this[1].toInt() == 0 &&
                    this[0].toInt() != 0 &&
                    !requireArguments().getBoolean("test")
                ) {
                    AlarmMediaPlayer.playAudio(
                        context,
                        isRisingVolume = false,
                        vibrationRequired = false,
                        ringtonePath = requireArguments().getString("music path")!!
                    )
                    binding.musicOffButton.visibility = View.VISIBLE
                }
            }
        }

        binding.musicOffButton.setOnClickListener {
            AlarmMediaPlayer.stopAudio()
            binding.musicOffButton.visibility = View.GONE
        }

        viewModel.setDifficultyLevel(requireArguments().getInt("difficulty"))
        viewModel.startGame()

        val optionButtons = listOf(
            binding.option1Button,
            binding.option2Button,
            binding.option3Button,
            binding.option4Button
        )

        viewModel.question.observe(viewLifecycleOwner) { question ->
            if (question.direction == QuestionDirection.BINARY_TO_DECIMAL) {
                binding.directionTextView.text = "Переведите в десятичную систему"
                binding.questionTextView.text = "${question.prompt}₂ = ?"
            } else {
                binding.directionTextView.text = "Переведите в двоичную систему"
                binding.questionTextView.text = "${question.prompt} = ?₂"
            }
            optionButtons.forEachIndexed { index, button ->
                button.text = question.options[index]
            }
        }

        viewModel.correctCount.observe(viewLifecycleOwner) {
            binding.binaryProgressTextView.text =
                "Верных ответов: $it/${viewModel.targetCorrect}"
        }

        optionButtons.forEachIndexed { index, button ->
            button.setOnClickListener { checkAnswer(index) }
        }

        return binding.root
    }

    override fun onResume() {
        if (!requireArguments().getBoolean("test")) {
            val notificationManager = NotificationManagerCompat.from(requireContext())
            notificationManager.cancel(requireArguments().getLong("alarm id").toInt())
        }
        super.onResume()
    }

    override fun onPause() {
        if (!requireArguments().getBoolean("test"))
            viewModel.startNewAlarm()
        super.onPause()
    }

    private fun checkAnswer(optionIndex: Int) {
        when (viewModel.answer(optionIndex)) {
            AnswerResult.WIN -> finishGame()
            AnswerResult.WRONG ->
                Toast.makeText(context, "Неправильно! −10 очков", Toast.LENGTH_SHORT).show()

            AnswerResult.CORRECT -> Unit
        }
    }

    private fun finishGame() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", 27)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_binaryGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_binaryGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }
}
