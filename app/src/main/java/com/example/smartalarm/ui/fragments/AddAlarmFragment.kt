package com.example.smartalarm.ui.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.navigation.Navigation
import com.example.smartalarm.R
import com.example.smartalarm.ui.viewmodels.AddAlarmFragmentViewModel
import com.example.smartalarm.databinding.FragmentAddAlarmBinding
import kotlinx.coroutines.launch

class AddAlarmFragment : Fragment() {

    private lateinit var viewModel: AddAlarmFragmentViewModel
    lateinit var binding: FragmentAddAlarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navToAlarmFragment()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[AddAlarmFragmentViewModel::class.java]
        binding = FragmentAddAlarmBinding.inflate(inflater, container, false)

        binding.addAlarmDaysToggleGroup.check(
            when (arguments?.getIntegerArrayList("currentDay")!![0]) {
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

        if (!arguments?.getBoolean("isNew")!!) {
            lifecycleScope.launch {
                whenStarted {
                    viewModel.currentAlarm = viewModel.getAlarm(arguments?.getLong("alarmId")!!)
                    with(binding) {
                        addAlarmTimePicker.hour = viewModel.currentAlarm!!.timeHour
                        addAlarmTimePicker.minute = viewModel.currentAlarm!!.timeMinute
                        addAlarmMakeRepetitiveSwitch.isChecked =
                            viewModel.currentAlarm!!.activateDate == null
                        addAlarmAlarmNameText.setText(viewModel.currentAlarm!!.name)
                        addAlarmSetBuzzSwitch.isChecked = viewModel.currentAlarm!!.isVibration
                        addAlarmGraduallyIncreaseVolumeSwitch.isChecked =
                            viewModel.currentAlarm!!.isRisingVolume
                    }
                }
            }
        }

        binding.addAlarmSaveButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.insertOrUpdateAlarmToDb(
                    binding.addAlarmTimePicker.hour,
                    binding.addAlarmTimePicker.minute,
                    getNumOfCheckedButton(),
                    binding.addAlarmAlarmNameText.text.toString(),
                    binding.addAlarmSetBuzzSwitch.isChecked,
                    binding.addAlarmGraduallyIncreaseVolumeSwitch.isChecked,
                    if (binding.addAlarmMakeRepetitiveSwitch.isChecked)
                        null
                    else
                        arguments?.getStringArrayList("datesOfWeek")?.get(getNumOfCheckedButton())
                )
                onResume()
            }
            navToAlarmFragment(getNumOfCheckedButton())
        }

        setInfoText()

        return binding.root
    }

    private fun setInfoText() {
        val buttonNum = getNumOfCheckedButton()
        binding.addAlarmChosenAlarmDateText.text =
            "Будильник на ${
                arguments?.getStringArrayList("infoCurrentDayOfWeek")?.get(buttonNum)
            } , ${arguments?.getStringArrayList("infoCurrentDay")?.get(buttonNum)}"
    }

    private fun getNumOfCheckedButton() = when (binding.addAlarmDaysToggleGroup.checkedButtonId) {
        R.id.addAlarmMondayButton -> 0
        R.id.addAlarmTuesdayButton -> 1
        R.id.addAlarmWednesdayButton -> 2
        R.id.addAlarmThursdayButton -> 3
        R.id.addAlarmFridayButton -> 4
        R.id.addAlarmSaturdayButton -> 5
        R.id.addAlarmSundayButton -> 6
        else -> -1
    }

    private fun navToAlarmFragment(dayOfWeek: Int = arguments?.getIntegerArrayList("currentDay")!![0]) {
        val bundle = Bundle()
        bundle.putIntegerArrayList(
            "currentDay", arrayListOf(
                dayOfWeek,
                arguments?.getIntegerArrayList("currentDay")!![1],
                arguments?.getIntegerArrayList("currentDay")!![2],
            )
        )

        Navigation.findNavController(binding.root)
            .navigate(
                R.id.action_addAlarmFragment_to_alarmsFragment2,
                bundle
            )
    }
}