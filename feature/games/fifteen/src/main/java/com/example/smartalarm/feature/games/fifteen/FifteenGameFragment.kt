package com.example.smartalarm.feature.games.fifteen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.example.smartalarm.core.alarm.AlarmMediaPlayer
import com.example.smartalarm.feature.games.fifteen.R
import com.example.smartalarm.feature.games.fifteen.databinding.FragmentFifteenGameBinding

/**
 * Экран игры «Пятнашки» (game id = 15): собери плитки по порядку 1..N,
 * сдвигая соседние с пустой клеткой плитки, чтобы выключить будильник.
 *
 * Поле строится программно в [GridLayout]; состояние партии живёт
 * в [FifteenGameViewModel] и переживает поворот экрана.
 */
class FifteenGameFragment : Fragment() {

    private lateinit var binding: FragmentFifteenGameBinding
    private lateinit var viewModel: FifteenGameViewModel
    private lateinit var game: FifteenGame

    private val tileButtons = mutableListOf<Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test"))
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_fifteenGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentFifteenGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[FifteenGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.fifteenTimeTextView.text = it
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

        game = viewModel.ensureGame(requireArguments().getInt("difficulty"))
        buildField()
        updateField()

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

    /**
     * Создаёт кнопки-плитки в [GridLayout] по размеру поля текущей партии.
     */
    private fun buildField() {
        val size = game.board.size
        val grid = binding.fifteenGridLayout
        grid.removeAllViews()
        grid.columnCount = size
        grid.rowCount = size
        tileButtons.clear()

        val margin = (4 * resources.displayMetrics.density).toInt()
        for (index in 0 until game.board.cellCount) {
            val button = Button(requireContext())
            button.textSize = 24f
            button.setOnClickListener { onTileClick(index) }
            val params = GridLayout.LayoutParams(
                GridLayout.spec(index / size, 1f),
                GridLayout.spec(index % size, 1f)
            )
            params.width = 0
            params.height = 0
            params.setMargins(margin, margin, margin, margin)
            grid.addView(button, params)
            tileButtons.add(button)
        }
    }

    /**
     * Отрисовывает текущее состояние поля и счётчик ходов:
     * пустая клетка — невидимая кнопка, остальные — числа плиток.
     */
    private fun updateField() {
        for (index in tileButtons.indices) {
            val value = game.board.tileAt(index)
            with(tileButtons[index]) {
                if (value == 0) {
                    visibility = View.INVISIBLE
                    text = ""
                } else {
                    visibility = View.VISIBLE
                    text = value.toString()
                }
            }
        }
        binding.fifteenMovesTextView.text = "Ходов: ${game.moveCount}"
    }

    private fun onTileClick(index: Int) {
        if (!game.moveTile(index)) return
        updateField()
        if (game.isWon)
            win()
    }

    private fun win() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", 15)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_fifteenGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_fifteenGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }
}
