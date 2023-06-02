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
import com.example.smartalarm.R
import com.example.smartalarm.databinding.FragmentGameResultBinding
import com.example.smartalarm.ui.viewmodels.GameResultViewModel
import kotlin.system.exitProcess

class GameResultFragment : Fragment() {

    private lateinit var binding: FragmentGameResultBinding
    private lateinit var viewModel: GameResultViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    goAway()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentGameResultBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[GameResultViewModel::class.java]

        with(requireArguments()) {
            if (!containsKey("game id")) {
                binding.resultScoreTextView.visibility = View.GONE
                binding.resultTimeTextView.visibility = View.GONE
            } else {
                if (!getBoolean("test"))
                    viewModel.setGameResult(
                        getLong("alarm id"),
                        getInt("game id"),
                        getInt("score"),
                        getString("time")!!
                    )

                binding.resultScoreTextView.text = "Очки: ${getInt("score")}"
                binding.resultTimeTextView.text = "Время: ${getString("time")}"
            }
        }

        viewModel.currentUser.observe(viewLifecycleOwner) {
            binding.goodMorningTextView.text = it
        }

        viewModel.currentTime.observe(viewLifecycleOwner) {
            binding.timeTextView.text = it
            if (it == "00:00") {
                binding.dateTextView.text = viewModel.getCurrentDate()
            }
        }

        binding.closeButton.setOnClickListener {
            goAway()
        }

        binding.dateTextView.text = viewModel.getCurrentDate()

        return binding.root
    }

    private fun goAway() {
        if (requireArguments().getBoolean("test")) {
            Navigation.findNavController(binding.root).navigate(
                R.id.action_gameResultFragment2_to_gameChoiceFragment,
                requireArguments(),
                NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
            )
        } else {
            activity?.finish()
            exitProcess(0)
        }
    }
}