package com.example.smartalarm.ui.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartalarm.R
import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.databinding.FragmentAlarmsBinding
import com.example.smartalarm.ui.adapters.AlarmAdapter
import com.example.smartalarm.ui.viewmodels.AlarmsFragmentViewModel
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AlarmsFragment : Fragment(), AlarmAdapter.OnAlarmClickListener {

    private lateinit var dateViewList: ArrayList<DateView>
    private lateinit var viewModel: AlarmsFragmentViewModel
    private lateinit var binding: FragmentAlarmsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                }
            })
    }

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
            add(
                DateView(
                    binding.monLayout,
                    binding.monTextView,
                    binding.monLegTextView,
                    binding.monFirstAlarmTextView
                )
            )
            add(
                DateView(
                    binding.tueLayout,
                    binding.tueTextView,
                    binding.tueLegTextView,
                    binding.tueFirstAlarmTextView
                )
            )
            add(
                DateView(
                    binding.wedLayout,
                    binding.wedTextView,
                    binding.wedLegTextView,
                    binding.wedFirstAlarmTextView
                )
            )
            add(
                DateView(
                    binding.thurLayout,
                    binding.thurTextView,
                    binding.thurLegTextView,
                    binding.thurFirstAlarmTextView
                )
            )
            add(
                DateView(
                    binding.friLayout,
                    binding.friTextView,
                    binding.friLegTextView,
                    binding.friFirstAlarmTextView
                )
            )
            add(
                DateView(
                    binding.satLayout,
                    binding.satTextView,
                    binding.satLegTextView,
                    binding.satFirstAlarmTextView
                )
            )
            add(
                DateView(
                    binding.sunLayout,
                    binding.sunTextView,
                    binding.sunLegTextView,
                    binding.sunFirstAlarmTextView
                )
            )
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
                    dateViewList[i].setTextsColor(
                        MaterialColors.getColor(
                            requireContext(),
                            com.google.android.material.R.attr.colorSecondary,
                            Color.BLACK
                        )
                    )
                else if (isWeekend)
                    dateViewList[i].setTextsColor(
                        MaterialColors.getColor(
                            requireContext(),
                            com.google.android.material.R.attr.colorTertiary,
                            Color.BLACK
                        )
                    )
                else if (isHoliday)
                    dateViewList[i].setTextsColor(
                        MaterialColors.getColor(
                            requireContext(),
                            com.google.android.material.R.attr.colorOnErrorContainer,
                            Color.BLACK
                        )
                    )
                else
                    dateViewList[i].setTextsColor(
                        MaterialColors.getColor(
                            requireContext(),
                            com.google.android.material.R.attr.colorOnPrimaryContainer,
                            Color.BLACK
                        )
                    )
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
            dateViewList[i].layout.setBackgroundResource(0)
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
        val dayTextView: TextView,
        val alarmTextView: TextView
    ) {
        fun setTextsColor(color: Int) {
            numTextView.setTextColor(color)
            dayTextView.setTextColor(color)
            alarmTextView.setTextColor(color)
        }
    }

    private fun navToAddAlarmFragment(bundle: Bundle?) {
        if (bundle == null)
            Navigation.findNavController(binding.root).navigate(
                R.id.action_alarmsFragment_to_addAlarmFragment,
                null,
                NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
            )
        else
            Navigation.findNavController(binding.root).navigate(
                R.id.action_alarmsFragment_to_addAlarmFragment,
                bundle,
                NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
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

    override fun getColor(on: Boolean, regular: Boolean): Int =
        if (on)
            if (regular)
                MaterialColors.getColor(
                    requireContext(),
                    com.google.android.material.R.attr.colorSurface,
                    Color.BLACK
                )
            else
                MaterialColors.getColor(
                    requireContext(),
                    com.google.android.material.R.attr.colorSurfaceContainer,
                    Color.BLACK
                )
        else
            MaterialColors.getColor(
                requireContext(),
                com.google.android.material.R.attr.colorSurfaceVariant,
                Color.BLACK
            )

    override fun getOnViewColor(on: Boolean): Int =
        if (on)
            MaterialColors.getColor(
                requireContext(),
                com.google.android.material.R.attr.colorOnSurface,
                Color.BLACK
            )
        else
            MaterialColors.getColor(
                requireContext(),
                com.google.android.material.R.attr.colorOnSurfaceVariant,
                Color.BLACK
            )
}

