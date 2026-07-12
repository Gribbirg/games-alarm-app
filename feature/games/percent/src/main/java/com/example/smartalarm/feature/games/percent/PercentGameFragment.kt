package com.example.smartalarm.feature.games.percent

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
import com.example.smartalarm.feature.games.percent.R
import com.example.smartalarm.feature.games.percent.databinding.FragmentPercentGameBinding

/**
 * Экран мини-игры «Проценты» (game id = 26).
 *
 * Игроку показывается текстовая задача на проценты с целым ответом, и он
 * выбирает ответ из четырёх вариантов. Для победы нужно N верных ответов
 * (3/4/5 по сложности); сложность также задаёт типы задач (простые
 * проценты / любые проценты и изменение числа / цены и обратные задачи).
 * Ошибка стоит 10 очков и приводит к новой задаче.
 *
 * Соблюдает общий контракт экрана игры (см. `.claude/rules/games.md`):
 * аргументы «alarm id», «start time», «test», «difficulty», «music path»;
 * снятие уведомления в onResume; перезапуск будильника в onPause;
 * повторное включение мелодии каждые две минуты; возврат к выбору игр
 * по системной «назад» в пробном режиме; передача «time», «score»,
 * «game id» на экран результата при победе.
 */
class PercentGameFragment : Fragment() {

    private lateinit var binding: FragmentPercentGameBinding
    private lateinit var viewModel: PercentGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test")) {
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_percentGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentPercentGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[PercentGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.percentTimeTextView.text = it
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

        viewModel.task.observe(viewLifecycleOwner) { task ->
            binding.taskTextView.text = task.text
            optionButtons.forEachIndexed { index, button ->
                button.text = task.options[index].toString()
            }
        }

        viewModel.correctCount.observe(viewLifecycleOwner) {
            binding.percentProgressTextView.text =
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
        bundle.putInt("game id", 26)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_percentGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_percentGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }
}
