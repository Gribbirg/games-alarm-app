package com.example.smartalarm.feature.games.stroop

import android.content.res.ColorStateList
import android.os.Bundle
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
import com.example.smartalarm.feature.games.stroop.R
import com.example.smartalarm.feature.games.stroop.databinding.FragmentStroopGameBinding

/**
 * Экран мини-игры «Цвет и слово» (эффект Струпа, game id = 7).
 *
 * На экране слово-название цвета, окрашенное в другой цвет. Игрок нажимает
 * кнопку цвета, которым слово написано (на сложности 3 иногда — наоборот,
 * цвета, который слово означает; об этом явно предупреждает подсказка).
 * Победа — [StroopSettings.roundsToWin] верных ответов.
 *
 * Реализует общий контракт экрана игры (см. `.claude/rules/games.md`):
 * снятие уведомления в onResume, пересоздание будильника в onPause,
 * повтор мелодии каждые две минуты, возврат к выбору игр по «назад»
 * в режиме пробы и переход к экрану результата при победе.
 */
class StroopGameFragment : Fragment() {

    private lateinit var binding: FragmentStroopGameBinding
    private lateinit var viewModel: StroopGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test"))
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_stroopGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentStroopGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[StroopGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.stroopTimeTextView.text = it
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

        viewModel.startGame(requireArguments().getInt("difficulty"))

        optionButtons().forEachIndexed { index, button ->
            button.setOnClickListener { onOptionClick(index) }
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

    private fun optionButtons() = listOf(
        binding.stroopOption1Button,
        binding.stroopOption2Button,
        binding.stroopOption3Button,
        binding.stroopOption4Button,
    )

    /** Отрисовывает текущий раунд: слово, подсказку режима и кнопки вариантов. */
    private fun showRound() {
        val round = viewModel.game.currentRound

        binding.stroopWordTextView.text = round.word.displayName()
        binding.stroopWordTextView.setTextColor(round.ink.argb())

        when (round.mode) {
            StroopQuestionMode.INK -> {
                binding.stroopTaskTextView.text = "Каким ЦВЕТОМ написано слово?"
                binding.stroopInvertedHintTextView.visibility = View.GONE
            }

            StroopQuestionMode.MEANING -> {
                binding.stroopTaskTextView.text = "Что слово ОЗНАЧАЕТ?"
                binding.stroopInvertedHintTextView.visibility = View.VISIBLE
            }
        }

        optionButtons().forEachIndexed { index, button ->
            val color = round.options.getOrNull(index)
            if (color != null) {
                button.visibility = View.VISIBLE
                button.text = color.displayName()
                button.backgroundTintList = ColorStateList.valueOf(color.argb())
                button.setTextColor(color.onArgb())
            } else {
                button.visibility = View.INVISIBLE
            }
        }

        binding.stroopProgressTextView.text =
            "Верных ответов: ${viewModel.game.correctCount} / ${viewModel.game.settings.roundsToWin}"
    }

    private fun onOptionClick(index: Int) {
        val color = viewModel.game.currentRound.options.getOrNull(index) ?: return

        if (viewModel.game.answer(color)) {
            if (viewModel.game.isWon)
                finishGame()
            else
                showRound()
        } else {
            Toast.makeText(context, "Неправильно!", Toast.LENGTH_SHORT).show()
            showRound()
        }
    }

    /** Кладёт результат в аргументы и переходит к экрану результата. */
    private fun finishGame() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", 7)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_stroopGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_stroopGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }
}
