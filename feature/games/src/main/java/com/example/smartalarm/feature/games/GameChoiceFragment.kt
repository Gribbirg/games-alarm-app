package com.example.smartalarm.feature.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartalarm.feature.games.R
import com.example.smartalarm.core.data.model.AlarmGameData
import com.example.smartalarm.feature.games.databinding.FragmentGameChoiceBinding
import com.example.smartalarm.feature.games.GameAdapter
import com.example.smartalarm.feature.games.GameChoiceViewModel

class GameChoiceFragment : Fragment(), GameAdapter.OnGameClickListener {

    private lateinit var viewModel: GameChoiceViewModel
    private lateinit var binding: FragmentGameChoiceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Navigation.findNavController(binding.root)
                        .navigate(
                            R.id.action_gameChoiceFragment_to_addAlarmFragment,
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
        binding = FragmentGameChoiceBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[GameChoiceViewModel::class.java]

        viewModel.getGames(arguments?.getIntegerArrayList("games")!!)

        viewModel.gamesRecycler.observe(viewLifecycleOwner) {
            binding.gamesRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = GameAdapter(it, this@GameChoiceFragment)
            }
        }

        binding.saveButton.setOnClickListener {
            val bundle = requireArguments()
            bundle.putIntegerArrayList("games", viewModel.getDifficultiesList())
            Navigation.findNavController(binding.root)
                .navigate(
                    R.id.action_gameChoiceFragment_to_addAlarmFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        }

        return binding.root
    }

    override fun onOnOffSwitchClickListener(gameData: AlarmGameData) {
        viewModel.games[gameData.id - 1].isOn = gameData.isOn
    }

    override fun onChangeDifficultyClickListener(gameData: AlarmGameData) {
        viewModel.games[gameData.id - 1].difficulty = gameData.difficulty
    }

    override fun testButtonClickListener(gameData: AlarmGameData) {
        val bundle = requireArguments()
        bundle.putBoolean("test", true)
        bundle.putInt("difficulty", gameData.difficulty)
        bundle.putIntegerArrayList("games", viewModel.getDifficultiesList())
        val navController = Navigation.findNavController(binding.root)
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
        when (gameData.id) {
            1 -> navController.navigate(
                R.id.action_gameChoiceFragment_to_calcGameFragment2,
                bundle,
                navOptions
            )

            2 -> navController.navigate(
                R.id.action_gameChoiceFragment_to_memoryGameFragment2,
                bundle,
                navOptions
            )

            3 -> navController.navigate(
                R.id.action_gameChoiceFragment_to_equationGameFragment2,
                bundle,
                navOptions
            )

            4 -> navController.navigate(
                R.id.action_gameChoiceFragment_to_sortingGameFragment2,
                bundle,
                navOptions
            )

            5 -> navController.navigate(
                R.id.action_gameChoiceFragment_to_pairsGameFragment2,
                bundle,
                navOptions
            )

            6 -> navController.navigate(
                R.id.action_gameChoiceFragment_to_sequenceGameFragment2,
                bundle,
                navOptions
            )

            7 -> navController.navigate(
                R.id.action_gameChoiceFragment_to_stroopGameFragment2,
                bundle,
                navOptions
            )

            8 -> navController.navigate(
                R.id.action_gameChoiceFragment_to_oddoneoutGameFragment2,
                bundle,
                navOptions
            )

            9 -> navController.navigate(
                R.id.action_gameChoiceFragment_to_mazeGameFragment2,
                bundle,
                navOptions
            )

            10 -> navController.navigate(
                R.id.action_gameChoiceFragment_to_anagramGameFragment2,
                bundle,
                navOptions
            )

            11 -> navController.navigate(
                R.id.action_gameChoiceFragment_to_truefalseGameFragment2,
                bundle,
                navOptions
            )
        }
    }
}