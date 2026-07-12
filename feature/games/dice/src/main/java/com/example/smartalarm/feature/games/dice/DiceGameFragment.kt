package com.example.smartalarm.feature.games.dice

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
import com.example.smartalarm.feature.games.dice.R
import com.example.smartalarm.feature.games.dice.databinding.FragmentDiceGameBinding

/**
 * Экран игры «Кубики» (game id = [DICE_GAME_ID]).
 *
 * Показывает бросок игральных костей (крупные юникод-символы ⚀..⚅);
 * игрок складывает выпавшие значения и вводит сумму. Верная сумма открывает
 * следующий раунд, неверная — штрафуется и перебрасывает кости того же
 * раунда; после последнего раунда происходит переход к экрану результата.
 * На сложности 3 действует правило «кости с чётным значением считаются
 * удвоенными» — подсказка о нём показывается над костями.
 * Состояние игры хранится в [DiceGameViewModel] и переживает поворот экрана.
 */
class DiceGameFragment : Fragment() {

    private lateinit var binding: FragmentDiceGameBinding
    private lateinit var viewModel: DiceGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getBoolean("test"))
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_diceGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentDiceGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[DiceGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.diceTimeTextView.text = it
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

        binding.ruleTextView.visibility =
            if (viewModel.game.doubleEven) View.VISIBLE else View.GONE

        binding.diceCheckButton.setOnClickListener {
            checkAnswer()
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

    /** Перерисовывает кости текущего броска и счётчик раундов. */
    private fun showRound() {
        val game = viewModel.game
        binding.diceTextView.text = diceText(game.currentRound.values)
        binding.roundTextView.text = "Раунд ${game.roundNumber} из ${game.totalRounds}"
    }

    /** Проверяет введённую сумму и реагирует на вердикт игры. */
    private fun checkAnswer() {
        when (viewModel.answer(binding.diceAnswerEditText.text.toString())) {
            AnswerResult.EMPTY ->
                Toast.makeText(context, "Введите сумму!", Toast.LENGTH_SHORT).show()

            AnswerResult.WRONG -> {
                Toast.makeText(context, "Неправильно!", Toast.LENGTH_SHORT).show()
                binding.diceAnswerEditText.text.clear()
                showRound()
            }

            AnswerResult.NEXT_ROUND -> {
                binding.diceAnswerEditText.text.clear()
                showRound()
            }

            AnswerResult.WIN -> win()
        }
    }

    /** Кладёт результат в аргументы и переходит к экрану результата. */
    private fun win() {
        val bundle = requireArguments()
        bundle.putString("time", viewModel.timeCurrentString.value)
        bundle.putInt("score", viewModel.finishScore())
        bundle.putInt("game id", DICE_GAME_ID)

        if (requireArguments().getBoolean("test"))
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_diceGameFragment2_to_gameResultFragment2,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        else {
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_diceGameFragment_to_gameResultFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
            viewModel.setPositiveResult()
        }
    }
}
