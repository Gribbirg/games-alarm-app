package com.example.smartalarm.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartalarm.R
import com.example.smartalarm.databinding.FragmentProfileBinding
import com.example.smartalarm.databinding.FragmentRecordsBinding
import com.example.smartalarm.ui.adapters.GameAdapter
import com.example.smartalarm.ui.adapters.MyRecordsAdapter
import com.example.smartalarm.ui.viewmodels.RecordsFragmentViewModel


class RecordsFragment : Fragment() {

    lateinit var binding: FragmentRecordsBinding
    lateinit var viewModel: RecordsFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[RecordsFragmentViewModel::class.java]

        binding.typeSelectionButtons.check(R.id.gamesButton)
        binding.fromSelectionButtons.check(R.id.myButton)

        viewModel.myRecordsData.observe(viewLifecycleOwner) {
            binding.recordsRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = MyRecordsAdapter(it)
            }
        }

        viewModel.getRecordsFromDb(true)
        return binding.root
    }
}