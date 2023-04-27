package com.example.smartalarm.ui.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.smartalarm.R
import com.example.smartalarm.ui.viewmodels.AddAlarmFragmentViewModel
import com.example.smartalarm.databinding.FragmentAddAlarmBinding
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

        binding.addAlarmDaysToggleGroup.check(
            when (arguments?.getInt("currentDayNumber")!!) {
                0 -> R.id.addAlarmMondayButton
                1 -> R.id.addAlarmTuesdayButton
                2 -> R.id.addAlarmWednesdayButton
                3 -> R.id.addAlarmThursdayButton
                4 -> R.id.addAlarmFridayButton
                5 -> R.id.addAlarmSaturdayButton
                6 -> R.id.addAlarmSundayButton
                else -> 0
            }
        )

        binding.addAlarmDaysToggleGroup.addOnButtonCheckedListener { _, _, _ ->
            setInfoText()
        }


        binding.addAlarmSaveButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.insertAlarmToDb(
                    binding.addAlarmTimePicker.hour,
                    binding.addAlarmTimePicker.minute,
                    when (binding.addAlarmDaysToggleGroup.checkedButtonId) {
                        R.id.addAlarmMondayButton -> 0
                        R.id.addAlarmTuesdayButton -> 1
                        R.id.addAlarmWednesdayButton -> 2
                        R.id.addAlarmThursdayButton -> 3
                        R.id.addAlarmFridayButton -> 4
                        R.id.addAlarmSaturdayButton -> 5
                        R.id.addAlarmSundayButton -> 6
                        else -> -1
                    },
                    binding.addAlarmAlarmNameText.text.toString(),
                    binding.addAlarmSetBuzzSwitch.isChecked,
                    binding.addAlarmGraduallyIncreaseVolumeSwitch.isChecked,
                    if (binding.addAlarmMakeRepetitiveSwitch.isChecked)
                        null
                    else
                        "0.0.0"
                )
                onResume()
            }
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_addAlarmFragment_to_alarmsFragment2)
        }

        setInfoText()

        return binding.root
    }

    fun setInfoText() {
        val buttonNum = when (binding.addAlarmDaysToggleGroup.checkedButtonId) {
            R.id.addAlarmMondayButton -> 0
            R.id.addAlarmTuesdayButton -> 1
            R.id.addAlarmWednesdayButton -> 2
            R.id.addAlarmThursdayButton -> 3
            R.id.addAlarmFridayButton -> 4
            R.id.addAlarmSaturdayButton -> 5
            R.id.addAlarmSundayButton -> 6
            else -> -1
        }
        binding.addAlarmChosenAlarmDateText.text =
            "Будильник на ${
                arguments?.getStringArrayList("infoCurrentDayOfWeek")?.get(buttonNum)
            } , ${arguments?.getStringArrayList("infoCurrentDay")?.get(buttonNum)}"
    }
}