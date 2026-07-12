package com.example.smartalarm.core.data.model

import com.example.smartalarm.core.data.db.GameData

data class AlarmGameData(
    var id: Int,
    var name: String,
    var isOn: Boolean = false,
    var difficulty: Int = 1
) {
    constructor(gameData: GameData, difficulty: Int) :
            this(gameData.id, gameData.name) {
                if (difficulty != 0) {
                    isOn = true
                    this.difficulty = difficulty
                }
            }
}
