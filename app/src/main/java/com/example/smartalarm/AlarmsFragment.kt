package com.example.smartalarm

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Toast
import com.example.smartalarm.databinding.FragmentAlarmsBinding


class AlarmsFragment : Fragment() {

    lateinit var textViewList: ArrayList<TextView>
    lateinit var alarmsFragmentViewModel : AlarmsFragmentViewModel
    lateinit var binding: FragmentAlarmsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlarmsBinding.inflate(inflater, container, false)
        alarmsFragmentViewModel = AlarmsFragmentViewModel()

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
        setDay(alarmsFragmentViewModel.getTodayNumInWeek() - 1)

        binding.nextMonthButton.setOnClickListener{
            alarmsFragmentViewModel.changeWeek(1)
            setDaysNumAndMonth()
        }
        binding.previousMonthButton.setOnClickListener{
            alarmsFragmentViewModel.changeWeek(-1)
            setDaysNumAndMonth()
        }
        return binding.root
    }

    private fun setDaysNumAndMonth() {
        for (i in 0..6) {
            textViewList[i].text =
                alarmsFragmentViewModel.weekCalendarData.daysList[i].dayNumber.toString()
        }
        setMonth()
        setDay(-1)
    }

    private fun setMonth() {
        val listOfMonth = alarmsFragmentViewModel.weekCalendarData.monthList

        binding.monthStartTextView.text = listOfMonth[0]
        binding.monthTextView.text = listOfMonth[1]
        binding.monthENDTextView.text = listOfMonth[2]
    }

    private fun setDay(numOfDayOfWeek : Int) {
        for (i in 0..6) {
            with(alarmsFragmentViewModel.weekCalendarData.daysList[i]) {
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
