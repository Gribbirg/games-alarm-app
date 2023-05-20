package com.example.smartalarm.ui.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.smartalarm.R
import com.example.smartalarm.databinding.FragmentGameResultBinding
import com.example.smartalarm.ui.viewmodels.GameResultViewModel

class GameResultFragment : Fragment() {

    private lateinit var binding: FragmentGameResultBinding
    private lateinit var viewModel: GameResultViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGameResultBinding.inflate(inflater, container, false)

        return inflater.inflate(R.layout.fragment_game_result, container, false)
    }

}