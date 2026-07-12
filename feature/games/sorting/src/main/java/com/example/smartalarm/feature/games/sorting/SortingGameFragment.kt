package com.example.smartalarm.feature.games.sorting

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
import com.example.smartalarm.feature.games.sorting.R
import com.example.smartalarm.feature.games.sorting.databinding.FragmentSortingGameBinding

/**
 * Экран игры «По порядку» (game id = 4): сетка кнопок со случайными
 * уникальными числами, которые нужно нажимать строго по возрастанию.
 * Правильно нажатая кнопка гаснет; ошибка — минус 10 очков, без сброса раунда.
 *
 * Соблюдает общий контракт экрана игры (см. `.claude/rules/games.md`):
 * аргументы "alarm id", "start time", "test", "difficulty", "music path";
 * снятие уведомления в onResume, перезапуск будильника в onPause,
 * повторное включение мелодии каждые две минуты, возврат «назад»
 * к выбору игр в режиме пробы.
 */
class SortingGameFragment : Fragment() {

    private lateinit var binding: FragmentSortingGameBinding
    private lateinit var viewModel: SortingGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test"))
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_sortingGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentSortingGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[SortingGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.sortingTimeTextView.text = it
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
        buildGrid()

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
     * Строит сетку кнопок по текущему состоянию партии: уже нажатые
     * числа сразу отображаются погашенными (важно после поворота экрана).
     */
    private fun buildGrid() {
        val grid = binding.numbersGrid
        grid.removeAllViews()
        grid.columnCount = GRID_COLUMNS

        val margin = (4 * resources.displayMetrics.density).toInt()
        for (value in viewModel.game.numbers) {
            val button = Button(requireContext()).apply {
                text = value.toString()
                textSize = 20f
                layoutParams = GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f)
                ).apply {
                    width = 0
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    setMargins(margin, margin, margin, margin)
                }
                if (viewModel.game.isPressed(value))
                    turnOff(this)
                setOnClickListener { onNumberClicked(value, this) }
            }
            grid.addView(button)
        }
    }

    /**
     * Обрабатывает нажатие числа [value] на кнопке [button].
     */
    private fun onNumberClicked(value: Int, button: Button) {
        when (viewModel.onNumberPressed(value)) {
            PressResult.CORRECT -> turnOff(button)

            PressResult.WIN -> {
                turnOff(button)
                finishGame()
            }

            PressResult.WRONG ->
                Toast.makeText(context, "Неправильно!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * «Гасит» правильно нажатую кнопку: делает её неактивной и полупрозрачной.
     */
    private fun turnOff(button: Button) {
        button.isEnabled = false
        button.alpha = 0.3f
    }

    /**
     * Кладёт результат в аргументы и переходит к экрану результата
     * (разными action'ами для режима пробы и будильника).
     */
    private fun finishGame() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", 4)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_sortingGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_sortingGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }

    companion object {
        /** Число столбцов сетки для всех сложностей (6, 9 и 12 чисел делятся на 3). */
        private const val GRID_COLUMNS = 3
    }
}
