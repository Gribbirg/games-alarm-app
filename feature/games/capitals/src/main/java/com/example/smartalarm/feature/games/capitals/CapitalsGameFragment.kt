package com.example.smartalarm.feature.games.capitals

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
import com.example.smartalarm.feature.games.capitals.R
import com.example.smartalarm.feature.games.capitals.databinding.FragmentCapitalsGameBinding

/**
 * Экран игры «Столицы» (game id = 29).
 *
 * Показывает вопрос викторины — «какой город — столица страны X?» или
 * «X — столица какой страны?» — и 4 кнопки-варианта. Верный ответ приближает
 * победу, ошибка штрафуется; в обоих случаях задаётся новый вопрос
 * (пары за игру не повторяются). После нужного числа верных ответов
 * происходит переход к экрану результата.
 */
class CapitalsGameFragment : Fragment() {

    private lateinit var binding: FragmentCapitalsGameBinding
    private lateinit var viewModel: CapitalsGameViewModel
    private lateinit var optionButtons: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test"))
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_capitalsGameFragment2_to_gameChoiceFragment,
                                requireArguments(),
                                NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                            )
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCapitalsGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[CapitalsGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.capitalsTimeTextView.text = it
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

        optionButtons = listOf(
            binding.option1Button,
            binding.option2Button,
            binding.option3Button,
            binding.option4Button,
        )
        optionButtons.forEach { button ->
            button.setOnClickListener { onOptionClicked(button.text.toString()) }
        }

        showQuestion()

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

    /** Показывает текущий вопрос игры: текст, прогресс и 4 варианта ответа. */
    private fun showQuestion() {
        val game = viewModel.game
        val question = game.currentQuestion

        binding.progressTextView.text =
            "Верных ответов: ${game.correctCount} из ${game.targetCorrect}"
        binding.questionTextView.text = question.text
        optionButtons.forEachIndexed { index, button ->
            button.text = question.options[index]
        }
    }

    private fun onOptionClicked(option: String) {
        when (viewModel.onAnswer(option)) {
            AnswerResult.WRONG -> {
                Toast.makeText(context, "Неправильно!", Toast.LENGTH_SHORT).show()
                showQuestion()
            }

            AnswerResult.NEXT_QUESTION -> showQuestion()

            AnswerResult.WIN -> win()
        }
    }

    /** Кладёт результат в аргументы и переходит к экрану результата. */
    private fun win() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", 29)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_capitalsGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_capitalsGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }
}
