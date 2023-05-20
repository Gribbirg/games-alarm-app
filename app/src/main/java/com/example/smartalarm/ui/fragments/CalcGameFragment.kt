package com.example.smartalarm.ui.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.smartalarm.R
import com.example.smartalarm.ui.viewmodels.CalcGameViewModel

class CalcGameFragment : Fragment() {

    companion object {
        fun newInstance() = CalcGameFragment()
    }

    private lateinit var viewModel: CalcGameViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calc_game, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CalcGameViewModel::class.java)
        // TODO: Use the ViewModel
    }

}