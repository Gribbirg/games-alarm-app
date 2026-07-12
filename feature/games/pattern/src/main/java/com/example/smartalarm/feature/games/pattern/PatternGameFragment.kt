package com.example.smartalarm.feature.games.pattern

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
import com.example.smartalarm.feature.games.pattern.R
import com.example.smartalarm.feature.games.pattern.databinding.FragmentPatternGameBinding

/**
 * Экран игры «Закономерность» (game id = 24).
 *
 * Показывает ряд эмодзи, построенный по повторяющемуся паттерну, и четыре
 * варианта его продолжения. Верный выбор открывает следующий раунд, после
 * последнего раунда происходит переход к экрану результата. Ошибочный выбор
 * штрафуется и перегенерирует раунд (см. [PatternGame]), так что перебор
 * вариантов не помогает.
 */
class PatternGameFragment : Fragment() {

    private lateinit var binding: FragmentPatternGameBinding
    private lateinit var viewModel: PatternGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test"))
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_patternGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentPatternGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[PatternGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.patternTimeTextView.text = it
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
            binding.option4Button,
        )
        optionButtons.forEachIndexed { index, button ->
            button.setOnClickListener { onOptionClicked(index) }
        }

        showRound()

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

    /** Отрисовывает текущий раунд: ряд эмодзи, счётчик раундов и варианты. */
    private fun showRound() {
        val game = viewModel.game
        val round = game.currentRound

        binding.roundTextView.text = "Раунд ${game.roundNumber} из ${game.totalRounds}"
        binding.sequenceTextView.text =
            round.visibleSequence.joinToString(" ") + " → ?"

        binding.option1Button.text = round.options[0]
        binding.option2Button.text = round.options[1]
        binding.option3Button.text = round.options[2]
        binding.option4Button.text = round.options[3]
    }

    private fun onOptionClicked(index: Int) {
        when (viewModel.onOptionClicked(index)) {
            AnswerResult.WRONG -> {
                Toast.makeText(context, "Неправильно!", Toast.LENGTH_SHORT).show()
                showRound()
            }

            AnswerResult.NEXT_ROUND -> showRound()

            AnswerResult.WIN -> win()
        }
    }

    /** Кладёт результат в аргументы и переходит к экрану результата. */
    private fun win() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", 24)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_patternGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_patternGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }
}
