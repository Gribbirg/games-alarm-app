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
