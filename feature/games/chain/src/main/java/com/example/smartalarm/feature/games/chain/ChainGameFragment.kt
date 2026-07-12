package com.example.smartalarm.feature.games.chain

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
import com.example.smartalarm.feature.games.chain.R
import com.example.smartalarm.feature.games.chain.databinding.FragmentChainGameBinding

/**
 * Экран игры «Цепочка» (game id = 16).
 *
 * Показывает стартовое число, затем по кнопке «Дальше» — операции по одной
 * («+ 7», «× 2», «− 5»…); игрок считает в уме и в конце вводит итог. Ошибка
 * стоит 10 очков и начинает новую цепочку; для победы нужно решить одну
 * цепочку на сложностях 1–2 и две на сложности 3. Реализует общий контракт
 * экрана игры (см. `.claude/rules/games.md`): снимает уведомление будильника,
 * перезапускает будильник при выходе, периодически включает мелодию
 * и передаёт результат на экран итога.
 */
class ChainGameFragment : Fragment() {

    private lateinit var binding: FragmentChainGameBinding
    private lateinit var viewModel: ChainGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test")) {
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_chainGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentChainGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ChainGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.chainTimeTextView.text = it
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

        viewModel.displayText.observe(viewLifecycleOwner) {
            binding.displayTextView.text = it
        }

        viewModel.progressText.observe(viewLifecycleOwner) {
            binding.progressTextView.text = it
        }

        viewModel.inputPhase.observe(viewLifecycleOwner) { inputPhase ->
            binding.nextButton.visibility = if (inputPhase) View.GONE else View.VISIBLE
            binding.answerHolder.visibility = if (inputPhase) View.VISIBLE else View.GONE
            binding.checkButton.visibility = if (inputPhase) View.VISIBLE else View.GONE
        }

        binding.nextButton.setOnClickListener {
            viewModel.showNextStep()
        }

        binding.checkButton.setOnClickListener {
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

    private fun checkResult() {
        when (viewModel.submitAnswer(binding.answerEditText.text.toString())) {
            ChainAnswerResult.WIN -> finishGame()

            ChainAnswerResult.CHAIN_SOLVED -> {
                Toast.makeText(context, "Верно! Ещё одна цепочка", Toast.LENGTH_SHORT).show()
                binding.answerEditText.setText("")
            }

            ChainAnswerResult.WRONG -> {
                Toast.makeText(context, "Неправильно! Новая цепочка", Toast.LENGTH_SHORT).show()
                binding.answerEditText.setText("")
            }

            ChainAnswerResult.EMPTY ->
                Toast.makeText(context, "Введите итог", Toast.LENGTH_SHORT).show()
        }
    }

    private fun finishGame() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", 16)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_chainGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_chainGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }
}
