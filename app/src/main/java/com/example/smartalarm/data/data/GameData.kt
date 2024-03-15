package com.example.smartalarm.data.data

import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import java.util.Vector

@Serializable
data class GameData(
    val id: Int,
    val name: String,
    val description: String,
    var icon: Int,
    val levels: List<GameLevelData>,
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