package com.example.smartalarm.feature.games.anagram

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.example.smartalarm.core.alarm.AlarmMediaPlayer
import com.example.smartalarm.feature.games.anagram.R
import com.example.smartalarm.feature.games.anagram.databinding.FragmentAnagramGameBinding
import com.example.smartalarm.feature.games.anagram.logic.SubmitResult

/**
 * Экран игры «Анаграммы».
 *
 * Игроку показываются перемешанные буквы русского слова; нажимая кнопки-буквы
 * по порядку, он собирает слово (набранное отображается сверху, «Сброс»
 * очищает набор). Чтобы выключить будильник, нужно собрать несколько слов —
 * их число и длина зависят от сложности. Логика игры — в
 * [com.example.smartalarm.feature.games.anagram.logic.AnagramGame],
 * таймер и очки — в [AnagramGameViewModel].
 */
class AnagramGameFragment : Fragment() {

    private lateinit var binding: FragmentAnagramGameBinding
    private lateinit var viewModel: AnagramGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test"))
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_anagramGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentAnagramGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[AnagramGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.anagramTimeTextView.text = it
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

        renderWord()

        binding.anagramResetButton.setOnClickListener {
            viewModel.game.resetInput()
            renderWord()
        }

        binding.anagramCheckButton.setOnClickListener {
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

    /**
     * Перерисовывает экран по текущему состоянию игры: счётчик слов,
     * собранное слово и ряды кнопок-букв (использованные буквы выключены).
     */
    private fun renderWord() {
        val game = viewModel.game

        binding.wordsProgressTextView.text =
            "Слово ${(game.wordsCompleted + 1).coerceAtMost(game.totalWords)} из ${game.totalWords}"

        binding.lettersContainer.removeAllViews()
        val typedIndices = game.typedIndices
        game.shuffledLetters.withIndex().chunked(LETTERS_PER_ROW).forEach { rowLetters ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            for ((index, letter) in rowLetters) {
                row.addView(createLetterButton(index, letter, index in typedIndices))
            }
            binding.lettersContainer.addView(row)
        }

        updateTypedWord()
    }

    /** Создаёт кнопку одной буквы; нажатая ранее буква приходит выключенной. */
    private fun createLetterButton(index: Int, letter: Char, used: Boolean): Button {
        val density = resources.displayMetrics.density
        val size = (LETTER_BUTTON_WIDTH_DP * density).toInt()
        val margin = (LETTER_BUTTON_MARGIN_DP * density).toInt()
        return Button(requireContext()).apply {
            text = letter.uppercaseChar().toString()
            textSize = 20f
            minWidth = 0
            minimumWidth = 0
            isEnabled = !used
            layoutParams = LinearLayout.LayoutParams(
                size,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(margin, 0, margin, 0) }
            setOnClickListener {
                if (viewModel.game.pressLetter(index)) {
                    isEnabled = false
                    updateTypedWord()
                }
            }
        }
    }

    /** Показывает набранное слово; несобранные буквы отображаются прочерками. */
    private fun updateTypedWord() {
        val game = viewModel.game
        val typed = game.typedWord.uppercase().toList()
        val placeholders = List(game.shuffledLetters.size - typed.size) { '_' }
        binding.typedWordTextView.text = (typed + placeholders).joinToString(" ")
    }

    private fun checkResult() {
        when (viewModel.game.submit()) {
            SubmitResult.NOT_COMPLETE ->
                Toast.makeText(context, "Слово собрано не полностью!", Toast.LENGTH_SHORT).show()

            SubmitResult.WRONG -> {
                Toast.makeText(context, "Неправильно!", Toast.LENGTH_SHORT).show()
                viewModel.mistake()
                renderWord()
            }

            SubmitResult.WORD_DONE -> {
                Toast.makeText(context, "Верно!", Toast.LENGTH_SHORT).show()
                renderWord()
            }

            SubmitResult.GAME_WON -> {
                val bundle = requireArguments()
                bundle.putString("time", viewModel.timeCurrentString.value)
                bundle.putInt("score", viewModel.finishScore())
                bundle.putInt("game id", 10)

                if (requireArguments().getBoolean("test"))
                    Navigation.findNavController(binding.root)
                        .navigate(
                            R.id.action_anagramGameFragment2_to_gameResultFragment2,
                            bundle,
                            NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                        )
                else {
                    Navigation.findNavController(binding.root)
                        .navigate(
                            R.id.action_anagramGameFragment_to_gameResultFragment,
                            bundle,
                            NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                        )
                    viewModel.setPositiveResult()
                }
            }
        }
    }

    private companion object {
        /** Максимум кнопок-букв в одном ряду. */
        const val LETTERS_PER_ROW = 6

        /** Ширина кнопки-буквы, dp. */
        const val LETTER_BUTTON_WIDTH_DP = 52

        /** Горизонтальный отступ кнопки-буквы, dp. */
        const val LETTER_BUTTON_MARGIN_DP = 2
    }
}
