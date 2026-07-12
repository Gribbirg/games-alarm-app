package com.example.smartalarm.feature.games.equation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.example.smartalarm.core.alarm.AlarmMediaPlayer
import com.example.smartalarm.feature.games.equation.R
import com.example.smartalarm.feature.games.equation.databinding.FragmentEquationGameBinding

/**
 * Экран игры «Уравнение» (game id = 3).
 *
 * Показывает равенство с пропущенным элементом (число или оператор)
 * и четыре варианта ответа; для победы нужно решить несколько уравнений
 * (3/4/5 в зависимости от сложности). Реализует общий контракт экрана игры
 * (см. `.claude/rules/games.md`): снимает уведомление будильника,
 * перезапускает будильник при выходе, периодически включает мелодию
 * и передаёт результат на экран итога.
 */
class EquationGameFragment : Fragment() {

    private lateinit var binding: FragmentEquationGameBinding
    private lateinit var viewModel: EquationGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test")) {
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_equationGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentEquationGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[EquationGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.equationTimeTextView.text = it
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

        viewModel.currentTask.observe(viewLifecycleOwner) { task ->
            binding.equationTextView.text = task.text
            answerButtons().forEachIndexed { index, button ->
                button.text = task.options[index]
            }
        }

        viewModel.progressText.observe(viewLifecycleOwner) {
            binding.progressTextView.text = it
        }

        answerButtons().forEach { button ->
            button.setOnClickListener { onAnswerClicked(button.text.toString()) }
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

    private fun answerButtons(): List<Button> = listOf(
        binding.answerButton1,
        binding.answerButton2,
        binding.answerButton3,
        binding.answerButton4
    )

    private fun onAnswerClicked(option: String) {
        when (viewModel.submitAnswer(option)) {
            AnswerResult.WIN -> finishGame()

            AnswerResult.CORRECT ->
                Toast.makeText(context, "Верно!", Toast.LENGTH_SHORT).show()

            AnswerResult.WRONG ->
                Toast.makeText(context, "Неправильно!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun finishGame() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", 3)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_equationGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_equationGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }
}
