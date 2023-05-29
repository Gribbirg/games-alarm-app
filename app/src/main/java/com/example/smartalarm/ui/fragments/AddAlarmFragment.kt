package com.example.smartalarm.ui.fragments

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.example.smartalarm.R
import com.example.smartalarm.data.utils.RealPathUtil
import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.ui.viewmodels.AddAlarmFragmentViewModel
import com.example.smartalarm.databinding.FragmentAddAlarmBinding
import kotlinx.coroutines.launch

class AddAlarmFragment : Fragment() {

    private lateinit var viewModel: AddAlarmFragmentViewModel
    lateinit var binding: FragmentAddAlarmBinding

    private var ringtonePath: String = "null"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navToAlarmFragment()
                }
            })
    }

    override fun onResume() {
        super.onResume()
        if (ringtonePath != "null") {
            binding.addAlarmChosenMelodyText.setText("Выбранная мелодия: ".plus(ringtonePath.substringAfterLast("/")))
        }
        Log.i("resume_update textView", "it resumed")
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
                    viewModel.currentAlarm = viewModel.getAlarm(arguments?.getLong("alarmId")!!)
                    viewModel.gamesList = viewModel.currentAlarm!!.gamesList
                    setStateFromAlarm(AlarmSimpleData(viewModel.currentAlarm!!))
            }
            with(arguments?.getIntegerArrayList("games")) {
                if (this != null)
                    viewModel.gamesList = this
            }
            with(requireArguments().getStringArrayList("state")) {
                if (this != null) {
                    setStateFromAlarm(AlarmSimpleData(this))
                }
            }
        }

        binding.addAlarmSaveButton.setOnClickListener {
//            ringtonePath = RealPathUtil.getRealPath(
//                requireContext(),
//                RingtoneManager.getActualDefaultRingtoneUri(
//                    context,
//                    RingtoneManager.TYPE_ALARM
//                )
//            ).toString()

            Log.i("chosen song", ringtonePath)

            if (viewModel.insertOrUpdateAlarm(getAlarmFromState()))
                navToAlarmFragment(getNumOfCheckedButton())
            else
                AlertDialog.Builder(binding.root.context)
                    .setTitle("Предупреждение")
                    .setIcon(R.drawable.baseline_warning_24)
                    .setMessage("Нельзя поставить одноразовый будильник в прошлое!")
                    .setPositiveButton("Понятно") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
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
            selectLauncher.launch(arrayOf("audio/*"))
        }

        binding.addAlarmMenuButton.setOnClickListener {
            val menu = PopupMenu(requireContext(), it)
            menu.inflate(R.menu.menu_add_alarm)

            menu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.addAlarmMenuPasteItem -> {
                        pasteAlarm()
                        true
                    }

                    else -> true
                }
            }
            try {
                val field = PopupMenu::class.java.getDeclaredField("mPopup")
                field.isAccessible = true
                val popup = field.get(menu)
                popup.javaClass
                    .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(popup, true)
            } catch (e: Exception) {
                Log.e("Menu", "Error showing menu icons", e)
            } finally {
                menu.show()
            }
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
            name = binding.addAlarmAlarmNameText.text.toString(),
            isVibration = binding.addAlarmSetBuzzSwitch.isChecked,
            isRisingVolume = binding.addAlarmGraduallyIncreaseVolumeSwitch.isChecked,
            ringtonePath = ringtonePath,
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
            ringtonePath = alarm.ringtonePath
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
        val uriPathHelper = RealPathUtil

        Log.i("chosen song before check", ringtonePath)
        ringtonePath = uriPathHelper.getRealPath(requireContext(), uri).toString()
        Log.i("chosen song after check", ringtonePath)
    }

    private fun pasteAlarm() {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val pasteIntent: Intent? = clipboard.primaryClip?.getItemAt(0)?.intent

        if (pasteIntent != null) {

            val alarmSimpleDataStringArray = pasteIntent.getStringArrayListExtra("alarm simple")
            val alarmGamesList = pasteIntent.getIntegerArrayListExtra("alarm games")

            if (alarmSimpleDataStringArray != null && alarmGamesList != null) {

                val copyAlarm = AlarmSimpleData(alarmSimpleDataStringArray)
                val currentAlarm = getAlarmFromState()

                copyAlarm.dayOfWeek = currentAlarm.dayOfWeek
                copyAlarm.activateDate = copyAlarm.activateDate

                setStateFromAlarm(copyAlarm)
                viewModel.gamesList = alarmGamesList

                Toast.makeText(requireContext(), "Будильник вставлен", Toast.LENGTH_LONG).show()

            } else {
                toastNoCopyAlarm()
            }
        } else {
            toastNoCopyAlarm()
        }
    }

    private fun toastNoCopyAlarm() {
        Toast.makeText(requireContext(), "Нет скопированного будильника", Toast.LENGTH_LONG)
            .show()
    }
}