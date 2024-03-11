package com.example.smartalarm.ui.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smartalarm.ui.compose.addalarm.AddAlarmScreen
import com.example.smartalarm.ui.compose.addalarm.AddAlarmViewModel
import com.example.smartalarm.ui.compose.alarms.AlarmsScreen
import com.example.smartalarm.ui.compose.alarms.AlarmsViewModel
import com.example.smartalarm.ui.compose.profile.ProfileScreen
import com.example.smartalarm.ui.compose.profile.ProfileViewModel
import com.example.smartalarm.ui.compose.records.RecordsScreen
import com.example.smartalarm.ui.compose.records.RecordsViewModel
import com.example.smartalarm.ui.compose.settings.SettingsScreen
import com.example.smartalarm.ui.compose.settings.SettingsViewModel

@Composable
fun NavGraph(
    navHostController: NavHostController,
    alarmsViewModel: AlarmsViewModel,
    recordsViewModel: RecordsViewModel,
    profileViewModel: ProfileViewModel,
    settingsViewModel: SettingsViewModel,
    addAlarmViewModel: AddAlarmViewModel
) {
    NavHost(
        navController = navHostController,
        startDestination = Screen.Alarms.route
    ) {
        composable(Screen.Alarms.route) {
            val state by alarmsViewModel.state.collectAsState()
            AlarmsScreen(
                state = state,
                onEvent = alarmsViewModel::onEvent,
                onAlarmItemEvent = alarmsViewModel::onAlarmsListItemEvent,
                onTimePickerDialogEvent = alarmsViewModel::onTimePickerDialogEvent,
                navigateToAddAlarmScreen = { isNew, alarm ->
                    addAlarmViewModel.setAlarm(isNew, alarm)
                    navHostController.navigate("${Screen.Alarms.route}/addalarm")
                }
            )
        }
        composable(Screen.Records.route) {
            val state by recordsViewModel.state.collectAsState()
            RecordsScreen(onEvent = recordsViewModel::onEvent, state = state)
        }

        composable(Screen.Profile.route) {
            val state by profileViewModel.state.collectAsState()
            ProfileScreen(onEvent = profileViewModel::onEvent, state = state)
        }

        composable(Screen.Settings.route) {
            val state by settingsViewModel.state.collectAsState()
            SettingsScreen(onEvent = settingsViewModel::onEvent, state = state)
        }

        composable("${Screen.Alarms.route}/addalarm") {
            val state by addAlarmViewModel.state.collectAsState()
            AddAlarmScreen(
                onEvent = addAlarmViewModel::onEvent,
                onAlarmItemEvent = addAlarmViewModel::onAlarmItemEvent,
                onTimePickerDialogEvent = addAlarmViewModel::onTimePickerDialogEvent,
                state = state,
                toAlarmsScreen = { dayOfWeek, isNew, alarm ->
                    alarmsViewModel.refresh(dayOfWeek)
                    alarmsViewModel.afterAlarmChange(isNew, alarm)
                    navHostController.navigate(Screen.Alarms.route)
                }
            )
        }
    }
}