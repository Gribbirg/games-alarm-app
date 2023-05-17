package com.example.smartalarm.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.smartalarm.R
import com.example.smartalarm.data.data.AlarmGameData
import com.example.smartalarm.databinding.GameItemBinding

class GameAdapter(var data: ArrayList<AlarmGameData>, private val listener: OnGameClickListener) :
    RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    class GameViewHolder(val binding: GameItemBinding, val listener: OnGameClickListener) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = GameItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GameViewHolder(binding, listener)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val currencyData = data[position]

        with(holder.binding) {

            gameNameTextView.text = currencyData.name
            difficultyButton.text = when (currencyData.difficulty) {
                1 -> "Лёгкая"
                2 -> "Средняя"
                3 -> "Сложная"
                else -> ""
            }
            onOffSwitch.isChecked = currencyData.isOn

            onOffSwitch.setOnClickListener {
                currencyData.isOn = onOffSwitch.isChecked
                holder.listener.onOnOffSwitchClickListener(currencyData)
            }

            difficultyButton.setOnClickListener {
                val menu = PopupMenu(root.context, it)
                menu.inflate(R.menu.game_difficulty)
                menu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.easyItem -> {
                            currencyData.difficulty = 1
                            difficultyButton.text = "Лёгкая"
                            holder.listener.onChangeDifficultyClickListener(currencyData)
                            true
                        }
                        R.id.middleItem -> {
                            currencyData.difficulty = 2
                            difficultyButton.text = "Средняя"
                            holder.listener.onChangeDifficultyClickListener(currencyData)
                            true
                        }
                        R.id.hardItem -> {
                            currencyData.difficulty = 3
                            difficultyButton.text = "Сложная"
                            holder.listener.onChangeDifficultyClickListener(currencyData)
                            true
                        }
                        else -> true
                    }
                }
                menu.show()
            }
        }
    }

    interface OnGameClickListener {

        fun onOnOffSwitchClickListener(gameData: AlarmGameData)

        fun onChangeDifficultyClickListener(gameData: AlarmGameData)

    }
}