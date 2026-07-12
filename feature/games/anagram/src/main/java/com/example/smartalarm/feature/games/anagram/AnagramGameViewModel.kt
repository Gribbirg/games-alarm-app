package com.example.smartalarm.feature.games.anagram

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
import com.example.smartalarm.feature.games.anagram.logic.AnagramGame
import kotlinx.coroutines.launch

/**
 * ViewModel экрана игры «Анаграммы».
 *
 * Тонкий слой над чистой логикой [AnagramGame]: ведёт таймер игры,
 * считает очки, перезапускает будильник, если игра не пройдена,
 * и хранит состояние игры между пересозданиями фрагмента.
 */
class AnagramGameViewModel(application: Application) : AndroidViewModel(application) {
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

    /** Состояние игры (чистая логика). Создаётся в [startGame]. */
    lateinit var game: AnagramGame
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
     * Создаёт игру для выставленного уровня сложности, если она ещё не
     * создана — при пересоздании фрагмента текущий прогресс сохраняется.
     */
    fun startGame() {
        if (!::game.isInitialized)
            game = AnagramGame(difficulty)
    }

    /**
     * Загружает будильник из БД, чтобы перезапустить его, если игрок
     * не пройдёт игру.
     */
    fun getAlarm(alarmId: Long) {
        viewModelScope.launch {
            currentAlarm = alarmDbRepository.getAlarmWithGames(alarmId)
            currentAlarm.milisTime = timeStarted
        }
    }

    /** Перезапускает будильник, если игра ещё не пройдена (вызов из onPause). */
    fun startNewAlarm() {
        if (!result) {
            Log.i("game", "Game not finished!")
            alarmCreateRepository.create(currentAlarm)
        }
    }

    /**
     * Задаёт время старта игры.
     *
     * @param time время срабатывания будильника в миллисекундах;
     * 0 — пробный запуск, отсчёт идёт от текущего момента
     */
    fun setStartTime(time: Long) {
        timeStarted = if (time != 0L)
            time
        else
            System.currentTimeMillis()
    }

    /** Задаёт уровень сложности (1..3). */
    fun setDifficultyLevel(difficulty: Int) {
        this.difficulty = difficulty
    }

    /**
     * Начисляет финальные очки за прохождение:
     * `(600 − прошло_секунд) × difficulty` плюс накопленные штрафы.
     */
    fun finishScore(): Int {
        score += ((600 - ((System.currentTimeMillis() - timeStarted) / 1000)) * difficulty).toInt()
        return score
    }

    /** Штраф за ошибку (неверно собранное слово): −10 очков. */
    fun mistake() {
        score -= 10
    }

    /** Помечает игру пройденной, чтобы onPause не перезапустил будильник. */
    fun setPositiveResult() {
        result = true
    }

    override fun onCleared() {
        handler.removeCallbacks(runnable)
        super.onCleared()
    }
}
