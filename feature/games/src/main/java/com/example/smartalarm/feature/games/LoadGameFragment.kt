package com.example.smartalarm.feature.games

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.smartalarm.feature.games.R
import com.example.smartalarm.feature.games.databinding.FragmentLoadGameBinding
import com.example.smartalarm.core.alarm.AlarmIntentKeys
import com.example.smartalarm.core.alarm.AlarmMediaPlayer
import com.example.smartalarm.core.alarm.AlarmVibrator
import com.example.smartalarm.feature.games.LoadGameViewModel

class LoadGameFragment : Fragment() {

    private lateinit var binding: FragmentLoadGameBinding
    private lateinit var viewModel: LoadGameViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoadGameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[LoadGameViewModel::class.java]

        AlarmVibrator.stop()
        AlarmMediaPlayer.stopAudio()

        val bundle = Bundle()

        viewModel.path.observe(viewLifecycleOwner) {
            bundle.putString("music path", it)
        }

        viewModel.currentGame.observe(viewLifecycleOwner) {
            val navController = Navigation.findNavController(binding.root)

            bundle.putLong("alarm id", requireActivity().intent.getLongExtra(AlarmIntentKeys.ALARM_ID, -1))
            bundle.putBoolean("test", false)

            if (it.isEmpty()) {
                navController.navigate(R.id.action_loadGameFragment_to_gameResultFragment, bundle)
            } else {
                bundle.putInt("difficulty", it[1])
                when (it[0]) {
                    1 -> navController.navigate(
                        R.id.action_loadGameFragment_to_calcGameFragment,
                        bundle
                    )

                    2 -> navController.navigate(
                        R.id.action_loadGameFragment_to_memoryGameFragment,
                        bundle
                    )

                    3 -> navController.navigate(
                        R.id.action_loadGameFragment_to_equationGameFragment,
                        bundle
                    )

                    4 -> navController.navigate(
                        R.id.action_loadGameFragment_to_sortingGameFragment,
                        bundle
                    )

                    5 -> navController.navigate(
                        R.id.action_loadGameFragment_to_pairsGameFragment,
                        bundle
                    )

                    6 -> navController.navigate(
                        R.id.action_loadGameFragment_to_sequenceGameFragment,
                        bundle
                    )

                    7 -> navController.navigate(
                        R.id.action_loadGameFragment_to_stroopGameFragment,
                        bundle
                    )

                    8 -> navController.navigate(
                        R.id.action_loadGameFragment_to_oddoneoutGameFragment,
                        bundle
                    )

                    9 -> navController.navigate(
                        R.id.action_loadGameFragment_to_mazeGameFragment,
                        bundle
                    )

                    10 -> navController.navigate(
                        R.id.action_loadGameFragment_to_anagramGameFragment,
                        bundle
                    )

                    11 -> navController.navigate(
                        R.id.action_loadGameFragment_to_truefalseGameFragment,
                        bundle
                    )
                }
            }
        }

        bundle.putLong("start time", requireActivity().intent.getLongExtra(AlarmIntentKeys.START_TIME, 0L))

        viewModel.getAlarm(requireActivity().intent.getLongExtra(AlarmIntentKeys.ALARM_ID, -1))

        return binding.root
    }

}