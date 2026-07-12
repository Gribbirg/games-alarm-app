package com.example.smartalarm.feature.games.pairs

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.smartalarm.feature.games.pairs.R
import com.example.smartalarm.feature.games.pairs.databinding.FragmentPairsGameBinding

/**
 * Экран игры «Найди пару» (game id 5).
 *
 * Сетка закрытых карточек, под которыми спрятаны пары эмодзи. Игрок открывает
 * по две карточки: совпавшие остаются открытыми, несовпавшие показываются
 * [HIDE_DELAY_MILLIS] мс (ввод в это время заблокирован) и закрываются,
 * а за ошибку начисляется штраф. Победа — когда найдены все пары.
 *
 * Реализует общий контракт экрана игры (см. `.claude/rules/games.md`):
 * аргументы `alarm id`/`start time`/`test`/`difficulty`/`music path`,
 * снятие уведомления в `onResume`, перезапуск будильника в `onPause`,
 * повторное включение мелодии каждые две минуты бездействия и переход
 * к результату с `time`/`score`/`game id` при победе.
 */
class PairsGameFragment : Fragment() {

    private lateinit var binding: FragmentPairsGameBinding
    private lateinit var viewModel: PairsGameViewModel

    private val hideHandler = Handler(Looper.getMainLooper())
    private var isInputLocked = false
    private val cardButtons = mutableListOf<Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test"))
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_pairsGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentPairsGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[PairsGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.pairsTimeTextView.text = it
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

        // После пересоздания view (поворот экрана) отложенное закрытие потеряно —
        // закрываем возможную несовпавшую пару сразу, чтобы не заблокировать игру.
        viewModel.game.hideMismatched()

        buildGrid()
        refreshAllCards()
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

    override fun onDestroyView() {
        hideHandler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    /** Создаёт кнопки-карточки в сетке по колоде из ViewModel. */
    private fun buildGrid() {
        val game = viewModel.game
        val columns = pairsColumnsFor(viewModel.difficulty)

        binding.pairsGridLayout.removeAllViews()
        binding.pairsGridLayout.columnCount = columns
        binding.pairsGridLayout.rowCount = game.cards.size / columns

        cardButtons.clear()
        val margin = (CARD_MARGIN_DP * resources.displayMetrics.density).toInt()

        for (index in game.cards.indices) {
            val button = Button(requireContext())
            button.textSize = CARD_TEXT_SIZE_SP
            button.layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, 1f)
            ).apply {
                width = 0
                height = 0
                setMargins(margin, margin, margin, margin)
            }
            button.setOnClickListener { onCardClick(index) }
            cardButtons.add(button)
            binding.pairsGridLayout.addView(button)
        }
    }

    /** Обрабатывает нажатие на карточку с индексом [index]. */
    private fun onCardClick(index: Int) {
        if (isInputLocked)
            return

        when (val result = viewModel.game.openCard(index)) {
            is PairsMoveResult.FirstRevealed -> refreshCard(result.index)

            is PairsMoveResult.Match -> {
                refreshCard(result.first)
                refreshCard(result.second)
                updateStats()
                if (result.isWin)
                    finishGame()
            }

            is PairsMoveResult.Mismatch -> {
                refreshCard(result.first)
                refreshCard(result.second)
                viewModel.mistake()
                updateStats()
                isInputLocked = true
                hideHandler.postDelayed({
                    viewModel.game.hideMismatched()
                    refreshCard(result.first)
                    refreshCard(result.second)
                    isInputLocked = false
                }, HIDE_DELAY_MILLIS)
            }

            PairsMoveResult.Ignored -> Unit
        }
    }

    /** Приводит вид карточки [index] в соответствие состоянию игры. */
    private fun refreshCard(index: Int) {
        val game = viewModel.game
        val button = cardButtons[index]
        when {
            game.isMatched(index) -> {
                button.text = game.cards[index]
                button.isEnabled = false
                button.alpha = MATCHED_CARD_ALPHA
            }

            game.isRevealed(index) -> {
                button.text = game.cards[index]
                button.isEnabled = true
                button.alpha = 1f
            }

            else -> {
                button.text = CARD_HIDDEN_TEXT
                button.isEnabled = true
                button.alpha = 1f
            }
        }
    }

    /** Обновляет вид всех карточек (после создания сетки). */
    private fun refreshAllCards() {
        for (index in cardButtons.indices)
            refreshCard(index)
    }

    /** Обновляет счётчики найденных пар и ошибок. */
    private fun updateStats() {
        val game = viewModel.game
        binding.pairsStatsTextView.text =
            "Пары: ${game.matchedPairsCount}/${game.totalPairsCount}   Ошибки: ${game.mistakes}"
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
                    R.id.action_pairsGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_pairsGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }

    private companion object {
        /** Id игры «Найди пару» в `ALL_GAMES`. */
        const val GAME_ID = 5

        /** Сколько миллисекунд показывать несовпавшую пару перед закрытием. */
        const val HIDE_DELAY_MILLIS = 700L

        /** Текст на закрытой карточке. */
        const val CARD_HIDDEN_TEXT = "?"

        /** Прозрачность найденной карточки. */
        const val MATCHED_CARD_ALPHA = 0.5f

        /** Отступ вокруг карточки, dp. */
        const val CARD_MARGIN_DP = 4

        /** Размер текста (эмодзи) на карточке, sp. */
        const val CARD_TEXT_SIZE_SP = 26f
    }
}
