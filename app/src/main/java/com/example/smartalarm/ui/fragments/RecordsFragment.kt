package com.example.smartalarm.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartalarm.R
import com.example.smartalarm.data.db.GameData
import com.example.smartalarm.databinding.FragmentProfileBinding
import com.example.smartalarm.databinding.FragmentRecordsBinding
import com.example.smartalarm.ui.adapters.AllRecordsAdapter
import com.example.smartalarm.ui.adapters.GameAdapter
import com.example.smartalarm.ui.adapters.MyRecordsAdapter
import com.example.smartalarm.ui.viewmodels.RecordsFragmentViewModel


class RecordsFragment : Fragment(), MyRecordsAdapter.OnMyRecordClickListener {

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

        binding.typeSelectionButtons.addOnButtonCheckedListener { _, _, _ ->
            setupChange()
        }

        binding.fromSelectionButtons.addOnButtonCheckedListener { _, _, _ ->
            setupChange()
        }

        viewModel.myRecordsData.observe(viewLifecycleOwner) {
            binding.recordsRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = MyRecordsAdapter(it, this@RecordsFragment)
            }
        }

        viewModel.allRecordsData.observe(viewLifecycleOwner) {
            binding.allRecordsRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = AllRecordsAdapter(it)
            }
        }

        viewModel.getRecordsFromDb(0)
        setupChange()
        return binding.root
    }

    private fun getNumOfButtonById(): Int {
        var res = 0
        if (binding.typeSelectionButtons.checkedButtonId == R.id.lastsButton)
            res++
        if (binding.fromSelectionButtons.checkedButtonId == R.id.allButton)
            res += 2
        return res
    }

    private fun setupChange() {
        with(getNumOfButtonById()) {
            viewModel.getRecordsFromDb(this)
            if (this < 2) {
                binding.allRecordsRecyclerView.visibility = View.GONE
                binding.recordsRecyclerView.visibility = View.VISIBLE
            } else {
                binding.recordsRecyclerView.visibility = View.GONE
                binding.allRecordsRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    override fun onShareClickListener(gameData: GameData) {
        if (!viewModel.shareRecord(gameData))
            Toast.makeText(context, "Войдите в аккаунт!", Toast.LENGTH_LONG).show()
    }
}