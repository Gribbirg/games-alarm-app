package com.example.smartalarm.ui.compose.alarms.view.alarmslist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import com.example.smartalarm.ui.compose.alarms.AlarmsListLoadedState
import com.example.smartalarm.ui.compose.alarms.AlarmsListLoadingState
import com.example.smartalarm.ui.compose.alarms.AlarmsListState
import com.example.smartalarm.ui.theme.GamesAlarmTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlarmsListView(
    state: AlarmsListState
) {
    when (state) {
        is AlarmsListLoadingState -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }

        is AlarmsListLoadedState -> {
            HorizontalPager(state = rememberPagerState { state.alarmsList.size }) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight()
                ) {

                }
            }
        }
    }
}

@Preview(wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE, apiLevel = 33)
@Composable
fun AlarmsListViewPreview() {
    GamesAlarmTheme {
        Scaffold {
            Box(modifier = Modifier.padding(it)) {
                AlarmsListView(state = AlarmsListLoadingState())
            }
        }
    }
}