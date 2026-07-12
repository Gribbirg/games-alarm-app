package com.example.smartalarm.feature.games.matrix

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.example.smartalarm.core.alarm.AlarmMediaPlayer
import com.example.smartalarm.feature.games.matrix.R
import com.example.smartalarm.feature.games.matrix.databinding.FragmentMatrixGameBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

/**
 * Экран мини-игры «Запомни клетки» (id игры — 22).
 *
 * На квадратном поле одновременно вспыхивает набор клеток, затем гаснет —
 * игрок отмечает по памяти те же клетки, порядок не важен (параметры
 * сложности — в [MatrixGameSettings]). Поле строится программно из
 * [MaterialCardView] в [GridLayout] под размер, заданный сложностью.
 * Игровая логика — в [MatrixGameLogic] (живёт в [MatrixGameViewModel]),
 * фрагмент отвечает только за показ/отметки и контракт экрана игры:
 * снятие уведомления, перезапуск будильника из `onPause`, повторное
 * включение мелодии каждые две минуты и навигацию к результату.
 */
class MatrixGameFragment : Fragment() {

    private lateinit var binding: FragmentMatrixGameBinding
    private lateinit var viewModel: MatrixGameViewModel

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var cells: List<MaterialCardView>

    private var isInputAllowed = false

    private var cellBaseColor = 0
    private var cellShowColor = 0
    private var cellMarkedColor = 0
    private var cellErrorColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // В графе будильника (test == false) выбора игр нет —
                    // «назад» там намеренно ничего не делает.
                    if (requireArguments().getBoolean("test"))
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_matrixGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentMatrixGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[MatrixGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.matrixTimeTextView.text = it
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

        viewModel.initGame(requireArguments().getInt("difficulty"))

        cellBaseColor = MaterialColors.getColor(binding.root, R.attr.colorPrimaryContainer)
        cellShowColor = MaterialColors.getColor(binding.root, R.attr.colorPrimary)
        cellMarkedColor = MaterialColors.getColor(binding.root, R.attr.colorTertiary)
        cellErrorColor = MaterialColors.getColor(binding.root, R.attr.colorError)

        buildGrid()

        // После поворота экрана: в фазе показа набор показывается заново
        // на полное время, в фазе воспроизведения восстанавливаются отметки.
        if (viewModel.isShowPhase)
            showTargets()
        else
            enterInputPhase()

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
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    /**
     * Строит квадратное поле под размер из настроек сложности:
     * заполняет [GridLayout] клетками-[MaterialCardView] с весами,
     * чтобы поле равномерно растянулось.
     */
    private fun buildGrid() {
        val gridSize = viewModel.logic.settings.gridSize
        val density = resources.displayMetrics.density
        val margin = (4 * density).toInt()

        binding.matrixGrid.removeAllViews()
        binding.matrixGrid.rowCount = gridSize
        binding.matrixGrid.columnCount = gridSize

        cells = List(gridSize * gridSize) { index ->
            val cell = MaterialCardView(requireContext()).apply {
                radius = 12 * density
                cardElevation = 4 * density
                setCardBackgroundColor(cellBaseColor)
                isClickable = true
                isFocusable = true
                setOnClickListener { onCellClicked(index) }
            }
            val params = GridLayout.LayoutParams(
                GridLayout.spec(index / gridSize, 1f),
                GridLayout.spec(index % gridSize, 1f)
            ).apply {
                width = 0
                height = 0
                setMargins(margin, margin, margin, margin)
            }
            binding.matrixGrid.addView(cell, params)
            cell
        }
    }

    /**
     * Фаза показа: весь набор текущего раунда одновременно вспыхивает на
     * `showTimeMs` и гаснет; на время показа ввод игрока заблокирован.
     */
    private fun showTargets() {
        isInputAllowed = false
        viewModel.markShowPhase()
        updateRoundText()
        resetCells()
        binding.statusTextView.text = "Запоминай!"

        handler.postDelayed({
            for (cell in viewModel.logic.targetCells)
                setCellColor(cell, cellShowColor)
            handler.postDelayed({
                viewModel.markInputPhase()
                enterInputPhase()
            }, viewModel.logic.settings.showTimeMs)
        }, SHOW_START_DELAY_MS)
    }

    /**
     * Фаза воспроизведения: набор погашен, игрок отмечает клетки по памяти;
     * уже отмеченные клетки остаются подсвеченными.
     */
    private fun enterInputPhase() {
        updateRoundText()
        resetCells()
        for (cell in viewModel.logic.markedCells)
            setCellColor(cell, cellMarkedColor)
        binding.statusTextView.text = "Повтори!"
        isInputAllowed = true
    }

    /** Обрабатывает отметку игрока на клетке [index]. */
    private fun onCellClicked(index: Int) {
        if (!isInputAllowed)
            return

        when (viewModel.logic.onCellTapped(index)) {
            MatrixTapResult.ALREADY_MARKED -> Unit

            MatrixTapResult.CORRECT -> setCellColor(index, cellMarkedColor)

            MatrixTapResult.MISTAKE -> {
                // Логика уже сгенерировала новый набор для этого раунда —
                // фаза показа помечается сразу, пауза только визуальная.
                isInputAllowed = false
                viewModel.markShowPhase()
                setCellColor(index, cellErrorColor)
                Toast.makeText(context, "Неправильно!", Toast.LENGTH_SHORT).show()
                handler.postDelayed({ showTargets() }, RESULT_PAUSE_MS)
            }

            MatrixTapResult.ROUND_COMPLETE -> {
                // Раунд переключается сразу, чтобы поворот экрана во время
                // паузы не потерял прогресс; пауза только визуальная.
                isInputAllowed = false
                setCellColor(index, cellMarkedColor)
                viewModel.logic.startNextRound()
                viewModel.markShowPhase()
                binding.statusTextView.text = "Верно!"
                handler.postDelayed({ showTargets() }, RESULT_PAUSE_MS)
            }

            MatrixTapResult.WIN -> {
                isInputAllowed = false
                setCellColor(index, cellMarkedColor)
                finishGame()
            }
        }
    }

    /**
     * Победа: кладёт в аргументы время, счёт и id игры и переходит к экрану
     * результата (в режимах пробы и будильника это разные action'ы).
     */
    private fun finishGame() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", 22)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_matrixGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_matrixGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }

    /** Возвращает всем клеткам базовый цвет. */
    private fun resetCells() {
        for (cell in cells)
            cell.setCardBackgroundColor(cellBaseColor)
    }

    private fun setCellColor(index: Int, color: Int) {
        cells[index].setCardBackgroundColor(color)
    }

    private fun updateRoundText() {
        binding.roundTextView.text =
            "Раунд ${viewModel.logic.round} из ${viewModel.logic.settings.roundsCount}"
    }

    companion object {
        /** Пауза перед вспышкой набора, чтобы игрок успел взглянуть на поле, мс. */
        private const val SHOW_START_DELAY_MS = 600L

        /** Пауза после ошибки или пройденного раунда перед новым показом, мс. */
        private const val RESULT_PAUSE_MS = 900L
    }
}
