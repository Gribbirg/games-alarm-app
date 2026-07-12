package com.example.smartalarm.feature.games.mole

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.example.smartalarm.core.alarm.AlarmMediaPlayer
import com.example.smartalarm.feature.games.mole.R
import com.example.smartalarm.feature.games.mole.databinding.FragmentMoleGameBinding

/**
 * Экран игры «Поймай крота» (game id = 23).
 *
 * Крот появляется в случайной норе на ограниченное время и прячется;
 * нажатие по кроту — попадание, по пустой норе — промах (−10 очков).
 * Победа — после нужного числа попаданий (зависит от сложности).
 *
 * Логика игры — в [MoleGame] (чистый Kotlin), состояние и счёт — в
 * [MoleGameViewModel]; здесь только тайминги показа крота
 * (Handler.postDelayed) и отрисовка сетки.
 */
class MoleGameFragment : Fragment() {

    private lateinit var binding: FragmentMoleGameBinding
    private lateinit var viewModel: MoleGameViewModel

    /** Тайминги появления крота: работают только между onResume и onPause. */
    private val moleHandler = Handler(Looper.getMainLooper())
    private val showRunnable = Runnable { showMole() }
    private val hideRunnable = Runnable { hideMole() }

    /** Ячейки-норы в порядке индексов игры (слева направо, сверху вниз). */
    private val cells = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test"))
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_moleGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentMoleGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[MoleGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.moleTimeTextView.text = it
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
        viewModel.ensureGameCreated()

        buildGrid(viewModel.game.difficulty.gridSize)
        renderHoles()
        updateCounters()

        return binding.root
    }

    override fun onResume() {
        if (!requireArguments().getBoolean("test")) {
            val notificationManager = NotificationManagerCompat.from(requireContext())
            notificationManager.cancel(requireArguments().getLong("alarm id").toInt())
        }
        if (!viewModel.game.isWon)
            moleHandler.postDelayed(showRunnable, FIRST_SHOW_DELAY_MS)
        super.onResume()
    }

    override fun onPause() {
        moleHandler.removeCallbacksAndMessages(null)
        viewModel.game.hideMole()
        renderHoles()
        if (!requireArguments().getBoolean("test"))
            viewModel.startNewAlarm()
        super.onPause()
    }

    /** Строит сетку нор нужного размера и вешает обработчики нажатий. */
    private fun buildGrid(size: Int) {
        val grid = binding.moleGridLayout
        grid.removeAllViews()
        grid.rowCount = size
        grid.columnCount = size
        cells.clear()
        for (i in 0 until size * size) {
            val cell = TextView(requireContext())
            cell.gravity = Gravity.CENTER
            cell.textSize = if (size <= 3) 44f else 32f
            cell.text = HOLE_EMOJI
            cell.setOnClickListener { onHoleClick(i) }
            val params = GridLayout.LayoutParams(
                GridLayout.spec(i / size, 1f),
                GridLayout.spec(i % size, 1f)
            )
            params.width = 0
            params.height = 0
            cell.layoutParams = params
            cells.add(cell)
            grid.addView(cell)
        }
    }

    /** Показывает крота в следующей норе и планирует его исчезновение. */
    private fun showMole() {
        viewModel.game.showMole()
        renderHoles()
        moleHandler.postDelayed(hideRunnable, viewModel.game.difficulty.showTimeMs)
    }

    /** Крот ушёл сам (без штрафа); планирует следующее появление. */
    private fun hideMole() {
        viewModel.game.hideMole()
        renderHoles()
        moleHandler.postDelayed(showRunnable, MOLE_HIDDEN_MS)
    }

    /** Обрабатывает нажатие на нору с индексом [index]. */
    private fun onHoleClick(index: Int) {
        if (viewModel.game.isWon)
            return
        when (viewModel.game.tap(index)) {
            MoleTapResult.HIT -> {
                moleHandler.removeCallbacks(hideRunnable)
                renderHoles()
                updateCounters()
                if (viewModel.game.isWon) {
                    moleHandler.removeCallbacksAndMessages(null)
                    win()
                } else {
                    moleHandler.postDelayed(showRunnable, MOLE_HIDDEN_MS)
                }
            }

            MoleTapResult.MISS -> {
                viewModel.mistake()
                updateCounters()
            }
        }
    }

    /** Перерисовывает норы по текущему состоянию игры. */
    private fun renderHoles() {
        for (i in cells.indices)
            cells[i].text = if (i == viewModel.game.currentHole) MOLE_EMOJI else HOLE_EMOJI
    }

    /** Обновляет счётчики попаданий и промахов. */
    private fun updateCounters() {
        binding.hitsTextView.text =
            "Поймано: ${viewModel.game.hits}/${viewModel.game.difficulty.targetHits}"
        binding.missesTextView.text = "Промахи: ${viewModel.game.misses}"
    }

    /** Записывает результат в аргументы и переходит к экрану результата. */
    private fun win() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", 23)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_moleGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_moleGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }

    companion object {
        /** Пауза перед первым появлением крота после onResume, мс. */
        private const val FIRST_SHOW_DELAY_MS = 600L

        /** Пауза между исчезновением крота и следующим появлением, мс. */
        private const val MOLE_HIDDEN_MS = 450L

        /** Пустая нора. */
        private const val HOLE_EMOJI = "🕳️"

        /** Крот. */
        private const val MOLE_EMOJI = "🐹"
    }
}
