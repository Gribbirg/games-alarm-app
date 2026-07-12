package com.example.smartalarm.feature.games.counter

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.example.smartalarm.core.alarm.AlarmMediaPlayer
import com.example.smartalarm.feature.games.counter.R
import com.example.smartalarm.feature.games.counter.databinding.FragmentCounterGameBinding

/**
 * Экран игры «Сосчитай» (game id = 13).
 *
 * Показывает квадратную сетку, заполненную вперемешку эмодзи нескольких
 * видов, и вопрос «Сколько на поле X?» с четырьмя вариантами ответа.
 * Верный ответ открывает следующий раунд, неверный — перегенерирует поле;
 * после последнего раунда происходит переход к экрану результата.
 * Сетка строится программно в [GridLayout] по данным [CounterGame].
 */
class CounterGameFragment : Fragment() {

    private lateinit var binding: FragmentCounterGameBinding
    private lateinit var viewModel: CounterGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test"))
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_counterGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentCounterGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[CounterGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.counterTimeTextView.text = it
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

    /** Кнопки вариантов ответа в порядке следования в разметке. */
    private fun optionButtons() = listOf(
        binding.option1Button,
        binding.option2Button,
        binding.option3Button,
        binding.option4Button,
    )

    /** Перерисовывает вопрос, счётчик раундов, сетку и варианты ответа. */
    private fun showRound() {
        val game = viewModel.game
        val round = game.currentRound

        binding.counterQuestionTextView.text = "Сколько на поле ${round.targetEmoji}?"
        binding.roundTextView.text = "Раунд ${game.roundNumber} из ${game.totalRounds}"

        optionButtons().forEachIndexed { index, button ->
            button.text = round.options[index].toString()
        }

        binding.emojiGrid.removeAllViews()
        binding.emojiGrid.columnCount = round.gridSide
        binding.emojiGrid.rowCount = round.gridSide

        val cellTextSizeSp = when (round.gridSide) {
            4 -> 28f
            5 -> 22f
            else -> 18f
        }
        val cellMargin = (2 * resources.displayMetrics.density).toInt()

        round.cells.forEachIndexed { index, symbol ->
            val cell = TextView(requireContext()).apply {
                text = symbol
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, cellTextSizeSp)
            }
            val params = GridLayout.LayoutParams(
                GridLayout.spec(index / round.gridSide, 1f),
                GridLayout.spec(index % round.gridSide, 1f)
            ).apply {
                width = 0
                height = 0
                setMargins(cellMargin, cellMargin, cellMargin, cellMargin)
            }
            binding.emojiGrid.addView(cell, params)
        }
    }

    private fun onOptionClicked(index: Int) {
        val option = viewModel.game.currentRound.options[index]
        when (viewModel.answer(option)) {
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
        bundle.putInt("game id", 13)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_counterGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_counterGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }
}
