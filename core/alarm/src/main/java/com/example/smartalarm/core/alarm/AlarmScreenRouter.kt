package com.example.smartalarm.core.alarm

import android.app.Activity

/**
 * Точка подключения экрана, открываемого при срабатывании будильника.
 *
 * Модуль :core:alarm не знает об активностях приложения, поэтому :app
 * регистрирует класс игровой активности в Application.onCreate — до того,
 * как [AlarmReceiver] сможет сработать.
 */
object AlarmScreenRouter {
    /**
     * Класс активности, запускаемой при срабатывании будильника
     * (в приложении — GamesActivity).
     */
    lateinit var alarmActivityClass: Class<out Activity>
}
