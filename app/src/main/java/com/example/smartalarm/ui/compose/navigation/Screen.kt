package com.example.smartalarm.ui.compose.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen (
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    data object Alarms : Screen("Будильники", Icons.Filled.Alarm, "alarms")
    data object Records: Screen("Рекорды", Icons.Filled.EmojiEvents, "records")
    data object Profile : Screen("Профиль", Icons.Filled.Person, "profile")
    data object Settings : Screen("Настройки", Icons.Filled.Settings, "settings")

    companion object {
        fun getList() = listOf(
            Alarms,
            Records,
            Profile,
            Settings
        )
    }
}