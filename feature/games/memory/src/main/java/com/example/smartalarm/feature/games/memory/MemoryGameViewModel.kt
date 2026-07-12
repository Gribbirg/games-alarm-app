package com.example.smartalarm.feature.games.memory

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.core.alarm.AlarmCreateRepository
import com.example.smartalarm.core.data.db.AlarmsDB
import com.example.smartalarm.core.data.model.AlarmData
import com.example.smartalarm.core.data.repositories.AlarmDbRepository
import kotlinx.coroutines.launch

/**
 * ViewModel экрана «Повтори узор».
 *
 * Тонкий слой над [MemoryGameLogic]: держит логику игры (переживает
 * пересоздание фрагмента), ведёт секундный таймер и умеет перезапустить
 * будильник, если игра не пройдена (контракт экрана игры).
 */
class MemoryGameViewModel(application: Application) : AndroidViewModel(application) {

    private var timeStarted: Long = 0
    private var timeCurrent: Long = 0

    /** Строка таймера в формате «м.сс», обновляется каждую секунду. */
    var timeCurrentString: MutableLiveData<String> = MutableLiveData()

    private val handler = Handler(Looper.getMainLooper())
    private var runnable = object : Runnable {
        override fun run() {
            timeCurrent = System.currentTimeMillis() - timeStarted
            timeCurrentString.postValue(
                "${timeCurrent / 60000}.${
                    if ((timeCurrent / 1000) % 60 < 10) "0"
                    else ""
                }${(timeCurrent / 1000) % 60}"
            )
            handler.postDelayed(this, 1000)
        }
    }

    /** Чистая логика игры; создаётся в [initGame]. */
    lateinit var logic: MemoryGameLogic
        private set

    private val alarmDbRepository = AlarmDbRepository(
        AlarmsDB.getInstance(getApplication())?.alarmsDao()!!
    )

    private lateinit var currentAlarm: AlarmData

    private val alarmCreateRepository = AlarmCreateRepository(application.applicationContext)

    private var result = false

    init {
        handler.post(runnable)
    }

    /**
     * Создаёт логику игры для уровня сложности [difficulty] (1..3).
     *
     * Повторные вызовы игнорируются: при пересоздании фрагмента
     * (например, повороте экрана) игра продолжается с текущего раунда.
     */
    fun initGame(difficulty: Int) {
        if (!::logic.isInitialized)
            logic = MemoryGameLogic(difficulty)
    }

    /** Загружает будильник [alarmId] из БД, чтобы его можно было перезапустить. */
    fun getAlarm(alarmId: Long) {
        viewModelScope.launch {
            currentAlarm = alarmDbRepository.getAlarmWithGames(alarmId)
            currentAlarm.milisTime = timeStarted
        }
    }

    /** Перезапускает будильник, если игра ещё не пройдена (вызывается из `onPause`). */
    fun startNewAlarm() {
        if (!result) {
            Log.i("game", "Game not finished!")
            alarmCreateRepository.create(currentAlarm)
        }
    }

    /**
     * Задаёт момент старта игры: [time] из аргументов экрана
     * или текущее время, если передан 0 (режим пробы).
     */
    fun setStartTime(time: Long) {
        timeStarted = if (time != 0L)
            time
        else
            System.currentTimeMillis()
    }

    /**
     * Итоговый счёт: штрафы за ошибки плюс
     * `(600 − прошло_секунд) × сложность` (см. [MemoryGameLogic.finalScore]).
     */
    fun finishScore(): Int =
        logic.finalScore((System.currentTimeMillis() - timeStarted) / 1000)

    /** Помечает игру пройденной, чтобы `onPause` не перезапустил будильник. */
    fun setPositiveResult() {
        result = true
    }

    override fun onCleared() {
        handler.removeCallbacks(runnable)
        super.onCleared()
    }
}
