package com.example.smartalarm.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.repositories.AlarmDbRepository
import com.example.smartalarm.databinding.AlarmItemBinding

class AlarmAdapter(var data: ArrayList<AlarmSimpleData>, val listener: OnAlarmClickListener) :
    RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(val binding: AlarmItemBinding, val listener: OnAlarmClickListener) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = AlarmItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlarmViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val currencyAlarmData = data[position]

        with(holder.binding) {
            alarmTimeTextView.text = if (currencyAlarmData.timeMinute >= 10)
                "${currencyAlarmData.timeHour}:${currencyAlarmData.timeMinute}"
            else
                "${currencyAlarmData.timeHour}:0${currencyAlarmData.timeMinute}"
            alarmNameTextView.text = currencyAlarmData.name

            if (currencyAlarmData.recordSeconds != null)
                recordTextView.text = "${currencyAlarmData.recordSeconds!!.toInt() / 60}:" +
                        "${currencyAlarmData.recordSeconds!!.toInt() % 60}"
            else
                recordTextView.text = "Нет данных"

            alarmOnOffSwitch.isChecked = currencyAlarmData.isOn
            alarmOnOffSwitch.setOnClickListener {
                currencyAlarmData.isOn = alarmOnOffSwitch.isChecked
                holder.listener.onOnOffSwitchClickListener(currencyAlarmData)
            }

            menuButton.setOnClickListener {
                holder.listener.showPopMenu(currencyAlarmData)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    interface OnAlarmClickListener {
        fun onOnOffSwitchClickListener(alarm: AlarmSimpleData)
        fun showPopMenu(alarm: AlarmSimpleData)
    }
}