package com.example.smartalarm.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isEmpty
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartalarm.R
import com.example.smartalarm.databinding.FragmentAlarmsBinding
import com.example.smartalarm.ui.adapters.AlarmAdapter
import com.example.smartalarm.ui.viewmodels.AlarmsFragmentViewModel
import kotlinx.coroutines.launch


class AlarmsFragment : Fragment() {

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

        val bundle = Bundle()

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

        setDaysNumAndMonth()
        viewModel.updateToday()
        setDay()

        binding.nextWeekButton.setOnClickListener {
            viewModel.changeWeek(1)
            setDaysNumAndMonth()
        }
        binding.previousWeekButton.setOnClickListener {
            viewModel.changeWeek(-1)
            setDaysNumAndMonth()
        }


        binding.addAlarmButton.setOnClickListener {
            if (viewModel.currentDayOfWeek != null) {
                bundle.putInt("currentDayNumber", viewModel.currentDayOfWeek!!)
                Navigation.findNavController(binding.root).navigate(
                    R.id.action_alarmsFragment_to_addAlarmFragment,
                    bundle
                )
            } else
                Toast.makeText(context, "Выберите день", Toast.LENGTH_LONG).show()
        }

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
                else
                    dateViewList[i].setTextsColor(Color.parseColor("#525252"))
            }
        }
        viewModel.currentDayOfWeek = null
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
    }

    private fun setRecyclerData() {
        if (viewModel.currentDayOfWeek != null) {
            lifecycleScope.launch {
                viewModel.getAlarmsFromDbByDayOfWeek(viewModel.currentDayOfWeek!!)
                onResume()
            }
            viewModel.alarmsList.observe(viewLifecycleOwner) {
                binding.alarmsRecyclerView.apply {
                    layoutManager = LinearLayoutManager(activity)
                    adapter = AlarmAdapter(it)
                }
                binding.noAlarmsTextView.visibility =
                    if (it.isEmpty())
                        View.VISIBLE
                    else
                        View.INVISIBLE
            }
        } else {
            binding.alarmsRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = AlarmAdapter(ArrayList())
            }
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
}

