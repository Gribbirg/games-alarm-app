package com.example.smartalarm.feature.games.lights

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
import com.example.smartalarm.feature.games.lights.R
import com.example.smartalarm.feature.games.lights.databinding.FragmentLightsGameBinding

/**
 * Экран игры «Погаси свет» (Lights Out, game id 14).
 *
 * Квадратная сетка «лампочек»; нажатие на клетку переключает её и четырёх
 * ортогональных соседей. Цель — погасить все лампочки. Поле генерируется
 * K случайными нажатиями из погашенного состояния, поэтому решение
 * гарантировано. Каждое нажатие сверх «пара» (3×K) штрафуется −10 очков.
 *
 * Реализует общий контракт экрана игры (см. `.claude/rules/games.md`):
 * аргументы `alarm id`/`start time`/`test`/`difficulty`/`music path`,
 * снятие уведомления в `onResume`, перезапуск будильника в `onPause`,
 * повторное включение мелодии каждые две минуты бездействия и переход
 * к результату с `time`/`score`/`game id` при победе.
 */
class LightsGameFragment : Fragment() {

    private lateinit var binding: FragmentLightsGameBinding
    private lateinit var viewModel: LightsGameViewModel

    private val cellButtons = mutableListOf<Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test"))
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_lightsGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentLightsGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[LightsGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.lightsTimeTextView.text = it
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

        buildGrid()
        refreshAllCells()
        updateStats()

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

    /** Создаёт кнопки-лампочки в сетке по состоянию партии из ViewModel. */
    private fun buildGrid() {
        val game = viewModel.game

        binding.lightsGridLayout.removeAllViews()
        binding.lightsGridLayout.columnCount = game.size
        binding.lightsGridLayout.rowCount = game.size

        cellButtons.clear()
        val margin = (CELL_MARGIN_DP * resources.displayMetrics.density).toInt()
        val textSize = lampTextSizeFor(game.size)

        for (index in 0 until game.cellCount) {
            val button = Button(requireContext())
            button.textSize = textSize
            button.layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, 1f)
            ).apply {
                width = 0
                height = 0
                setMargins(margin, margin, margin, margin)
            }
            button.setOnClickListener { onCellClick(index) }
            cellButtons.add(button)
            binding.lightsGridLayout.addView(button)
        }
    }

    /** Обрабатывает нажатие на клетку с индексом [index]. */
    private fun onCellClick(index: Int) {
        val game = viewModel.game
        if (game.isWon || !game.press(index))
            return

        if (game.pressCount > viewModel.pressPar)
            viewModel.mistake()

        // Нажатие меняет максимум 5 клеток, но поле маленькое (не больше 5×5) —
        // проще и надёжнее обновить все.
        refreshAllCells()
        updateStats()

        if (game.isWon)
            finishGame()
    }

    /** Приводит вид всех лампочек в соответствие состоянию игры. */
    private fun refreshAllCells() {
        val game = viewModel.game
        for (index in cellButtons.indices) {
            val button = cellButtons[index]
            if (game.isOn(index)) {
                button.text = LAMP_ON_TEXT
                button.alpha = 1f
            } else {
                button.text = LAMP_OFF_TEXT
                button.alpha = LAMP_OFF_ALPHA
            }
        }
    }

    /** Обновляет счётчики горящих лампочек и нажатий. */
    private fun updateStats() {
        val game = viewModel.game
        binding.lightsStatsTextView.text =
            "Горит: ${game.litCount}/${game.cellCount}   " +
                    "Нажатий: ${game.pressCount} (без штрафа ≤ ${viewModel.pressPar})"
    }

    /** Записывает результат в аргументы и переходит к экрану результата. */
    private fun finishGame() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", GAME_ID)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_lightsGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_lightsGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }

    private companion object {
        /** Id игры «Погаси свет» в `ALL_GAMES`. */
        const val GAME_ID = 14

        /** Текст горящей лампочки. */
        const val LAMP_ON_TEXT = "💡"

        /** Текст погашенной клетки. */
        const val LAMP_OFF_TEXT = "⚫"

        /** Прозрачность погашенной клетки. */
        const val LAMP_OFF_ALPHA = 0.35f

        /** Отступ вокруг клетки, dp. */
        const val CELL_MARGIN_DP = 4

        /**
         * Размер текста (эмодзи) лампочки, sp: крупнее на маленьком поле,
         * мельче на 5×5, чтобы сетка помещалась на экране.
         */
        fun lampTextSizeFor(size: Int): Float = when (size) {
            3 -> 30f
            4 -> 26f
            else -> 22f
        }
    }
}
