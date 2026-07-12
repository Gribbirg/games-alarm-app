package com.example.smartalarm.feature.games.digits

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.smartalarm.feature.games.digits.R
import com.example.smartalarm.feature.games.digits.databinding.FragmentDigitsGameBinding

/**
 * Экран игры «Запомни число» (game id = 19): число показывается на несколько
 * секунд, затем скрывается, и игрок вводит его по памяти. Три раунда, длина
 * числа растёт с каждым раундом; ошибка — −10 очков и новое число той же длины.
 *
 * Состояние партии живёт в [DigitsGameViewModel] и переживает поворот экрана;
 * фрагмент отвечает только за тайминг показа (Handler.postDelayed) и контракт
 * экрана игры (уведомление, перезапуск будильника, музыка, навигация).
 */
class DigitsGameFragment : Fragment() {

    private lateinit var binding: FragmentDigitsGameBinding
    private lateinit var viewModel: DigitsGameViewModel

    private val showHandler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable { viewModel.finishShowPhase() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test"))
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_digitsGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentDigitsGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[DigitsGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.digitsTimeTextView.text = it
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

        viewModel.phase.observe(viewLifecycleOwner) { phase ->
            binding.digitsRoundTextView.text =
                "Раунд ${viewModel.roundNumber} из ${viewModel.totalRounds}"
            when (phase) {
                DigitsPhase.SHOWING -> showNumber()
                DigitsPhase.INPUT -> askForNumber()
                else -> Unit
            }
        }

        binding.digitsCheckButton.setOnClickListener {
            checkResult()
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

    override fun onDestroyView() {
        showHandler.removeCallbacks(hideRunnable)
        super.onDestroyView()
    }

    /**
     * Фаза показа: выводит число, прячет поле ввода и планирует скрытие через
     * остаток времени показа (после поворота экрана — только оставшееся время).
     */
    private fun showNumber() {
        binding.digitsNumberTextView.text = viewModel.currentNumber
        binding.digitsActionTextView.text = "Запомните число!"
        binding.digitsEditText.text.clear()
        binding.inputHolder.visibility = View.INVISIBLE
        binding.digitsCheckButton.visibility = View.INVISIBLE

        showHandler.removeCallbacks(hideRunnable)
        showHandler.postDelayed(hideRunnable, viewModel.remainingShowTimeMillis())
    }

    /** Фаза ввода: заменяет число маской «•» и показывает поле ввода с кнопкой. */
    private fun askForNumber() {
        binding.digitsNumberTextView.text = "•".repeat(viewModel.currentNumber.length)
        binding.digitsActionTextView.text = "Введите число по памяти"
        binding.inputHolder.visibility = View.VISIBLE
        binding.digitsCheckButton.visibility = View.VISIBLE
    }

    private fun checkResult() {
        when (viewModel.submitAnswer(binding.digitsEditText.text.toString())) {
            AnswerResult.WIN -> {
                val bundle = requireArguments()
                bundle.putString("time", viewModel.timeCurrentString.value)
                bundle.putInt("score", viewModel.finishScore())
                bundle.putInt("game id", 19)

                if (requireArguments().getBoolean("test"))
                    Navigation.findNavController(binding.root)
                        .navigate(
                            R.id.action_digitsGameFragment2_to_gameResultFragment2,
                            bundle,
                            NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                        )
                else {
                    Navigation.findNavController(binding.root)
                        .navigate(
                            R.id.action_digitsGameFragment_to_gameResultFragment,
                            bundle,
                            NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                        )
                    viewModel.setPositiveResult()
                }
            }

            AnswerResult.NEXT_ROUND -> Unit

            AnswerResult.WRONG ->
                Toast.makeText(context, "Неправильно! −10 очков", Toast.LENGTH_SHORT).show()
        }
    }
}
