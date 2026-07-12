package com.example.smartalarm.feature.games.targetsum

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.example.smartalarm.core.alarm.AlarmMediaPlayer
import com.example.smartalarm.feature.games.targetsum.R
import com.example.smartalarm.feature.games.targetsum.databinding.FragmentTargetsumGameBinding
import com.google.android.material.color.MaterialColors

/**
 * Экран мини-игры «Набери сумму» (game id 21 в `ALL_GAMES`).
 *
 * Сетка чисел-кнопок и целевая сумма. Игрок выделяет числа нажатиями
 * (повторное нажатие снимает выделение), сумма выделенных показывается
 * живьём; по кнопке «Проверить» сумма сравнивается с целью. Совпала —
 * раунд пройден и генерируется новое поле; не совпала — штраф, выделение
 * сохраняется. Принимается любое подмножество с точной суммой. Победа —
 * пройдены все раунды (2/2/3 по сложностям). Игровая логика — в
 * [TargetSumGame] (живёт в [TargetSumGameViewModel] и переживает поворот
 * экрана), фрагмент отвечает только за отрисовку сетки и контракт экрана
 * игры (см. `.claude/rules/games.md`): снятие уведомления в `onResume`,
 * перезапуск будильника из `onPause`, повторное включение мелодии каждые
 * две минуты и навигацию к результату.
 */
class TargetSumGameFragment : Fragment() {

    private lateinit var binding: FragmentTargetsumGameBinding
    private lateinit var viewModel: TargetSumGameViewModel

    private val numberButtons = mutableListOf<Button>()

    private var cellBaseColor = 0
    private var cellBaseTextColor = 0
    private var cellSelectedColor = 0
    private var cellSelectedTextColor = 0

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
                                R.id.action_targetsumGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentTargetsumGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[TargetSumGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.targetsumTimeTextView.text = it
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
        cellBaseTextColor = MaterialColors.getColor(binding.root, R.attr.colorOnPrimaryContainer)
        cellSelectedColor = MaterialColors.getColor(binding.root, R.attr.colorPrimary)
        cellSelectedTextColor = MaterialColors.getColor(binding.root, R.attr.colorOnPrimary)

        binding.checkButton.setOnClickListener { checkSelection() }
        binding.resetButton.setOnClickListener {
            viewModel.game.clearSelection()
            refreshAllButtons()
            updateTexts()
        }

        // После пересоздания view (поворот экрана) сетка строится заново
        // по состоянию из ViewModel — раунд и выделение сохраняются.
        buildGrid()
        updateTexts()

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

    /** Создаёт кнопки-числа в сетке по текущему раунду из ViewModel. */
    private fun buildGrid() {
        val game = viewModel.game

        binding.targetsumGridLayout.removeAllViews()
        binding.targetsumGridLayout.columnCount = game.settings.columns
        binding.targetsumGridLayout.rowCount = game.settings.gridSize / game.settings.columns

        numberButtons.clear()
        val margin = (CELL_MARGIN_DP * resources.displayMetrics.density).toInt()

        for (index in game.numbers.indices) {
            val button = Button(requireContext())
            button.text = game.numbers[index].toString()
            button.textSize = CELL_TEXT_SIZE_SP
            button.layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, 1f)
            ).apply {
                width = 0
                height = 0
                setMargins(margin, margin, margin, margin)
            }
            button.setOnClickListener { onNumberClicked(index) }
            numberButtons.add(button)
            binding.targetsumGridLayout.addView(button)
        }
        refreshAllButtons()
    }

    /** Обрабатывает нажатие игрока на число с индексом [index]. */
    private fun onNumberClicked(index: Int) {
        viewModel.game.toggle(index)
        refreshButton(index)
        updateTexts()
    }

    /** Проверяет выделение по кнопке «Проверить». */
    private fun checkSelection() {
        when (viewModel.game.check()) {
            CheckResult.MISTAKE ->
                Toast.makeText(context, "Неправильно!", Toast.LENGTH_SHORT).show()

            CheckResult.ROUND_COMPLETE -> {
                Toast.makeText(context, "Верно!", Toast.LENGTH_SHORT).show()
                viewModel.game.startNextRound()
                buildGrid()
                updateTexts()
            }

            CheckResult.WIN -> finishGame()
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
        bundle.putInt("game id", GAME_ID)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_targetsumGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_targetsumGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }

    /** Приводит вид кнопки [index] в соответствие выделению в логике. */
    private fun refreshButton(index: Int) {
        val button = numberButtons[index]
        if (index in viewModel.game.selectedIndices) {
            button.backgroundTintList = ColorStateList.valueOf(cellSelectedColor)
            button.setTextColor(cellSelectedTextColor)
        } else {
            button.backgroundTintList = ColorStateList.valueOf(cellBaseColor)
            button.setTextColor(cellBaseTextColor)
        }
    }

    /** Обновляет вид всех кнопок (после создания сетки или сброса выделения). */
    private fun refreshAllButtons() {
        for (index in numberButtons.indices)
            refreshButton(index)
    }

    /** Обновляет цель, живую сумму выделенного и счётчик раундов. */
    private fun updateTexts() {
        val game = viewModel.game
        binding.targetTextView.text = "Наберите: ${game.target}"
        binding.selectedSumTextView.text = "Выбрано: ${game.selectedSum}"
        binding.roundTextView.text = "Раунд ${game.roundNumber} из ${game.settings.roundsCount}"
    }

    private companion object {
        /** Id игры «Набери сумму» в `ALL_GAMES`. */
        const val GAME_ID = 21

        /** Отступ вокруг кнопки-числа, dp. */
        const val CELL_MARGIN_DP = 4

        /** Размер текста числа на кнопке, sp. */
        const val CELL_TEXT_SIZE_SP = 22f
    }
}
