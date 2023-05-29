package com.example.smartalarm.ui.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.smartalarm.R
import com.example.smartalarm.data.data.AccountData
import com.example.smartalarm.data.data.RecordInternetData
import com.example.smartalarm.data.db.GameData
import com.example.smartalarm.databinding.AllRecordsItemBinding

class AllRecordsAdapter(
    var data: List<AccountData?>,
    private val listener: OnWorldRecordClickListener
) :
    RecyclerView.Adapter<AllRecordsAdapter.AllRecordsViewHolder>() {

    class AllRecordsViewHolder(
        val binding: AllRecordsItemBinding,
        val listener: OnWorldRecordClickListener
    ) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllRecordsViewHolder {
        val binding = AllRecordsItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AllRecordsViewHolder(binding, listener)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: AllRecordsViewHolder, position: Int) {
        val currentData = data[position]

        with(holder.binding) {

            if (currentData != null) {
                val game = RecordInternetData(currentData.records!!)
                Glide.with(photoRecordHolder.context).load(currentData.photo)
                    .into(userPhotoRecordImageView)
                userNameTextView.text = currentData.name
                gameNameRecordsTextView.text = game.name
                dateRecordTextView.text = game.recordDate
                recordTimeTextView.text = "Время: ${game.recordTime}"
                recordPointsTextView.text = game.record.toString()

                if (holder.listener.isCurrentUser(currentData)) {
                    deleteRecordButton.visibility = View.VISIBLE

                    deleteRecordButton.setOnClickListener {

                        AlertDialog.Builder(holder.binding.root.context)
                            .setTitle("Удаление рекорда")
                            .setIcon(R.drawable.baseline_warning_24)
                            .setMessage("Вы уверены, что хотите удалить данный результат с сервера?")
                            .setPositiveButton("Да") { dialog, _ ->
                                listener.onDeleteClickListener(currentData)
                                dialog.dismiss()
                            }
                            .setNegativeButton("Нет") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()

                    }
                }

            } else {
                Glide.with(photoRecordHolder.context).load(R.drawable.baseline_no_accounts_24)
                    .into(userPhotoRecordImageView)
                userNameTextView.visibility = View.INVISIBLE
                gameNameRecordsTextView.text = "Стань первым!"
                dateRecordTextView.visibility = View.INVISIBLE
                recordTimeTextView.visibility = View.INVISIBLE
                recordPointsTextView.visibility = View.INVISIBLE
            }
        }
    }

    interface OnWorldRecordClickListener {
        fun onDeleteClickListener(accountData: AccountData)
        fun isCurrentUser(accountData: AccountData): Boolean
    }
}