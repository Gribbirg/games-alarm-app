package com.example.smartalarm.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartalarm.R
import com.example.smartalarm.databinding.FragmentAlarmsBinding
import com.example.smartalarm.ui.activities.MainActivity
import com.example.smartalarm.ui.adapters.AlarmAdapter
import com.example.smartalarm.ui.viewmodels.AlarmsFragmentViewModel


class AlarmsFragment : Fragment() {

    lateinit var textViewList: ArrayList<TextView>
    lateinit var viewModel : AlarmsFragmentViewModel
    lateinit var binding: FragmentAlarmsBinding
    lateinit var recyclerViewAdapter: AlarmAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlarmsBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[AlarmsFragmentViewModel::class.java]

        textViewList = ArrayList()
        with (textViewList) {
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
                setDay(i)
            }

        setDaysNumAndMonth()
        setDay(viewModel.getTodayNumInWeek() - 1)

        binding.nextMonthButton.setOnClickListener{
            viewModel.changeWeek(1)
            setDaysNumAndMonth()
        }
        binding.previousMonthButton.setOnClickListener{
            viewModel.changeWeek(-1)
            setDaysNumAndMonth()
        }


        with(binding.alarmsRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = AlarmAdapter()
        }


        binding.addAlarmButton.setOnClickListener {
            activity.let {
                (it as MainActivity).setCurrentFragment(AddAlarmFragment())
            }
        }

        return binding.root
    }

    private fun setDaysNumAndMonth() {
        for (i in 0..6) {
            textViewList[i].text =
                viewModel.weekCalendarData.daysList[i].dayNumber.toString()
        }
        setMonth()
        setDay(-1)
    }

    private fun setMonth() {
        val listOfMonth = viewModel.weekCalendarData.monthList

        binding.monthStartTextView.text = listOfMonth[0]
        binding.monthTextView.text = listOfMonth[1]
        binding.monthENDTextView.text = listOfMonth[2]
    }

    private fun setDay(numOfDayOfWeek : Int) {
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
        if (numOfDayOfWeek != -1)
            textViewList[numOfDayOfWeek].setBackgroundResource(R.drawable.text_view_circle_pressed)
    }
}
