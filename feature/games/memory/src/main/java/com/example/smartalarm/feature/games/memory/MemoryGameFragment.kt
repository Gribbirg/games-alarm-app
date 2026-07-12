package com.example.smartalarm.feature.games.memory

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
import com.example.smartalarm.feature.games.memory.R
import com.example.smartalarm.feature.games.memory.databinding.FragmentMemoryGameBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

/**
 * Экран мини-игры «Повтори узор» (id игры — 2).
 *
 * Приложение подсвечивает последовательность ячеек поля 3×3, игрок повторяет
 * её нажатиями; с каждым раундом последовательность растёт на одну ячейку
 * (параметры сложности — в [MemoryGameSettings]). Игровая логика — в
 * [MemoryGameLogic] (живёт в [MemoryGameViewModel]), фрагмент отвечает только
 * за показ/подсветку и контракт экрана игры: снятие уведомления, перезапуск
 * будильника из `onPause`, повторное включение мелодии каждые две минуты и
 * навигацию к результату.
 */
class MemoryGameFragment : Fragment() {

    private lateinit var binding: FragmentMemoryGameBinding
    private lateinit var viewModel: MemoryGameViewModel

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var cells: List<MaterialCardView>

    private var isInputAllowed = false

    private var cellBaseColor = 0
    private var cellHighlightColor = 0
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
                                R.id.action_memoryGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentMemoryGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[MemoryGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.memoryTimeTextView.text = it
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
        cellHighlightColor = MaterialColors.getColor(binding.root, R.attr.colorPrimary)
        cellErrorColor = MaterialColors.getColor(binding.root, R.attr.colorError)

        cells = listOf(
            binding.cell0, binding.cell1, binding.cell2,
            binding.cell3, binding.cell4, binding.cell5,
            binding.cell6, binding.cell7, binding.cell8
        )
        cells.forEachIndexed { index, cell ->
            cell.setOnClickListener { onCellClicked(index) }
        }

        updateRoundText()
        showSequence()

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
     * Проигрывает последовательность текущего раунда: ячейки по очереди
     * загораются цветом подсветки; на время показа ввод игрока заблокирован.
     */
    private fun showSequence() {
        isInputAllowed = false
        binding.statusTextView.text = "Смотрите..."

        val settings = viewModel.logic.settings
        var delay = SHOW_START_DELAY_MS
        for (cellIndex in viewModel.logic.sequence) {
            handler.postDelayed({ setCellColor(cellIndex, cellHighlightColor) }, delay)
            handler.postDelayed({ setCellColor(cellIndex, cellBaseColor) }, delay + settings.showCellMs)
            delay += settings.showCellMs + settings.betweenCellsMs
        }
        handler.postDelayed({
            isInputAllowed = true
            binding.statusTextView.text = "Повторите!"
        }, delay)
    }

    /** Обрабатывает нажатие игрока на ячейку [index]. */
    private fun onCellClicked(index: Int) {
        if (!isInputAllowed)
            return

        when (viewModel.logic.onCellTapped(index)) {
            TapResult.CORRECT -> flashCell(index, cellHighlightColor)

            TapResult.MISTAKE -> {
                flashCell(index, cellErrorColor)
                Toast.makeText(context, "Неправильно!", Toast.LENGTH_SHORT).show()
                isInputAllowed = false
                handler.postDelayed({ showSequence() }, RESULT_PAUSE_MS)
            }

            TapResult.ROUND_COMPLETE -> {
                flashCell(index, cellHighlightColor)
                isInputAllowed = false
                binding.statusTextView.text = "Верно!"
                handler.postDelayed({
                    viewModel.logic.startNextRound()
                    updateRoundText()
                    showSequence()
                }, RESULT_PAUSE_MS)
            }

            TapResult.WIN -> {
                flashCell(index, cellHighlightColor)
                isInputAllowed = false
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
        bundle.putInt("game id", 2)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_memoryGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_memoryGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }

    /** Коротко подсвечивает ячейку [index] цветом [color] как отклик на нажатие. */
    private fun flashCell(index: Int, color: Int) {
        setCellColor(index, color)
        handler.postDelayed({ setCellColor(index, cellBaseColor) }, TAP_FLASH_MS)
    }

    private fun setCellColor(index: Int, color: Int) {
        cells[index].setCardBackgroundColor(color)
    }

    private fun updateRoundText() {
        binding.roundTextView.text =
            "Раунд ${viewModel.logic.round} из ${viewModel.logic.settings.roundsCount}"
    }

    companion object {
        /** Пауза перед началом показа последовательности, мс. */
        private const val SHOW_START_DELAY_MS = 800L

        /** Пауза после ошибки или пройденного раунда перед новым показом, мс. */
        private const val RESULT_PAUSE_MS = 900L

        /** Длительность подсветки ячейки при нажатии игрока, мс. */
        private const val TAP_FLASH_MS = 200L
    }
}
