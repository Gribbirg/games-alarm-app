package com.example.smartalarm.feature.games.clock

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
import com.example.smartalarm.feature.games.clock.R
import com.example.smartalarm.feature.games.clock.databinding.FragmentClockGameBinding

/**
 * Экран игры «Который час» (game id 18).
 *
 * На циферблате [ClockFaceView] показано время; игрок должен выбрать
 * соответствующую цифровую запись из четырёх кнопок. Раунды, варианты
 * и проверку ответов ведёт [ClockGame] (через [ClockGameViewModel]).
 *
 * Экран реализует общий контракт игр будильника: снимает уведомление
 * в onResume, перезапускает будильник в onPause при непройденной игре,
 * заново включает мелодию каждые две минуты бездействия и по победе
 * переходит к экрану результата с аргументами `time`, `score`, `game id`.
 */
class ClockGameFragment : Fragment() {

    private lateinit var binding: FragmentClockGameBinding
    private lateinit var viewModel: ClockGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test")) {
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_clockGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentClockGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ClockGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.clockTimeTextView.text = it
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

    private fun optionButtons() = listOf(
        binding.optionButton1,
        binding.optionButton2,
        binding.optionButton3,
        binding.optionButton4
    )

    private fun showRound() {
        val game = viewModel.game
        val round = game.currentRound
        binding.clockFaceView.setHandAngles(
            round.time.hourHandAngle,
            round.time.minuteHandAngle
        )
        binding.roundTextView.text = "Раунд ${game.roundNumber} из ${game.totalRounds}"
        optionButtons().forEachIndexed { index, button ->
            button.text = round.options[index].toString()
        }
    }

    private fun onOptionClicked(index: Int) {
        val game = viewModel.game
        if (game.isFinished) return

        if (game.answer(game.currentRound.options[index])) {
            if (game.isFinished) {
                finishGame()
            } else {
                Toast.makeText(context, "Верно!", Toast.LENGTH_SHORT).show()
                showRound()
            }
        } else {
            Toast.makeText(context, "Неправильно!", Toast.LENGTH_SHORT).show()
            viewModel.mistake()
            showRound()
        }
    }

    private fun finishGame() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", 18)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_clockGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_clockGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }
}
