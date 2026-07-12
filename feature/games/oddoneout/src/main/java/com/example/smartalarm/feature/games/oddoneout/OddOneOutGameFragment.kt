package com.example.smartalarm.feature.games.oddoneout

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
import com.example.smartalarm.feature.games.oddoneout.R
import com.example.smartalarm.feature.games.oddoneout.databinding.FragmentOddoneoutGameBinding

/**
 * Экран игры «Найди лишнее» (game id = 8).
 *
 * Показывает квадратную сетку одинаковых эмодзи, среди которых ровно один
 * отличается; нажатие на него открывает следующий раунд, после последнего
 * раунда происходит переход к экрану результата. Сетка строится
 * программно в [GridLayout] по данным [OddOneOutGame].
 */
class OddOneOutGameFragment : Fragment() {

    private lateinit var binding: FragmentOddoneoutGameBinding
    private lateinit var viewModel: OddOneOutGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test"))
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_oddoneoutGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentOddoneoutGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[OddOneOutGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.oddTimeTextView.text = it
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

    /** Перестраивает сетку [GridLayout] под текущий раунд игры. */
    private fun showRound() {
        val game = viewModel.game
        val round = game.currentRound

        binding.roundTextView.text = "Раунд ${game.roundNumber} из ${game.totalRounds}"

        binding.emojiGrid.removeAllViews()
        binding.emojiGrid.columnCount = round.gridSide
        binding.emojiGrid.rowCount = round.gridSide

        val cellTextSizeSp = when (round.gridSide) {
            3 -> 40f
            4 -> 32f
            else -> 24f
        }
        val cellMargin = (4 * resources.displayMetrics.density).toInt()
        val backgroundValue = TypedValue()
        requireContext().theme.resolveAttribute(
            android.R.attr.selectableItemBackground, backgroundValue, true
        )

        round.symbols.forEachIndexed { index, symbol ->
            val cell = TextView(requireContext()).apply {
                text = symbol
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, cellTextSizeSp)
                isClickable = true
                isFocusable = true
                setBackgroundResource(backgroundValue.resourceId)
                setOnClickListener { onCellClicked(index) }
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

    private fun onCellClicked(index: Int) {
        when (viewModel.onCellClicked(index)) {
            ClickResult.WRONG -> Toast.makeText(context, "Неправильно!", Toast.LENGTH_SHORT).show()
            ClickResult.NEXT_ROUND -> showRound()
            ClickResult.WIN -> win()
        }
    }

    /** Кладёт результат в аргументы и переходит к экрану результата. */
    private fun win() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", 8)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_oddoneoutGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_oddoneoutGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }
}
