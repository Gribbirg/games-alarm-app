package com.example.smartalarm.ui.fragments

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
import com.example.smartalarm.R
import com.example.smartalarm.data.data.AlarmGameData
import com.example.smartalarm.databinding.FragmentGameChoiceBinding
import com.example.smartalarm.ui.adapters.GameAdapter
import com.example.smartalarm.ui.viewmodels.GameChoiceViewModel

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
        when (gameData.id) {
            1 -> navController.navigate(
                R.id.action_gameChoiceFragment_to_calcGameFragment2,
                bundle,
                NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
            )

            2 -> navController.navigate(
                R.id.action_gameChoiceFragment_to_taskGameFragment2,
                bundle,
                NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
            )
        }
    }
}