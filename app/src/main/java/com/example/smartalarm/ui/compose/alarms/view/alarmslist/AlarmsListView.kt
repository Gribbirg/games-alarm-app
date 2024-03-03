package com.example.smartalarm.ui.compose.alarms.view.alarmslist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.example.smartalarm.ui.compose.alarms.PreviewListener
import com.example.smartalarm.ui.theme.GamesAlarmTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlarmsListView(
    state: AlarmsListState,
    listener: AlarmsListListener
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
            val pagerState = rememberPagerState(initialPage = state.dayNum) { 700 }

            LaunchedEffect(state.dayNum) {
                if (state.dayNum != pagerState.currentPage) {
                    pagerState.animateScrollToPage(state.dayNum)
                }
            }

            LaunchedEffect(pagerState.currentPage) {
                if (state.dayNum != pagerState.targetPage) {
                    listener.pagerScroll(pagerState.currentPage)
                }
            }



            HorizontalPager(state = pagerState) { index ->

                val alarmsList = state.alarmsList[index % 7]

                if (alarmsList.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(alarmsList) { alarm ->
                            Text(text = alarm.toString())
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.AlarmOff, contentDescription = "Нет будильников")
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(text = "Нет будильников!")
                    }
                }
            }
        }

        is AlarmsListErrorState -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.Error, contentDescription = "Ошибка")
                Text(text = state.text)
            }
        }
    }
}

interface AlarmsListListener {
    fun pagerScroll(dayNum: Int)
}

@Preview(wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE, apiLevel = 33)
@Composable
fun AlarmsListViewPreview() {
    GamesAlarmTheme {
        Scaffold {
            Box(modifier = Modifier.padding(it)) {
                AlarmsListView(
                    state = AlarmsListLoadingState(0),
                    listener = PreviewListener()
                )
            }
        }
    }
}