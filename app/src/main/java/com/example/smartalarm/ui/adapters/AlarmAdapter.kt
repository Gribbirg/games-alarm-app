package com.example.smartalarm.ui.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.smartalarm.R
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.databinding.AlarmItemBinding


class AlarmAdapter(
    var data: ArrayList<AlarmData>,
    private val listener: OnAlarmClickListener
) :
    RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(val binding: AlarmItemBinding, val listener: OnAlarmClickListener) :
        RecyclerView.ViewHolder(binding.root) {
        fun setState(on: Boolean, regular: Boolean) {
            with(binding) {
                val onColor = listener.getOnViewColor(on)
                alarmMaterialCardView.setCardBackgroundColor(listener.getColor(on, regular))
                alarmTimeTextView.setTextColor(onColor)
                alarmNameTextView.setTextColor(onColor)
                recordTextView.setTextColor(onColor)
                if (on) {
                    menuButton.setBackgroundResource(R.drawable.ic_baseline_more_horiz_24)
                    vibrationImageView.setBackgroundResource(R.drawable.ic_baseline_vibration_24)
                    volumeUpImageView.setBackgroundResource(R.drawable.ic_baseline_volume_up_24)
                } else {
                    menuButton.setBackgroundResource(R.drawable.ic_baseline_more_horiz_on_off_24)
                    vibrationImageView.setBackgroundResource(R.drawable.ic_baseline_vibration_on_off_24)
                    volumeUpImageView.setBackgroundResource(R.drawable.ic_baseline_volume_up_on_off_24)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = AlarmItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlarmViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = data[position]
        val alarmSimpleData = alarm.alarmSimpleData

        with(holder.binding) {
            alarmTimeTextView.text = if (alarmSimpleData.timeMinute >= 10)
                "${alarmSimpleData.timeHour}:${alarmSimpleData.timeMinute}"
            else
                "${alarmSimpleData.timeHour}:0${alarmSimpleData.timeMinute}"
            alarmNameTextView.text = alarmSimpleData.name

            if (alarmSimpleData.recordScore != null)
                recordTextView.text = "Лучший результат: ${alarmSimpleData.recordScore}"
            else
                recordTextView.text = "Нет данных"

            if (alarmSimpleData.activateDate != null) {
                recordTextView.text = "Одноразовый"
            }

            alarmOnOffSwitch.isChecked = alarmSimpleData.isOn
            holder.setState(alarmOnOffSwitch.isChecked, alarmSimpleData.activateDate == null)
            alarmOnOffSwitch.setOnClickListener {
                alarmSimpleData.isOn = alarmOnOffSwitch.isChecked
                holder.listener.onOnOffSwitchClickListener(alarm)
                holder.setState(alarmOnOffSwitch.isChecked, alarmSimpleData.activateDate == null)
            }

            if (alarmSimpleData.isVibration) vibrationImageView.visibility = View.VISIBLE
            if (alarmSimpleData.isRisingVolume) volumeUpImageView.visibility = View.VISIBLE

            menuButton.setOnClickListener {
                val menu = PopupMenu(holder.binding.root.context, it)
                menu.inflate(R.menu.menu_alarm_unit)

                menu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.alarmUnitMenuCopyItem -> {
                            holder.listener.copyAlarm(alarm)
                            true
                        }

                        R.id.alarmUnitMenuEditItem -> {
                            holder.listener.openEditMenu(alarm)
                            true
                        }

                        R.id.alarmUnitMenuDeleteItem -> {
                            AlertDialog.Builder(holder.binding.root.context)
                                .setTitle("Удаление будильника")
                                .setIcon(R.drawable.baseline_warning_24)
                                .setMessage("Вы уверены, что хотите удалить ${alarmSimpleData.name}?")
                                .setPositiveButton("Да") { dialog, _ ->
                                    holder.listener.deleteAlarm(alarm)
                                    Toast.makeText(
                                        holder.binding.root.context,
                                        "${alarmSimpleData.name} удалён",
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
        fun copyAlarm(alarm: AlarmData)
        fun onOnOffSwitchClickListener(alarm: AlarmData)
        fun openEditMenu(alarm: AlarmData)
        fun deleteAlarm(alarm: AlarmData)
        fun getColor(on: Boolean, regular: Boolean): Int
        fun getOnViewColor(on: Boolean): Int
    }
}