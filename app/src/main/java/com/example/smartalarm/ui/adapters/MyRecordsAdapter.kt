package com.example.smartalarm.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.smartalarm.R
import com.example.smartalarm.data.db.GameData
import com.example.smartalarm.databinding.RecordItemBinding

class MyRecordsAdapter(var data: List<GameData>, private val listener: OnMyRecordClickListener) :
    RecyclerView.Adapter<MyRecordsAdapter.MyRecordsViewHolder>() {

    class MyRecordsViewHolder(val binding: RecordItemBinding, val listener: OnMyRecordClickListener) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRecordsViewHolder {
        val binding = RecordItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyRecordsViewHolder(binding, listener)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MyRecordsViewHolder, position: Int) {
        val currentData = data[position]

        with(holder.binding) {
            gameNameRecordsTextView.text = currentData.name
            if (currentData.recordDate != null)
                dateRecordTextView.text = currentData.recordDate
            if (currentData.record != null)
                recordPointsTextView.text = currentData.record.toString()
            if (currentData.recordTime != null)
                recordTimeTextView.text = "Время: ${currentData.recordTime}"
            else
                shareButton.visibility = View.GONE

            if (currentData.recordShared)
                shareButton.visibility = View.GONE

            shareButton.setOnClickListener {
                AlertDialog.Builder(holder.binding.root.context)
                    .setTitle("Поделиться результатом")
                    .setIcon(R.drawable.baseline_warning_24)
                    .setMessage("Вы уверены, что хотите поделиться данным результатом? Его смогут увидеть другие пользователи")
                    .setPositiveButton("Да") { dialog, _ ->
                        listener.onShareClickListener(currentData)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Нет") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }
    }

    interface OnMyRecordClickListener {
        fun onShareClickListener(gameData: GameData)
    }
}