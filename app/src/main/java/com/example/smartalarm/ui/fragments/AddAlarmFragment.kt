package com.example.smartalarm.ui.fragments

import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.example.smartalarm.R
import com.example.smartalarm.data.db.AlarmSimpleData
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
            getDayOfWeekButtonByNum(requireArguments().getIntegerArrayList("currentDay")!![0])
        )

        lifecycleScope.launch {
            if (!arguments?.getBoolean("isNew")!!) {
                whenStarted {
                    viewModel.currentAlarm = viewModel.getAlarm(arguments?.getLong("alarmId")!!)
                    viewModel.gamesList = viewModel.currentAlarm!!.gamesList
                    setStateFromAlarm(viewModel.currentAlarm!!.alarmSimpleData)
                }
            }
            with(arguments?.getIntegerArrayList("games")) {
                if (this != null)
                    viewModel.gamesList = this
            }
        }

        with(requireArguments().getStringArrayList("state")) {
            if (this != null) {
                setStateFromAlarm(AlarmSimpleData(this))
            }
        }


        binding.addAlarmSaveButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.insertOrUpdateAlarmToDb(
                    getAlarmFromState()
                )
                onResume()
            }
            navToAlarmFragment(getNumOfCheckedButton())
        }

        binding.addAlarmGamesButton.setOnClickListener {
            val bundle = requireArguments()
            bundle.putIntegerArrayList("games", viewModel.gamesList)
            bundle.putStringArrayList("state", getAlarmFromState().toStringArray())

            Navigation
                .findNavController(binding.root)
                .navigate(
                    R.id.action_addAlarmFragment_to_gameChoiceFragment,
                    bundle,
                    NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                )
        }

        binding.addAlarmDaysToggleGroup.addOnButtonCheckedListener { _, _, _ ->
            setInfoText()
        }

        binding.addAlarmChooseMelodyButton.setOnClickListener {
            selectLauncher.launch(arrayOf("audio/mpeg"))
        }

        setInfoText()

        return binding.root
    }

    private fun getDayOfWeekButtonByNum(num: Int): Int =
        when (num) {
            0 -> R.id.addAlarmMondayButton
            1 -> R.id.addAlarmTuesdayButton
            2 -> R.id.addAlarmWednesdayButton
            3 -> R.id.addAlarmThursdayButton
            4 -> R.id.addAlarmFridayButton
            5 -> R.id.addAlarmSaturdayButton
            6 -> R.id.addAlarmSundayButton
            else -> 0
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
                bundle,
                NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
            )
    }

    private fun getAlarmFromState(): AlarmSimpleData {
        return AlarmSimpleData(
            timeHour = binding.addAlarmTimePicker.hour,
            timeMinute = binding.addAlarmTimePicker.minute,
            dayOfWeek = getNumOfCheckedButton(),
            name = if (binding.addAlarmAlarmNameText.text.toString() == "")
                "Будильник"
            else
                binding.addAlarmAlarmNameText.text.toString(),
            isVibration = binding.addAlarmSetBuzzSwitch.isChecked,
            isRisingVolume = binding.addAlarmGraduallyIncreaseVolumeSwitch.isChecked,
            activateDate = if (binding.addAlarmMakeRepetitiveSwitch.isChecked)
                null
            else
                arguments?.getStringArrayList("datesOfWeek")?.get(getNumOfCheckedButton()),
            recordScore = null,
            recordSeconds = null
        )
    }

    private fun setStateFromAlarm(alarm: AlarmSimpleData) {
        with(binding) {
            addAlarmTimePicker.hour = alarm.timeHour
            addAlarmTimePicker.minute = alarm.timeMinute
            addAlarmMakeRepetitiveSwitch.isChecked = alarm.activateDate == null
            addAlarmAlarmNameText.setText(alarm.name)
            addAlarmSetBuzzSwitch.isChecked = alarm.isVibration
            addAlarmGraduallyIncreaseVolumeSwitch.isChecked = alarm.isRisingVolume
            addAlarmDaysToggleGroup.check(getDayOfWeekButtonByNum(alarm.dayOfWeek))
        }
    }

    private val selectLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            try {
                uri?.let { selectFile(it) }
            } catch (e: Exception) {
                Log.i("selection fail", e.toString())
            }
        }

    private fun selectFile(uri: Uri) {
        val selectLauncher = context?.contentResolver?.openInputStream(uri)?.use {}
    }
}