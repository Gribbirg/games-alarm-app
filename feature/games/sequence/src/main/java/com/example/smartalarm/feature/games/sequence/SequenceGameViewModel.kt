package com.example.smartalarm.feature.games.sequence

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
import kotlin.random.Random

/**
 * ViewModel экрана «Продолжи ряд»: таймер, очки и перезапуск будильника —
 * тонкий слой над чистой игровой логикой [SequenceGame].
 */
class SequenceGameViewModel(application: Application) : AndroidViewModel(application) {
    private var timeStarted: Long = 0
    private var timeCurrent: Long = 0

    /** Прошедшее время партии в формате «м.сс», обновляется раз в секунду. */
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

    /** Состояние партии. Создаётся в [startGame]. */
    lateinit var game: SequenceGame
        private set

    private var score = 0

    /** Текущая сложность 1..3, задаётся в [setDifficultyLevel]. */
    var difficulty = 0

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
     * Загружает будильник из БД, чтобы уметь перезапустить его,
     * если игрок свернул игру, не выиграв.
     */
    fun getAlarm(alarmId: Long) {
        viewModelScope.launch {
            currentAlarm = alarmDbRepository.getAlarmWithGames(alarmId)
            currentAlarm.milisTime = timeStarted
        }
    }

    /** Перезапускает будильник, если партия ещё не выиграна (вызов из onPause). */
    fun startNewAlarm() {
        if (!result) {
            Log.i("game", "Game not finished!")
            alarmCreateRepository.create(currentAlarm)
        }
    }

    /** Задаёт время старта партии (0 — начать отсчёт с текущего момента). */
    fun setStartTime(time: Long) {
        timeStarted = if (time != 0L)
            time
        else
            System.currentTimeMillis()
    }

    /** Задаёт сложность 1..3. Вызывать до [startGame]. */
    fun setDifficultyLevel(difficulty: Int) {
        this.difficulty = difficulty
    }

    /** Создаёт новую партию для текущей сложности. */
    fun startGame() {
        game = SequenceGame(difficulty, Random.Default)
    }

    /**
     * Итоговые очки при победе:
     * накопленные штрафы + (600 − прошло_секунд) × сложность.
     */
    fun finishScore(): Int {
        score += ((600 - ((System.currentTimeMillis() - timeStarted) / 1000)) * difficulty).toInt()
        return score
    }

    /**
     * Принимает ответ игрока; при ошибке снимает 10 очков.
     *
     * @return true, если ответ верный.
     */
    fun checkAnswer(option: Int): Boolean {
        val correct = game.submitAnswer(option)
        if (!correct)
            score -= 10
        return correct
    }

    /** Помечает партию выигранной — будильник перезапускать не нужно. */
    fun setPositiveResult() {
        result = true
    }
}
