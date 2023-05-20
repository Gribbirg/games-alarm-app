package com.example.smartalarm.ui.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.smartalarm.R
import com.example.smartalarm.databinding.FragmentCalcGameBinding
import com.example.smartalarm.ui.viewmodels.CalcGameViewModel

class CalcGameFragment : Fragment() {

    private lateinit var binding: FragmentCalcGameBinding
    private lateinit var viewModel: CalcGameViewModel

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
            if (
                binding.sumEditText.text.toString().toInt() == viewModel.arifData.sumResult &&
                binding.multEditText.text.toString().toInt() == viewModel.arifData.multResult
            ) {

                val bundle = requireArguments()
                bundle.putString("time", viewModel.timeCurrentString.value)
                bundle.putInt("score", viewModel.finishScore())

                if (requireArguments().getBoolean("test"))
                    Navigation.findNavController(binding.root)
                        .navigate(R.id.action_calcGameFragment2_to_gameResultFragment2, bundle)
                else
                    Navigation.findNavController(binding.root)
                        .navigate(R.id.action_calcGameFragment_to_gameResultFragment, bundle)

            } else {
                Toast.makeText(context, "Неправильно!", Toast.LENGTH_SHORT).show()
                viewModel.mistake()
            }
        }

        return binding.root
    }
}