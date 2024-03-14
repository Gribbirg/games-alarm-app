package com.example.smartalarm.data.data

import kotlinx.serialization.Serializable

@Serializable
data class GameData(
    val id: Int,
    val name: String,
    val description: String,
    val levels: List<GameLevelData>
) {

    @Serializable
    data class GameLevelData(
        val id: Int,
        val name: String,
        val description: String
    ) {
        override fun toString(): String {
            return "GameLevelData(id=$id, name='$name', description='$description')"
        }
    }

    override fun toString(): String {
        return "GameData(id=$id, name='$name', description='$description', levels=$levels)"
    }
}