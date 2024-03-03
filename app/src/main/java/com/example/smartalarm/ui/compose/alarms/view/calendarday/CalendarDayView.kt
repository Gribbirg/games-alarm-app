package com.example.smartalarm.ui.compose.alarms.view.calendarday

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartalarm.data.data.WeekCalendarData
import com.example.smartalarm.data.repositories.getDayOfWeekShortName

@Composable
fun CalendarDayView(
    modifier: Modifier,
    state: CalendarDayState,
    listener: OnDayViewClickListener,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (state.isSelected) MaterialTheme.colorScheme.surfaceContainerHighest
            else MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (state.isSelected) 5.dp else 0.dp,
            pressedElevation = 2.dp
        ),
        onClick = { listener.onDayViewClick(state.num) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val textColor = getTextColor(state.data)
            Text(text = state.data.dayNumber.toString(), fontSize = 25.sp, color = textColor)
            Text(text = getDayOfWeekShortName(state.data.dayOfWeek), color = textColor)
            Text(text = "", color = textColor)
        }
    }
}


@Composable
private fun getTextColor(data: WeekCalendarData.DateUnit): Color = when {
    data.today -> MaterialTheme.colorScheme.tertiary
    data.isHoliday -> MaterialTheme.colorScheme.error
    data.isWeekend -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onBackground
}

interface OnDayViewClickListener {
    fun onDayViewClick(dayNum: Int)
}