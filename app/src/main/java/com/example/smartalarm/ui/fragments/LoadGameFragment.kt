package com.example.smartalarm.ui.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.smartalarm.R
import com.example.smartalarm.databinding.FragmentLoadGameBinding
import com.example.smartalarm.ui.viewmodels.LoadGameViewModel

class LoadGameFragment : Fragment() {

    private lateinit var binding: FragmentLoadGameBinding
    private lateinit var viewModel: LoadGameViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoadGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[LoadGameViewModel::class.java]

        viewModel.currentGame.observe(viewLifecycleOwner) {
            val bundle = Bundle()
            bundle.putLong("alarm id", requireActivity().intent.getLongExtra("alarm id", -1))
            bundle.putBoolean("test", false)
            bundle.putInt("difficulty", it[1])

            val navController = Navigation.findNavController(binding.root)
            when (it[0]) {
                1 -> navController.navigate(
                    R.id.action_loadGameFragment_to_calcGameFragment,
                    bundle
                )

                2 -> navController.navigate(
                    R.id.action_loadGameFragment_to_taskGameFragment,
                    bundle
                )
            }
        }

        viewModel.getAlarm(requireActivity().intent.getLongExtra("alarm id", -1))

        return binding.root
    }

}