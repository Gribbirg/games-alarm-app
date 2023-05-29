package com.example.smartalarm.ui.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.example.smartalarm.R
import com.example.smartalarm.databinding.FragmentCalcGameBinding
import com.example.smartalarm.services.AlarmMediaPlayer
import com.example.smartalarm.services.AlarmReceiver
import com.example.smartalarm.ui.viewmodels.CalcGameViewModel

class CalcGameFragment : Fragment() {

    private lateinit var binding: FragmentCalcGameBinding
    private lateinit var viewModel: CalcGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Navigation.findNavController(binding.root)
                        .navigate(
                            R.id.action_calcGameFragment2_to_gameChoiceFragment,
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
        binding = FragmentCalcGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[CalcGameViewModel::class.java]

        viewModel.setStartTime(requireArguments().getLong("start time", 0))
        if (!requireArguments().getBoolean("test"))
            viewModel.getAlarm(requireArguments().getLong("alarm id"))

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.calcTimeTextView.text = it
            with(it.split(".")) {
                if (this[0].toInt() % 2 == 0 && this[1].toInt() == 0 && this[0].toInt() != 0) {
                    AlarmMediaPlayer.playAudio(
                        context,
                        false,
                        false,
                        requireArguments().getString("music path")!!
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
        viewModel.generateRandom()

        binding.sumTextView.text = viewModel.arifData.sumText
        binding.multTextView.text = viewModel.arifData.multText

        binding.calcResultCheckButton.setOnClickListener {
            checkResult()
        }

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

    private fun checkResult() {
        if (viewModel.checkResult(
                binding.sumEditText.text.toString(),
                binding.multEditText.text.toString()
            )
        ) {

            val bundle = requireArguments()
            bundle.putString("time", viewModel.timeCurrentString.value)
            bundle.putInt("score", viewModel.finishScore())
            bundle.putInt("game id", 1)

            if (requireArguments().getBoolean("test"))
                Navigation.findNavController(binding.root)
                    .navigate(
                        R.id.action_calcGameFragment2_to_gameResultFragment2,
                        bundle,
                        NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                    )
            else {
                Navigation.findNavController(binding.root)
                    .navigate(
                        R.id.action_calcGameFragment_to_gameResultFragment,
                        bundle,
                        NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                    )
                viewModel.setPositiveResult()
            }
        } else {
            Toast.makeText(context, "Неправильно!", Toast.LENGTH_SHORT).show()
            viewModel.mistake()
        }
    }
}