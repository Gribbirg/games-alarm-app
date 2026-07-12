package com.example.smartalarm.feature.games.reaction

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
 * ViewModel экрана игры «Поймай момент».
 *
 * Тонкий слой над чистой логикой [ReactionGame]: держит таймер экрана
 * («м.сс»), точку отсчёта движения бегунка ([waveStartMs]), пересоздаёт
 * будильник при уходе с экрана без победы и считает финальные очки
 * по формуле игры-образца (арифметики).
 */
class ReactionGameViewModel(application: Application) : AndroidViewModel(application) {
    private var timeStarted: Long = 0
    private var timeCurrent: Long = 0

    /** Время с начала игры в формате «м.сс», обновляется раз в секунду. */
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

    /** Чистая логика игры; создаётся в [startGame]. */
    lateinit var game: ReactionGame
        private set

    /**
     * Момент старта движения бегунка (миллисекунды `System.currentTimeMillis`).
     * Хранится во ViewModel, чтобы волна не «перепрыгивала» при пересоздании
     * фрагмента; позиция бегунка — `ReactionWave.position(now - waveStartMs, ...)`.
     */
    var waveStartMs: Long = 0
        private set

    private var difficulty = 0

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
     * Загружает будильник из БД, чтобы уметь пересоздать его в [startNewAlarm].
     *
     * @param alarmId id сработавшего будильника
     */
    fun getAlarm(alarmId: Long) {
        viewModelScope.launch {
            currentAlarm = alarmDbRepository.getAlarmWithGames(alarmId)
            currentAlarm.milisTime = timeStarted
        }
    }

    /** Пересоздаёт будильник, если игра ещё не пройдена (вызов из onPause). */
    fun startNewAlarm() {
        if (!result) {
            Log.i("game", "Game not finished!")
            alarmCreateRepository.create(currentAlarm)
        }
    }

    /**
     * Запоминает время старта игры.
     *
     * @param time время в миллисекундах; 0 — «проба», берётся текущее время
     */
    fun setStartTime(time: Long) {
        timeStarted = if (time != 0L)
            time
        else
            System.currentTimeMillis()
    }

    /**
     * Задаёт сложность и создаёт новую игру, если она ещё не создана
     * (переживает пересоздание фрагмента).
     *
     * @param difficulty уровень сложности 1..3
     */
    fun startGame(difficulty: Int) {
        this.difficulty = difficulty
        if (!::game.isInitialized) {
            game = ReactionGame(difficulty)
            waveStartMs = System.currentTimeMillis()
        }
    }

    /**
     * Финальные очки: набранные в игре плюс бонус за скорость
     * `(600 − прошло_секунд) × сложность`.
     */
    fun finishScore(): Int {
        return game.score +
                ((600 - ((System.currentTimeMillis() - timeStarted) / 1000)) * difficulty).toInt()
    }

    /** Помечает игру пройденной, чтобы onPause не пересоздал будильник. */
    fun setPositiveResult() {
        result = true
    }
}
