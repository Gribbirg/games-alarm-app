package com.example.smartalarm.feature.games.clock

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
 * ViewModel экрана игры «Который час».
 *
 * Тонкий слой между [ClockGameFragment] и чистой логикой [ClockGame]:
 * ведёт секундомер партии (строка «м.сс» в [timeCurrentString]), хранит счёт,
 * загружает будильник из БД и перезапускает его через [AlarmCreateRepository],
 * если игрок ушёл с экрана, не победив.
 */
class ClockGameViewModel(application: Application) : AndroidViewModel(application) {
    private var timeStarted: Long = 0
    private var timeCurrent: Long = 0

    /** Время с начала партии в формате «м.сс» (минуты без ведущего нуля, секунды с ним). */
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

    /** Чистая логика игры; создаётся в [startGame] и переживает пересоздание фрагмента. */
    lateinit var game: ClockGame
        private set

    private var score = 0

    /** Уровень сложности 1..3, задаётся из аргументов фрагмента. */
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
     * Создаёт [game] для текущей [difficulty], если она ещё не создана —
     * при повороте экрана партия продолжается, а не начинается заново.
     */
    fun startGame() {
        if (!::game.isInitialized) {
            game = ClockGame(difficulty, Random.Default)
        }
    }

    /** Загружает будильник [alarmId] из БД, чтобы его можно было перезапустить. */
    fun getAlarm(alarmId: Long) {
        viewModelScope.launch {
            currentAlarm = alarmDbRepository.getAlarmWithGames(alarmId)
            currentAlarm.milisTime = timeStarted
        }
    }

    /** Перезапускает будильник, если игра ещё не пройдена (вызывается из onPause). */
    fun startNewAlarm() {
        if (!result) {
            Log.i("game", "Game not finished!")
            alarmCreateRepository.create(currentAlarm)
        }
    }

    /**
     * Запоминает момент старта партии: [time] из аргументов будильника
     * или текущее время, если игра запущена как проба (0).
     */
    fun setStartTime(time: Long) {
        timeStarted = if (time != 0L)
            time
        else
            System.currentTimeMillis()
    }

    /** Сохраняет уровень сложности из аргументов фрагмента. */
    fun setDifficultyLevel(difficulty: Int) {
        this.difficulty = difficulty
    }

    /**
     * Начисляет финальный бонус за скорость — (600 − прошло секунд) × сложность —
     * и возвращает итоговый счёт партии.
     */
    fun finishScore(): Int {
        score += ((600 - ((System.currentTimeMillis() - timeStarted) / 1000)) * difficulty).toInt()
        return score
    }

    /** Штрафует за ошибку: −10 очков. */
    fun mistake() {
        score -= 10
    }

    /** Помечает игру пройденной, чтобы onPause не перезапускал будильник. */
    fun setPositiveResult() {
        result = true
    }

    override fun onCleared() {
        handler.removeCallbacks(runnable)
        super.onCleared()
    }
}
