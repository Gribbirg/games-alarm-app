package com.example.smartalarm.ui.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.example.smartalarm.R
import com.example.smartalarm.data.receivers.AlarmReceiver
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

        AlarmReceiver.stopAudio(context)

        binding = FragmentGameResultBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[GameResultViewModel::class.java]

        with(requireArguments()) {
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

        viewModel.currentUser.observe(viewLifecycleOwner) {
            if (it != null)
                binding.goodMorningTextView.text = "Доброе утро,\n${it.name}!"
        }

        binding.closeButton.setOnClickListener {
            goAway()
        }

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