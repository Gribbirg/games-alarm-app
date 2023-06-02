package com.example.smartalarm.ui.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.smartalarm.R
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.databinding.AlarmItemBinding
import com.google.android.material.color.MaterialColors


class AlarmAdapter(
    var data: ArrayList<AlarmData>,
    private val listener: OnAlarmClickListener
) :
    RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(val binding: AlarmItemBinding, val listener: OnAlarmClickListener) :
        RecyclerView.ViewHolder(binding.root) {
        fun setState(on: Boolean, regular: Boolean) {
            with(binding) {
                val onColor = getOnViewColor(on)
                alarmMaterialCardView.setCardBackgroundColor(getColor(on, regular))
                alarmTimeTextView.setTextColor(
                    MaterialColors.getColor(
                        binding.root.context,
                        if (on)
                            com.google.android.material.R.attr.colorPrimary
                        else
                            com.google.android.material.R.attr.colorOnSurfaceVariant,
                        Color.BLACK
                    )
                )
                alarmNameTextView.setTextColor(onColor)
                recordTextView.setTextColor(onColor)
                gameCounterTextView.setTextColor(
                    MaterialColors.getColor(
                        binding.root.context,
                        if (on)
                            com.google.android.material.R.attr.colorTertiary
                        else
                            com.google.android.material.R.attr.colorOnSurfaceVariant,
                        Color.BLACK
                    )
                )
                gameCounterTextTextView.setTextColor(onColor)
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

        private fun getColor(on: Boolean, regular: Boolean): Int =
            if (on)
                if (regular)
                    MaterialColors.getColor(
                        binding.root.context,
                        com.google.android.material.R.attr.colorSurface,
                        Color.BLACK
                    )
                else
                    MaterialColors.getColor(
                        binding.root.context,
                        com.google.android.material.R.attr.colorSurfaceContainer,
                        Color.BLACK
                    )
            else
                MaterialColors.getColor(
                    binding.root.context,
                    com.google.android.material.R.attr.colorSurfaceVariant,
                    Color.BLACK
                )

        private fun getOnViewColor(on: Boolean): Int =
            MaterialColors.getColor(
                binding.root.context,
                if (on)
                    com.google.android.material.R.attr.colorOnSurface
                else
                    com.google.android.material.R.attr.colorOnSurfaceVariant,
                Color.BLACK
            )
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

        with(holder.binding) {
            alarmTimeTextView.text = if (alarm.timeMinute >= 10)
                "${alarm.timeHour}:${alarm.timeMinute}"
            else
                "${alarm.timeHour}:0${alarm.timeMinute}"
            alarmNameTextView.text = alarm.name

            if (alarm.recordScore != null)
                recordTextView.text = "Лучший результат: ${alarm.recordScore}"
            else
                recordTextView.text = "Нет данных"

            if (alarm.activateDate != null) {
                recordTextView.text = "Одноразовый"
            }

            alarmOnOffSwitch.isChecked = alarm.isOn
            holder.setState(alarmOnOffSwitch.isChecked, alarm.activateDate == null)
            alarmOnOffSwitch.setOnClickListener {
                alarm.isOn = alarmOnOffSwitch.isChecked
                holder.listener.onOnOffSwitchClickListener(alarm)
                holder.setState(alarmOnOffSwitch.isChecked, alarm.activateDate == null)
            }

            if (alarm.isVibration) vibrationImageView.visibility = View.VISIBLE
            if (alarm.isRisingVolume) volumeUpImageView.visibility = View.VISIBLE

            var gameCount = 0
            for (game in alarm.gamesList)
                if (game != 0)
                    gameCount++

            gameCounterTextView.text = gameCount.toString()


            menuButton.setOnClickListener {
                val menu = PopupMenu(holder.binding.root.context, it)
                menu.menuInflater.inflate(R.menu.menu_alarm_unit, menu.menu)

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
                                .setMessage("Вы уверены, что хотите удалить ${alarm.name}?")
                                .setPositiveButton("Да") { dialog, _ ->
                                    holder.listener.deleteAlarm(alarm)
                                    Toast.makeText(
                                        holder.binding.root.context,
                                        "${alarm.name} удалён",
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

                try {
                    val field = PopupMenu::class.java.getDeclaredField("mPopup")
                    field.isAccessible = true
                    val popup = field.get(menu)
                    popup.javaClass
                        .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                        .invoke(popup, true)
                } catch (e: Exception) {
                    Log.e("Menu", "Error showing menu icons", e)
                } finally {
                    menu.show()
                }
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
    }
}