package com.example.smartalarm.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartalarm.R
import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.data.receivers.AlarmReceiver
import com.example.smartalarm.databinding.FragmentAlarmsBinding
import com.example.smartalarm.ui.adapters.AlarmAdapter
import com.example.smartalarm.ui.viewmodels.AlarmsFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AlarmsFragment : Fragment(), AlarmAdapter.OnAlarmClickListener {

    private lateinit var dateViewList: ArrayList<DateView>
    private lateinit var viewModel: AlarmsFragmentViewModel
    private lateinit var binding: FragmentAlarmsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlarmsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[AlarmsFragmentViewModel::class.java]

        with(arguments?.getIntegerArrayList("currentDay")) {
            if (this != null) {
                viewModel.setDate(this)
            } else {
                viewModel.setWeekData()
            }
        }

        lifecycleScope.launch {
            viewModel.getAlarmsFromDbByDayOfWeek(1)
        }

        dateViewList = ArrayList()

        with(dateViewList) {
            add(DateView(binding.monLayout, binding.monTextView, binding.monLegTextView))
            add(DateView(binding.tueLayout, binding.tueTextView, binding.tueLegTextView))
            add(DateView(binding.wedLayout, binding.wedTextView, binding.wedLegTextView))
            add(DateView(binding.thurLayout, binding.thurTextView, binding.thurLegTextView))
            add(DateView(binding.friLayout, binding.friTextView, binding.friLegTextView))
            add(DateView(binding.satLayout, binding.satTextView, binding.satLegTextView))
            add(DateView(binding.sunLayout, binding.sunTextView, binding.sunLegTextView))
        }

        for (i in 0..6)
            dateViewList[i].layout.setOnClickListener {
                viewModel.currentDayOfWeek = i
                setDay()
                setRecyclerData()
            }

        binding.nextWeekButton.setOnClickListener {
            viewModel.changeWeek(1)
            setDaysNumAndMonth()
            getEarliestAlarms()
        }
        binding.previousWeekButton.setOnClickListener {
            viewModel.changeWeek(-1)
            setDaysNumAndMonth()
            getEarliestAlarms()
        }


        binding.addAlarmButton.setOnClickListener {
            if (viewModel.currentDayOfWeek != null)
                navToAddAlarmFragment(viewModel.addInfoInformationToBundle(null))
            else
                Toast.makeText(context, "Выберите день", Toast.LENGTH_LONG).show()
        }


        viewModel.earliestAlarmsList.observe(viewLifecycleOwner) {
            val timesString = viewModel.timesToString(it)
            with(binding) {
                monFirstAlarmTextView.text = timesString[0]
                tueFirstAlarmTextView.text = timesString[1]
                wedFirstAlarmTextView.text = timesString[2]
                thurFirstAlarmTextView.text = timesString[3]
                friFirstAlarmTextView.text = timesString[4]
                satFirstAlarmTextView.text = timesString[5]
                sunFirstAlarmTextView.text = timesString[6]
            }
        }

        viewModel.alarmsList.observe(viewLifecycleOwner) {
            binding.alarmsRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = AlarmAdapter(it, this@AlarmsFragment)
            }
            setNoAlarmsViewsVisibility(it.isEmpty())
        }

        getEarliestAlarms()
        setDaysNumAndMonth()
        setDay()
        setRecyclerData()
        return binding.root
    }

    private fun setDaysNumAndMonth() {
        for (i in 0..6) {
            dateViewList[i].numTextView.text =
                viewModel.weekCalendarData.daysList[i].dayNumber.toString()
            with(viewModel.weekCalendarData.daysList[i]) {
                if (today)
                    dateViewList[i].setTextsColor(Color.parseColor("#0000FF"))
                else if (isWeekend)
                    dateViewList[i].setTextsColor(Color.parseColor("#FF0000"))
                else if (isHoliday)
                    dateViewList[i].setTextsColor(Color.parseColor("#C00000"))
                else
                    dateViewList[i].setTextsColor(Color.parseColor("#525252"))
            }
        }
        setMonth()
        setDay()
        setRecyclerData()
    }

    private fun setMonth() {
        val listOfMonth = viewModel.weekCalendarData.monthList

        binding.monthStartTextView.text = listOfMonth[0]
        binding.monthTextView.text = listOfMonth[1]
        binding.monthENDTextView.text = listOfMonth[2]
    }


    private fun setDay() {
        for (i in 0..6) {
            dateViewList[i].layout.setBackgroundResource(R.drawable.rounded_corners_grey)
        }
        if (viewModel.currentDayOfWeek != null)
            dateViewList[viewModel.currentDayOfWeek!!].layout.setBackgroundResource(R.drawable.rounded_corners_green)
        binding.infoTextView.text = viewModel.getInfoLine()
    }

    private fun setRecyclerData() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getAlarmsFromDbByDayOfWeek(viewModel.currentDayOfWeek)
        }
    }

    private fun setNoAlarmsViewsVisibility(isVisible: Boolean) {
        if (isVisible) {
            binding.noAlarmsImageView.visibility = View.VISIBLE
            binding.noAlarmsTextView.visibility = View.VISIBLE
        } else {
            binding.noAlarmsImageView.visibility = View.INVISIBLE
            binding.noAlarmsTextView.visibility = View.INVISIBLE
        }
    }

    data class DateView(
        val layout: LinearLayout,
        val numTextView: TextView,
        val dayTextView: TextView
    ) {
        fun setTextsColor(color: Int) {
            numTextView.setTextColor(color)
            dayTextView.setTextColor(color)
        }
    }

    private fun navToAddAlarmFragment(bundle: Bundle?) {
        if (bundle == null)
            Navigation.findNavController(binding.root).navigate(
                R.id.action_alarmsFragment_to_addAlarmFragment,
            )
        else
            Navigation.findNavController(binding.root).navigate(
                R.id.action_alarmsFragment_to_addAlarmFragment,
                bundle
            )
    }

    private fun getEarliestAlarms() {
        lifecycleScope.launch {
            viewModel.getEarliestAlarmsForAllWeek()
        }
    }

    override fun onOnOffSwitchClickListener(alarm: AlarmSimpleData) {
        lifecycleScope.launch {
            viewModel.setAlarmStateInDb(alarm)
            getEarliestAlarms()
        }
    }

    override fun openEditMenu(alarm: AlarmSimpleData) {
        navToAddAlarmFragment(viewModel.addInfoInformationToBundle(null, alarm.id))
    }

    override fun deleteAlarm(alarm: AlarmSimpleData) {
        lifecycleScope.launchWhenStarted {
            viewModel.deleteAlarmFromDb(alarm)
            setRecyclerData()
            getEarliestAlarms()
        }
    }
}

