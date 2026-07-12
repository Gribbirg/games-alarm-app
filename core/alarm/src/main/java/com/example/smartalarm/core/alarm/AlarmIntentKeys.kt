package com.example.smartalarm.core.alarm

/**
 * Ключи extras интентов, связывающих постановку будильника
 * ([AlarmCreateRepository]), его срабатывание ([AlarmReceiver]) и экран игр.
 *
 * Раньше ключи были строковыми литералами, продублированными по трём классам;
 * использовать только эти константы, чтобы они не разъезжались.
 */
object AlarmIntentKeys {
    const val ALARM_ID = "alarm id"
    const val ALARM_TIME = "alarm time"
    const val ALARM_NAME = "alarm name"
    const val ALARM_VIBRATION = "alarm vibration"
    const val ALARM_RISING_VOLUME = "alarm rising volume"
    const val ALARM_RINGTONE_PATH = "alarm ringtone path"
    const val START_TIME = "start time"
}
