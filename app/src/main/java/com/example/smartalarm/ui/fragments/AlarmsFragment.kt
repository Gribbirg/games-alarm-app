package com.example.smartalarm.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartalarm.R
import com.example.smartalarm.databinding.FragmentAlarmsBinding
import com.example.smartalarm.ui.activities.MainActivity
import com.example.smartalarm.ui.adapters.AlarmAdapter
import com.example.smartalarm.ui.viewmodels.AlarmsFragmentViewModel
import kotlinx.coroutines.launch


class AlarmsFragment : Fragment() {

    private lateinit var textViewList: ArrayList<TextView>
    private lateinit var viewModel: AlarmsFragmentViewModel
    private lateinit var binding: FragmentAlarmsBinding
    private var currentDayNumber: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlarmsBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[AlarmsFragmentViewModel::class.java]

        textViewList = ArrayList()
        with(textViewList) {
            add(binding.monTextView)
            add(binding.tueTextView)
            add(binding.wenTextView)
            add(binding.thuTextView)
            add(binding.friTextView)
            add(binding.satTextView)
            add(binding.sunTextView)
        }

        for (i in 0..6)
            textViewList[i].setOnClickListener {
                currentDayNumber = i
                setDay()
            }

        setDaysNumAndMonth()
        currentDayNumber = viewModel.getTodayNumInWeek() - 1
        setDay()

        binding.nextMonthButton.setOnClickListener {
            viewModel.changeWeek(1)
            setDaysNumAndMonth()
        }
        binding.previousMonthButton.setOnClickListener {
            viewModel.changeWeek(-1)
            setDaysNumAndMonth()
        }


        binding.addAlarmButton.setOnClickListener {
            if (currentDayNumber != null) {
                Navigation.findNavController(binding.root).navigate(R.id.action_alarmsFragment_to_addAlarmFragment)
            } else
                Toast.makeText(context, "Выберите день", Toast.LENGTH_LONG).show()
        }

        viewModel.alarmsList.observe(viewLifecycleOwner) {
            binding.alarmsRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = AlarmAdapter(it)
            }
        }

        lifecycleScope.launch {
            viewModel.getAlarmsFromDb()
            onResume()
        }

        return binding.root
    }

    private fun setDaysNumAndMonth() {
        for (i in 0..6) {
            textViewList[i].text =
                viewModel.weekCalendarData.daysList[i].dayNumber.toString()
        }
        currentDayNumber = null
        setMonth()
        setDay()
    }

    private fun setMonth() {
        val listOfMonth = viewModel.weekCalendarData.monthList

        binding.monthStartTextView.text = listOfMonth[0]
        binding.monthTextView.text = listOfMonth[1]
        binding.monthENDTextView.text = listOfMonth[2]
    }

    private fun setDay() {
        for (i in 0..6) {
            with(viewModel.weekCalendarData.daysList[i]) {
                if (today)
                    textViewList[i].setBackgroundResource(R.drawable.text_view_circle_blue)
                else if (isWeekend)
                    textViewList[i].setBackgroundResource(R.drawable.text_view_circle_red)
                else
                    textViewList[i].setBackgroundResource(R.drawable.text_view_circle)
            }
        }
        if (currentDayNumber != null)
            textViewList[currentDayNumber!!].setBackgroundResource(R.drawable.text_view_circle_pressed)
    }
}
