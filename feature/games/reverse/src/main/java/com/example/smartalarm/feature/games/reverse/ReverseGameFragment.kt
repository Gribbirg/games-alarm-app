package com.example.smartalarm.feature.games.reverse

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
import com.example.smartalarm.feature.games.reverse.R
import com.example.smartalarm.feature.games.reverse.databinding.FragmentReverseGameBinding
import com.example.smartalarm.feature.games.reverse.logic.AnswerResult

/**
 * Экран игры «Наоборот».
 *
 * Игроку показывается русское слово, написанное задом наперёд
 * (например, «АКОРОС»), и четыре варианта ответа; нужно выбрать исходное
 * слово («сорока»). Чтобы выключить будильник, нужно дать несколько верных
 * ответов — их число и длина слов зависят от сложности. При ошибке слово
 * меняется на новое. Логика игры — в
 * [com.example.smartalarm.feature.games.reverse.logic.ReverseGame],
 * таймер и очки — в [ReverseGameViewModel].
 */
class ReverseGameFragment : Fragment() {

    private lateinit var binding: FragmentReverseGameBinding
    private lateinit var viewModel: ReverseGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test"))
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_reverseGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentReverseGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ReverseGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.reverseTimeTextView.text = it
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

        optionButtons().forEachIndexed { index, button ->
            button.setOnClickListener { onOptionClicked(index) }
        }

        renderRound()

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

    /** Кнопки вариантов в том же порядке, что и варианты раунда. */
    private fun optionButtons() = listOf(
        binding.option1Button,
        binding.option2Button,
        binding.option3Button,
        binding.option4Button
    )

    /**
     * Перерисовывает экран по текущему состоянию игры: счётчик прогресса,
     * перевёрнутое слово и тексты кнопок-вариантов.
     */
    private fun renderRound() {
        val game = viewModel.game
        binding.roundProgressTextView.text =
            "Слово ${(game.correctCount + 1).coerceAtMost(game.totalRounds)} из ${game.totalRounds}"
        binding.reversedWordTextView.text = game.currentRound.displayed.uppercase()
        optionButtons().forEachIndexed { index, button ->
            button.text = game.currentRound.options[index]
        }
    }

    /** Обрабатывает выбор варианта: верно — дальше, ошибка — новое слово. */
    private fun onOptionClicked(optionIndex: Int) {
        when (viewModel.game.submitAnswer(optionIndex)) {
            AnswerResult.CORRECT -> {
                Toast.makeText(context, "Верно!", Toast.LENGTH_SHORT).show()
                renderRound()
            }

            AnswerResult.WRONG -> {
                Toast.makeText(context, "Неправильно!", Toast.LENGTH_SHORT).show()
                viewModel.mistake()
                renderRound()
            }

            AnswerResult.GAME_WON -> {
                val bundle = requireArguments()
                bundle.putString("time", viewModel.timeCurrentString.value)
                bundle.putInt("score", viewModel.finishScore())
                bundle.putInt("game id", 28)

                if (requireArguments().getBoolean("test"))
                    Navigation.findNavController(binding.root)
                        .navigate(
                            R.id.action_reverseGameFragment2_to_gameResultFragment2,
                            bundle,
                            NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                        )
                else {
                    Navigation.findNavController(binding.root)
                        .navigate(
                            R.id.action_reverseGameFragment_to_gameResultFragment,
                            bundle,
                            NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                        )
                    viewModel.setPositiveResult()
                }
            }
        }
    }
}
