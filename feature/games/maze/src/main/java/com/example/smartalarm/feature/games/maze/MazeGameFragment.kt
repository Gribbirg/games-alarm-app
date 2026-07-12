package com.example.smartalarm.feature.games.maze

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
import com.example.smartalarm.feature.games.maze.R
import com.example.smartalarm.feature.games.maze.databinding.FragmentMazeGameBinding

/**
 * Экран игры «Лабиринт» (game id = 9).
 *
 * Игрок кнопками ↑↓←→ ведёт фишку из левого верхнего угла к выходу
 * в правом нижнем; попытка пройти сквозь стену — штраф −10 очков.
 * Реализует стандартный контракт экрана игры (аргументы «alarm id»,
 * «start time», «test», «difficulty», «music path»; снятие уведомления,
 * перезапуск будильника, периодическое включение мелодии) по образцу
 * CalcGameFragment.
 */
class MazeGameFragment : Fragment() {

    private lateinit var binding: FragmentMazeGameBinding
    private lateinit var viewModel: MazeGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test")) {
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_mazeGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentMazeGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[MazeGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.mazeTimeTextView.text = it
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

        binding.mazeView.setMaze(viewModel.gameLogic.maze)
        binding.mazeView.setPlayerPosition(
            viewModel.gameLogic.playerRow,
            viewModel.gameLogic.playerCol
        )

        binding.upButton.setOnClickListener { move(Direction.UP) }
        binding.downButton.setOnClickListener { move(Direction.DOWN) }
        binding.leftButton.setOnClickListener { move(Direction.LEFT) }
        binding.rightButton.setOnClickListener { move(Direction.RIGHT) }

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

    private fun move(direction: Direction) {
        when (viewModel.tryMove(direction)) {
            MoveResult.MOVED -> updatePlayer()

            MoveResult.BLOCKED -> {
                Toast.makeText(context, "Там стена!", Toast.LENGTH_SHORT).show()
                viewModel.mistake()
            }

            MoveResult.FINISHED -> {
                updatePlayer()
                finishGame()
            }
        }
    }

    private fun updatePlayer() {
        binding.mazeView.setPlayerPosition(
            viewModel.gameLogic.playerRow,
            viewModel.gameLogic.playerCol
        )
    }

    private fun finishGame() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", 9)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_mazeGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_mazeGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }
}
