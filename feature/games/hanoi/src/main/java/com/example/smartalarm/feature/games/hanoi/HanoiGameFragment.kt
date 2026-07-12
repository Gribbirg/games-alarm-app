package com.example.smartalarm.feature.games.hanoi

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
import com.example.smartalarm.feature.games.hanoi.R
import com.example.smartalarm.feature.games.hanoi.databinding.FragmentHanoiGameBinding

/**
 * Экран игры «Ханойская башня».
 *
 * Три стержня, башня из дисков на первом; нужно перенести её на третий,
 * не кладя больший диск на меньший. Управление в два тапа: тап по
 * стержню-источнику выделяет его верхний диск, тап по стержню-цели
 * выполняет ход; повторный тап по источнику снимает выделение.
 * Недопустимый ход — штраф −10 очков и Toast. Сложность задаёт число
 * дисков (3/4/5), под счётчиком ходов показан минимум `2^N − 1`.
 *
 * Реализует общий контракт экрана игры (см. `.claude/rules/games.md`):
 * снятие уведомления в `onResume`, перезапуск будильника в `onPause`,
 * повторное включение мелодии каждые две минуты, возврат к выбору игр
 * по системной «назад» в пробном режиме и переход к результату с
 * `time`/`score`/`game id` при победе.
 */
class HanoiGameFragment : Fragment() {

    private lateinit var binding: FragmentHanoiGameBinding
    private lateinit var viewModel: HanoiGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test"))
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_hanoiGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentHanoiGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[HanoiGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.hanoiTimeTextView.text = it
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
        viewModel.ensureGame()

        binding.hanoiView.onRodTapped = { rod -> onRodTapped(rod) }
        render()

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
     * Обрабатывает тап по стержню [rod]: выбор источника, снятие
     * выделения, ход или штраф за недопустимый ход.
     */
    private fun onRodTapped(rod: Int) {
        val game = viewModel.ensureGame()
        if (game.isWon) return

        when (val selected = viewModel.selectedRod) {
            null ->
                // Пустой стержень источником быть не может — тап игнорируется.
                if (game.topDisk(rod) != null)
                    viewModel.selectedRod = rod

            rod -> viewModel.selectedRod = null

            else -> when (game.move(selected, rod)) {
                MoveResult.OK -> viewModel.selectedRod = null

                MoveResult.ILLEGAL -> {
                    // Выделение не снимаем: можно сразу выбрать другую цель.
                    viewModel.mistake()
                    Toast.makeText(
                        context,
                        "Нельзя класть больший диск на меньший!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                MoveResult.WIN -> {
                    viewModel.selectedRod = null
                    render()
                    finishGame()
                    return
                }
            }
        }
        render()
    }

    /** Перерисовывает башню и счётчик ходов по состоянию из ViewModel. */
    private fun render() {
        val game = viewModel.ensureGame()
        binding.hanoiView.setState(game.rodsSnapshot(), game.diskCount, viewModel.selectedRod)
        binding.movesTextView.text = "Ходы: ${game.moveCount} (мин. ${game.minMoves})"
    }

    /** Кладёт результат в аргументы и переходит к экрану результата. */
    private fun finishGame() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", 20)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_hanoiGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_hanoiGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }
}
