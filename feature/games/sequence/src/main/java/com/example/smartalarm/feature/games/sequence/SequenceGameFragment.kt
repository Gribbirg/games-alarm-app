package com.example.smartalarm.feature.games.sequence

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
import com.example.smartalarm.feature.games.sequence.R
import com.example.smartalarm.feature.games.sequence.databinding.FragmentSequenceGameBinding

/**
 * Экран мини-игры «Продолжи ряд» (game id = 6).
 *
 * Показывает числовой ряд из 4–5 членов и четыре варианта следующего члена;
 * для победы нужно решить 3/4/5 рядов (сложность 1/2/3). Реализует общий
 * контракт экрана игры: снятие уведомления, перезапуск будильника из onPause,
 * повторное включение мелодии каждые две минуты, переход к результату.
 */
class SequenceGameFragment : Fragment() {

    private lateinit var binding: FragmentSequenceGameBinding
    private lateinit var viewModel: SequenceGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test"))
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_sequenceGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentSequenceGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[SequenceGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.sequenceTimeTextView.text = it
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

        for (button in optionButtons())
            button.setOnClickListener {
                checkAnswer((it as Button).text.toString().toInt())
            }

        showTask()

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

    private fun optionButtons(): List<Button> = listOf(
        binding.option1Button,
        binding.option2Button,
        binding.option3Button,
        binding.option4Button
    )

    private fun showTask() {
        val game = viewModel.game
        binding.sequenceProgressTextView.text =
            "Ряд ${game.currentRoundNumber} из ${game.totalRounds}"
        binding.sequenceTextView.text = game.currentTask.questionText
        optionButtons().forEachIndexed { index, button ->
            button.text = game.currentTask.options[index].toString()
        }
    }

    private fun checkAnswer(option: Int) {
        if (viewModel.checkAnswer(option)) {
            if (viewModel.game.isFinished)
                win()
            else
                showTask()
        } else {
            Toast.makeText(context, "Неправильно!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun win() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", 6)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_sequenceGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_sequenceGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }
}
