package com.example.smartalarm.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartalarm.data.db.GameData
import com.example.smartalarm.databinding.RecordItemBinding

class MyRecordsAdapter(var data: List<GameData>) :
    RecyclerView.Adapter<MyRecordsAdapter.MyRecordsViewHolder>() {

    class MyRecordsViewHolder(val binding: RecordItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRecordsViewHolder {
        val binding = RecordItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyRecordsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MyRecordsViewHolder, position: Int) {
        val currencyData = data[position]

        with(holder.binding) {
            gameNameRecordsTextView.text = currencyData.name
            if (currencyData.recordDate != null)
                dateRecordTextView.text = currencyData.recordDate
            if (currencyData.record != null)
                recordPointsTextView.text = currencyData.record.toString()
            if (currencyData.recordTime != null)
                recordTimeTextView.text = currencyData.recordTime
        }
    }
}