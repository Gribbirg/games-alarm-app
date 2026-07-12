package com.example.smartalarm.feature.games.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.smartalarm.feature.games.demo.databinding.FragmentDemoResultBinding

/**
 * Экран результата демо-приложения: показывает очки и время, которые игра
 * кладёт в аргументы («score», «time»), вместо полноценного экрана
 * результата основного приложения (запись рекордов здесь не нужна).
 */
class DemoResultFragment : Fragment() {

    private lateinit var binding: FragmentDemoResultBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDemoResultBinding.inflate(inflater, container, false)

        binding.scoreTextView.text =
            getString(R.string.demo_result_score, requireArguments().getInt("score"))
        binding.timeTextView.text =
            getString(R.string.demo_result_time, requireArguments().getString("time"))

        binding.backToMenuButton.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_demoResultFragment_to_demoLauncherFragment)
        }

        return binding.root
    }
}
