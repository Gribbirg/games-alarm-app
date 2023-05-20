package com.example.smartalarm.ui.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartalarm.ui.viewmodels.GameChoiceViewModel
import com.example.smartalarm.R
import com.example.smartalarm.data.data.AlarmGameData
import com.example.smartalarm.databinding.FragmentGameChoiceBinding
import com.example.smartalarm.ui.adapters.GameAdapter

class GameChoiceFragment : Fragment(), GameAdapter.OnGameClickListener {

    private lateinit var viewModel: GameChoiceViewModel
    private lateinit var binding: FragmentGameChoiceBinding

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
                .navigate(R.id.action_gameChoiceFragment_to_addAlarmFragment, bundle)
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
        bundle.putBoolean("is test", true)
        bundle.putInt("difficulty", gameData.difficulty)
        val navController = Navigation.findNavController(binding.root)
        when (gameData.id) {
            1 -> navController.navigate(
                R.id.action_gameChoiceFragment_to_calcGameFragment2,
                bundle
            )

            2 -> navController.navigate(
                R.id.action_gameChoiceFragment_to_taskGameFragment2,
                bundle
            )
        }
    }
}