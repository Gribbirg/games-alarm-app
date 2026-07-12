package com.example.smartalarm.feature.games.truefalse

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
import com.example.smartalarm.feature.games.truefalse.R
import com.example.smartalarm.feature.games.truefalse.databinding.FragmentTruefalseGameBinding

/**
 * Экран мини-игры «Верно или нет» (game id = 11).
 *
 * Игроку показывается арифметическое утверждение и две кнопки — «Верно»
 * и «Неверно». Для победы нужна серия из N правильных ответов подряд
 * (5/7/10 по сложности); ошибка сбрасывает серию и стоит 10 очков.
 *
 * Соблюдает общий контракт экрана игры (см. `.claude/rules/games.md`):
 * аргументы «alarm id», «start time», «test», «difficulty», «music path»;
 * снятие уведомления в onResume; перезапуск будильника в onPause;
 * повторное включение мелодии каждые две минуты; возврат к выбору игр
 * по системной «назад» в пробном режиме; передача «time», «score»,
 * «game id» на экран результата при победе.
 */
class TrueFalseGameFragment : Fragment() {

    private lateinit var binding: FragmentTruefalseGameBinding
    private lateinit var viewModel: TrueFalseGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test")) {
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_truefalseGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentTruefalseGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[TrueFalseGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.truefalseTimeTextView.text = it
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

        viewModel.statement.observe(viewLifecycleOwner) {
            binding.statementTextView.text = it.text
        }

        viewModel.streak.observe(viewLifecycleOwner) {
            binding.streakTextView.text = "$it/${viewModel.targetStreak}"
        }

        binding.trueButton.setOnClickListener { checkAnswer(true) }
        binding.falseButton.setOnClickListener { checkAnswer(false) }

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

    private fun checkAnswer(userSaysTrue: Boolean) {
        when (viewModel.answer(userSaysTrue)) {
            AnswerResult.WIN -> finishGame()
            AnswerResult.WRONG ->
                Toast.makeText(context, "Ошибка! Серия сброшена", Toast.LENGTH_SHORT).show()

            AnswerResult.CORRECT -> Unit
        }
    }

    private fun finishGame() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", 11)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_truefalseGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_truefalseGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }
}
