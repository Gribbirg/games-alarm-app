package com.example.smartalarm.feature.games.weekday

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
import com.example.smartalarm.feature.games.weekday.R
import com.example.smartalarm.feature.games.weekday.databinding.FragmentWeekdayGameBinding

/**
 * Экран игры «День недели» (game id = 25).
 *
 * Игрок отвечает на вопросы вида «Сегодня — среда. Какой день недели будет
 * через 9 дней?», выбирая один из четырёх вариантов. Реализует общий контракт
 * экрана игры (см. `.claude/rules/games.md`): снятие уведомления в onResume,
 * перезапуск будильника в onPause, периодическое включение мелодии,
 * возврат к выбору игр по «назад» в пробном режиме и переход к результату
 * с аргументами `time`/`score`/`game id` при победе.
 */
class WeekdayGameFragment : Fragment() {

    private lateinit var binding: FragmentWeekdayGameBinding
    private lateinit var viewModel: WeekdayGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test")) {
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_weekdayGameFragment2_to_gameChoiceFragment,
                                requireArguments(),
                                NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                            )
                    }
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeekdayGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[WeekdayGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.weekdayTimeTextView.text = it
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

        viewModel.questionText.observe(viewLifecycleOwner) {
            binding.questionTextView.text = it
        }
        viewModel.progressText.observe(viewLifecycleOwner) {
            binding.progressTextView.text = it
        }

        val answerButtons = listOf(
            binding.answerButton1,
            binding.answerButton2,
            binding.answerButton3,
            binding.answerButton4
        )
        viewModel.options.observe(viewLifecycleOwner) { options ->
            answerButtons.forEachIndexed { index, button ->
                button.text = options[index].displayName
                    .replaceFirstChar { it.titlecase() }
            }
        }
        answerButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                onAnswerClicked(index)
            }
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

    private fun onAnswerClicked(index: Int) {
        val day = viewModel.options.value?.getOrNull(index) ?: return
        if (viewModel.answer(day)) {
            if (viewModel.isWon)
                finishGame()
            else
                Toast.makeText(context, "Верно!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Неправильно!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun finishGame() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", 25)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_weekdayGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_weekdayGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }
}
