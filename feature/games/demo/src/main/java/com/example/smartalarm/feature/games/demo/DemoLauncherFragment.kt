package com.example.smartalarm.feature.games.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.smartalarm.feature.games.demo.databinding.FragmentDemoLauncherBinding
import com.google.android.material.button.MaterialButton

/**
 * Список всех мини-игр: выбор сложности и запуск любой игры в один тап.
 *
 * Игры запускаются с теми же аргументами, что и из выбора игр основного
 * приложения в режиме пробы («test» = true), поэтому будильник, уведомления
 * и запись рекордов им не нужны.
 *
 * Новая игра добавляется одной строкой в [DEMO_GAMES] (плюс destination
 * в nav_games_demo_graph.xml и зависимость в build.gradle демо-модуля).
 */
class DemoLauncherFragment : Fragment() {

    /**
     * Игра в демо-списке.
     *
     * @property title название кнопки
     * @property actionId action из nav_games_demo_graph.xml, ведущий к игре
     */
    private data class DemoGame(val title: String, val actionId: Int)

    private lateinit var binding: FragmentDemoLauncherBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDemoLauncherBinding.inflate(inflater, container, false)

        val demoGames = listOf(
            DemoGame(getString(R.string.demo_game_calc), R.id.action_demoLauncherFragment_to_calcGameFragment2),
            DemoGame(getString(R.string.demo_game_memory), R.id.action_demoLauncherFragment_to_memoryGameFragment2),
            DemoGame(getString(R.string.demo_game_equation), R.id.action_demoLauncherFragment_to_equationGameFragment2),
            DemoGame(getString(R.string.demo_game_sorting), R.id.action_demoLauncherFragment_to_sortingGameFragment2),
            DemoGame(getString(R.string.demo_game_pairs), R.id.action_demoLauncherFragment_to_pairsGameFragment2),
            DemoGame(getString(R.string.demo_game_sequence), R.id.action_demoLauncherFragment_to_sequenceGameFragment2),
            DemoGame(getString(R.string.demo_game_stroop), R.id.action_demoLauncherFragment_to_stroopGameFragment2),
            DemoGame(getString(R.string.demo_game_oddoneout), R.id.action_demoLauncherFragment_to_oddoneoutGameFragment2),
            DemoGame(getString(R.string.demo_game_maze), R.id.action_demoLauncherFragment_to_mazeGameFragment2),
            DemoGame(getString(R.string.demo_game_anagram), R.id.action_demoLauncherFragment_to_anagramGameFragment2),
            DemoGame(getString(R.string.demo_game_truefalse), R.id.action_demoLauncherFragment_to_truefalseGameFragment2),
            DemoGame(getString(R.string.demo_game_reaction), R.id.action_demoLauncherFragment_to_reactionGameFragment2),
            DemoGame(getString(R.string.demo_game_counter), R.id.action_demoLauncherFragment_to_counterGameFragment2),
            DemoGame(getString(R.string.demo_game_lights), R.id.action_demoLauncherFragment_to_lightsGameFragment2),
            DemoGame(getString(R.string.demo_game_fifteen), R.id.action_demoLauncherFragment_to_fifteenGameFragment2),
            DemoGame(getString(R.string.demo_game_chain), R.id.action_demoLauncherFragment_to_chainGameFragment2),
            DemoGame(getString(R.string.demo_game_roman), R.id.action_demoLauncherFragment_to_romanGameFragment2),
            DemoGame(getString(R.string.demo_game_clock), R.id.action_demoLauncherFragment_to_clockGameFragment2),
            DemoGame(getString(R.string.demo_game_digits), R.id.action_demoLauncherFragment_to_digitsGameFragment2),
            DemoGame(getString(R.string.demo_game_hanoi), R.id.action_demoLauncherFragment_to_hanoiGameFragment2),
            DemoGame(getString(R.string.demo_game_targetsum), R.id.action_demoLauncherFragment_to_targetsumGameFragment2),
            DemoGame(getString(R.string.demo_game_matrix), R.id.action_demoLauncherFragment_to_matrixGameFragment2),
            DemoGame(getString(R.string.demo_game_mole), R.id.action_demoLauncherFragment_to_moleGameFragment2),
            DemoGame(getString(R.string.demo_game_pattern), R.id.action_demoLauncherFragment_to_patternGameFragment2),
            DemoGame(getString(R.string.demo_game_weekday), R.id.action_demoLauncherFragment_to_weekdayGameFragment2),
            DemoGame(getString(R.string.demo_game_percent), R.id.action_demoLauncherFragment_to_percentGameFragment2),
            DemoGame(getString(R.string.demo_game_binary), R.id.action_demoLauncherFragment_to_binaryGameFragment2),
            DemoGame(getString(R.string.demo_game_reverse), R.id.action_demoLauncherFragment_to_reverseGameFragment2),
            DemoGame(getString(R.string.demo_game_capitals), R.id.action_demoLauncherFragment_to_capitalsGameFragment2),
            DemoGame(getString(R.string.demo_game_spelling), R.id.action_demoLauncherFragment_to_spellingGameFragment2),
            DemoGame(getString(R.string.demo_game_dice), R.id.action_demoLauncherFragment_to_diceGameFragment2),
        )

        for (game in demoGames) {
            val button = MaterialButton(requireContext()).apply {
                text = game.title
                setOnClickListener { startGame(game.actionId) }
            }
            binding.gamesContainer.addView(
                button,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        return binding.root
    }

    private fun startGame(actionId: Int) {
        val difficulty = when (binding.difficultyRadioGroup.checkedRadioButtonId) {
            R.id.easyRadioButton -> 1
            R.id.hardRadioButton -> 3
            else -> 2
        }
        val bundle = Bundle().apply {
            putBoolean("test", true)
            putInt("difficulty", difficulty)
            putLong("alarm id", -1L)
        }
        Navigation.findNavController(binding.root).navigate(actionId, bundle)
    }
}
