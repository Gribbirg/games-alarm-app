package com.example.smartalarm.feature.games.reaction

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
import com.example.smartalarm.feature.games.reaction.R
import com.example.smartalarm.feature.games.reaction.databinding.FragmentReactionGameBinding

/**
 * Экран мини-игры «Поймай момент» (game id = 12).
 *
 * По горизонтальной шкале туда-обратно бегает бегунок (треугольная волна,
 * [ReactionWave]); на шкале подсвечена целевая зона. Игрок жмёт большую
 * кнопку «Стоп», когда бегунок в зоне. Победа — [ReactionSettings.hitsToWin]
 * попаданий; после каждого попадания зона перегенерируется в случайном
 * месте, промах — минус очки.
 *
 * Реализует общий контракт экрана игры (см. `.claude/rules/games.md`):
 * снятие уведомления в onResume, пересоздание будильника в onPause,
 * повтор мелодии каждые две минуты, возврат к выбору игр по «назад»
 * в режиме пробы и переход к экрану результата при победе.
 */
class ReactionGameFragment : Fragment() {

    private lateinit var binding: FragmentReactionGameBinding
    private lateinit var viewModel: ReactionGameViewModel

    private val tickHandler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            binding.reactionScaleView.setIndicatorPosition(currentPosition().toFloat())
            tickHandler.postDelayed(this, TICK_INTERVAL_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test"))
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_reactionGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentReactionGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ReactionGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.reactionTimeTextView.text = it
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

        binding.reactionStopButton.setOnClickListener { onStopClick() }

        showZone()
        showProgress()

        return binding.root
    }

    override fun onResume() {
        if (!requireArguments().getBoolean("test")) {
            val notificationManager = NotificationManagerCompat.from(requireContext())
            notificationManager.cancel(requireArguments().getLong("alarm id").toInt())
        }
        tickHandler.post(ticker)
        super.onResume()
    }

    override fun onPause() {
        tickHandler.removeCallbacks(ticker)
        if (!requireArguments().getBoolean("test"))
            viewModel.startNewAlarm()
        super.onPause()
    }

    /** Позиция бегунка (0..1) для текущего момента времени. */
    private fun currentPosition(): Double = ReactionWave.position(
        System.currentTimeMillis() - viewModel.waveStartMs,
        viewModel.game.settings.periodMs
    )

    private fun onStopClick() {
        if (viewModel.game.isWon) return

        if (viewModel.game.press(currentPosition())) {
            if (viewModel.game.isWon) {
                finishGame()
            } else {
                showZone()
                showProgress()
            }
        } else {
            Toast.makeText(context, "Мимо!", Toast.LENGTH_SHORT).show()
            showProgress()
        }
    }

    /** Отрисовывает текущую целевую зону на шкале. */
    private fun showZone() {
        val zone = viewModel.game.zone
        binding.reactionScaleView.setZone(zone.start.toFloat(), zone.end.toFloat())
    }

    /** Обновляет счётчик «Попаданий: k / N». */
    private fun showProgress() {
        binding.reactionProgressTextView.text =
            "Попаданий: ${viewModel.game.hitCount} / ${viewModel.game.settings.hitsToWin}"
    }

    /** Кладёт результат в аргументы и переходит к экрану результата. */
    private fun finishGame() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", 12)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_reactionGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_reactionGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }

    companion object {
        /** Интервал перерисовки бегунка, мс (~60 кадров в секунду). */
        private const val TICK_INTERVAL_MS = 16L
    }
}
