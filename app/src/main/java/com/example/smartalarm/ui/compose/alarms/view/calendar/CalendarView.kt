package com.example.smartalarm.ui.compose.alarms.view.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartalarm.data.repositories.getDefaultWeekDataList
import com.example.smartalarm.ui.compose.alarms.view.calendarday.CalendarDayState
import com.example.smartalarm.ui.compose.alarms.view.calendarday.CalendarDayView
import com.example.smartalarm.ui.theme.GamesAlarmTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarView(
    onEvent: (CalendarViewEvent) -> Unit,
    state: CalendarViewState
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = {
        state.data.size
    })

    LaunchedEffect(state.selectedDayNum) {
        pagerState.animateScrollToPage(state.selectedDayNum / 7)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            }) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Предыдущая неделя")
            }
            Text(text = state.data[pagerState.currentPage].monthList[0])
            Text(text = state.data[pagerState.currentPage].monthList[1])
            Text(text = state.data[pagerState.currentPage].monthList[2])
            IconButton(onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Следующая неделя")
            }
        }
        HorizontalPager(state = pagerState) { page ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Spacer(modifier = Modifier.width(2.dp))
                repeat(7) { num ->
                    val dayNum = page * 7 + num
                    CalendarDayView(
                        Modifier.weight(1f),
                        onEvent,
                        CalendarDayState(
                            state.data[page].daysList[num],
                            dayNum,
                            state.selectedDayNum == dayNum
                        ),
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
}

@Preview(apiLevel = 33, showBackground = true)
@Composable
fun CalendarViewPreview() {
    GamesAlarmTheme {
        Scaffold {
            Box(
                modifier = Modifier.padding(it)
            ) {
                CalendarView(
                    {},
                    CalendarViewState(getDefaultWeekDataList(100), 0),
                )
            }
        }
    }
}

@Preview(apiLevel = 33, showBackground = true)
@Composable
fun CalendarViewDarkPreview() {
    GamesAlarmTheme(darkTheme = true) {
        Scaffold {
            Box(
                modifier = Modifier.padding(it)
            ) {
                CalendarView(
                    {},
                    CalendarViewState(getDefaultWeekDataList(100), 0)
                )
            }
        }
    }
}

