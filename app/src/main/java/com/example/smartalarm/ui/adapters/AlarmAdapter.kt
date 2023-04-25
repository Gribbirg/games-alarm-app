package com.example.smartalarm.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartalarm.data.AlarmData
import com.example.smartalarm.data.AlarmSimpleData
import com.example.smartalarm.databinding.AlarmItemBinding

class AlarmAdapter() : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>(){

    var data = ArrayList<AlarmSimpleData>()

    fun setListData(data : ArrayList<AlarmSimpleData>) {
        this.data = data
    }

    class AlarmViewHolder(val binding: AlarmItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = AlarmItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val currencyAlarmData = data[position]

        with (holder.binding) {
            alarmTimeTextView.text = "${currencyAlarmData.timeHour}:${currencyAlarmData.timeMinute}"
            alarmNameTextView.text = currencyAlarmData.name
            alarmOnOffSwitch.isChecked = true
            recordTextView.text = "${currencyAlarmData.recordMinutes}:${currencyAlarmData.recordSeconds}"
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}