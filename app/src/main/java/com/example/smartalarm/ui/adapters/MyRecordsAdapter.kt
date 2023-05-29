package com.example.smartalarm.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.smartalarm.R
import com.example.smartalarm.data.db.GameData
import com.example.smartalarm.data.db.RecordsData
import com.example.smartalarm.databinding.RecordItemBinding

class MyRecordsAdapter(var data: List<RecordsData>, private val listener: OnMyRecordClickListener) :
    RecyclerView.Adapter<MyRecordsAdapter.MyRecordsViewHolder>() {

    class MyRecordsViewHolder(
        val binding: RecordItemBinding,
        val listener: OnMyRecordClickListener
    ) :
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
            gameNameRecordsTextView.text = currentData.gameName
            if (currentData.date != null) {
                dateRecordTextView.text = currentData.date
                recordPointsTextView.text = currentData.recordScore.toString()
                recordTimeTextView.text = "Время: ${currentData.recordTime}"
            }
            if (currentData.recordShared || currentData.date == null)
                shareButton.visibility = View.GONE

            shareButton.setOnClickListener {
                AlertDialog.Builder(holder.binding.root.context)
                    .setTitle("Поделиться результатом")
                    .setIcon(R.drawable.baseline_warning_24)
                    .setMessage("Вы уверены, что хотите поделиться данным результатом? Его смогут увидеть другие пользователи")
                    .setPositiveButton("Да") { dialog, _ ->
                        currentData.recordShared = true
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
        fun onShareClickListener(recordsData: RecordsData)
    }
}