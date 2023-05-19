package com.example.smartalarm.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.smartalarm.R
import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.repositories.AlarmDbRepository
import com.example.smartalarm.databinding.AlarmItemBinding


class AlarmAdapter(var data: ArrayList<AlarmSimpleData>, private val listener: OnAlarmClickListener) :
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

            if (currencyAlarmData.recordScore != null)
                recordTextView.text = "Лучший результат: ${currencyAlarmData.recordScore}"
            else
                recordTextView.text = "Нет данных"

            if (currencyAlarmData.activateDate != null) {
                    alarmMaterialCardView.setBackgroundColor(Color.parseColor("#e3e3e3"))
                recordTextView.visibility = View.GONE
            }

            alarmOnOffSwitch.isChecked = currencyAlarmData.isOn
            alarmOnOffSwitch.setOnClickListener {
                currencyAlarmData.isOn = alarmOnOffSwitch.isChecked
                holder.listener.onOnOffSwitchClickListener(currencyAlarmData)
            }

            if (currencyAlarmData.isVibration) vibrationImageView.visibility = View.VISIBLE
            if (currencyAlarmData.isRisingVolume) volumeUpImageView.visibility = View.VISIBLE

            menuButton.setOnClickListener {
                val menu = PopupMenu(holder.binding.root.context, it)
                menu.inflate(R.menu.menu_alarm_unit)

                menu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.alarmUnitMenuEditItem -> {
                            holder.listener.openEditMenu(currencyAlarmData)
                            true
                        }

                        R.id.alarmUnitMenuDeleteItem -> {
                            AlertDialog.Builder(holder.binding.root.context)
                                .setTitle("Удаление будильника")
                                .setIcon(R.drawable.baseline_warning_24)
                                .setMessage("Вы уверены, что хотите удалить ${currencyAlarmData.name}?")
                                .setPositiveButton("Да") { dialog, _ ->
                                    holder.listener.deleteAlarm(currencyAlarmData)
                                    Toast.makeText(
                                        holder.binding.root.context,
                                        "${currencyAlarmData.name} удалён",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    dialog.dismiss()
                                }
                                .setNegativeButton("Нет") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create()
                                .show()
                            true
                        }

                        else -> true
                    }
                }

                menu.show()
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    interface OnAlarmClickListener {
        fun onOnOffSwitchClickListener(alarm: AlarmSimpleData)
        fun openEditMenu(alarm: AlarmSimpleData)
        fun deleteAlarm(alarm: AlarmSimpleData)
    }
}