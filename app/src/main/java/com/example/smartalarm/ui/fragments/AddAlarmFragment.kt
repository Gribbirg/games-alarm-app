package com.example.smartalarm.ui.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.smartalarm.ui.viewmodels.AddAlarmFragmentViewModel
import com.example.smartalarm.databinding.FragmentAddAlarmBinding
import com.example.smartalarm.ui.activities.MainActivity

class AddAlarmFragment : Fragment() {

    private lateinit var viewModel: AddAlarmFragmentViewModel
    lateinit var binding: FragmentAddAlarmBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[AddAlarmFragmentViewModel::class.java]
        binding = FragmentAddAlarmBinding.inflate(inflater, container, false)

        binding.saveButton.setOnClickListener {
            activity.let {
                (it as MainActivity).setCurrentFragment(AlarmsFragment())
            }
        }

        return binding.root
    }
}