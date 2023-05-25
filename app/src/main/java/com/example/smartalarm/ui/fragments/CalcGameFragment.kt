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
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.example.smartalarm.R
import com.example.smartalarm.databinding.FragmentCalcGameBinding
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
        binding = FragmentCalcGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[CalcGameViewModel::class.java]

        viewModel.timeCurrentString.observe(viewLifecycleOwner) {
            binding.calcTimeTextView.text = it
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
            else
                Navigation.findNavController(binding.root)
                    .navigate(
                        R.id.action_calcGameFragment_to_gameResultFragment,
                        bundle,
                        NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                    )

        } else {
            Toast.makeText(context, "Неправильно!", Toast.LENGTH_SHORT).show()
            viewModel.mistake()
        }
    }
}