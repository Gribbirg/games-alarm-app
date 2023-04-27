package com.example.smartalarm.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.smartalarm.data.AlarmSimpleData
import com.example.smartalarm.databinding.AlarmItemBinding

class AlarmAdapter(var data: ArrayList<AlarmSimpleData>) :
    RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

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

        with(holder.binding) {
            alarmTimeTextView.text = "${currencyAlarmData.timeHour}:${currencyAlarmData.timeMinute}"
            alarmNameTextView.text = currencyAlarmData.name
            alarmOnOffSwitch.isChecked = currencyAlarmData.isOn
            if (currencyAlarmData.recordSeconds != null)
                recordTextView.text = "${currencyAlarmData.recordSeconds!!.toInt() / 60}:" +
                        "${currencyAlarmData.recordSeconds!!.toInt() % 60}"
            else
                recordTextView.text = "Нет данных"
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}