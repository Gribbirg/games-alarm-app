package com.example.smartalarm.ui.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.smartalarm.R
import com.example.smartalarm.ui.viewmodels.AddAlarmFragmentViewModel
import com.example.smartalarm.databinding.FragmentAddAlarmBinding
import com.example.smartalarm.ui.activities.MainActivity
import kotlinx.coroutines.launch

class AddAlarmFragment : Fragment() {

    private lateinit var viewModel: AddAlarmFragmentViewModel
    lateinit var binding: FragmentAddAlarmBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[AddAlarmFragmentViewModel::class.java]
        binding = FragmentAddAlarmBinding.inflate(inflater, container, false)

        binding.addAlarmSaveButton.setOnClickListener {

            lifecycleScope.launch {
                viewModel.insertAlarmToDb(
                    binding.hourEditText.text.toString().toInt(),
                    binding.minuteEditText.text.toString().toInt(),
                    requireArguments().getInt("currentDayNumber"),
                    binding.nameEditText.text.toString()
                )
                onResume()
            }

            Navigation.findNavController(binding.root)
                .navigate(R.id.action_addAlarmFragment_to_alarmsFragment2)
        }

        return binding.root
    }

}