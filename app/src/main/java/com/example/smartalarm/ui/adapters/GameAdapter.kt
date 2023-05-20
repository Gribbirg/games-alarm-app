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
        val currentData = data[position]

        with(holder.binding) {

            gameNameTextView.text = currentData.name
            difficultyButton.text = when (currentData.difficulty) {
                1 -> "Лёгкая"
                2 -> "Средняя"
                3 -> "Сложная"
                else -> ""
            }
            onOffSwitch.isChecked = currentData.isOn

            onOffSwitch.setOnClickListener {
                currentData.isOn = onOffSwitch.isChecked
                holder.listener.onOnOffSwitchClickListener(currentData)
            }

            difficultyButton.setOnClickListener {
                val menu = PopupMenu(root.context, it)
                menu.inflate(R.menu.game_difficulty)
                menu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.easyItem -> {
                            currentData.difficulty = 1
                            difficultyButton.text = "Лёгкая"
                            holder.listener.onChangeDifficultyClickListener(currentData)
                            true
                        }
                        R.id.middleItem -> {
                            currentData.difficulty = 2
                            difficultyButton.text = "Средняя"
                            holder.listener.onChangeDifficultyClickListener(currentData)
                            true
                        }
                        R.id.hardItem -> {
                            currentData.difficulty = 3
                            difficultyButton.text = "Сложная"
                            holder.listener.onChangeDifficultyClickListener(currentData)
                            true
                        }
                        else -> true
                    }
                }
                menu.show()
            }

            testGameButton.setOnClickListener {
                holder.listener.testButtonClickListener(currentData)
            }
        }
    }

    interface OnGameClickListener {

        fun onOnOffSwitchClickListener(gameData: AlarmGameData)

        fun onChangeDifficultyClickListener(gameData: AlarmGameData)

        fun testButtonClickListener(gameData: AlarmGameData)

    }
}