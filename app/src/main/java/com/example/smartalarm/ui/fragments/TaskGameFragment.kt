package com.example.smartalarm.ui.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.smartalarm.R
import com.example.smartalarm.ui.viewmodels.TaskGameViewModel

class TaskGameFragment : Fragment() {

    companion object {
        fun newInstance() = TaskGameFragment()
    }

    private lateinit var viewModel: TaskGameViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_game, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TaskGameViewModel::class.java)
        // TODO: Use the ViewModel
    }

}