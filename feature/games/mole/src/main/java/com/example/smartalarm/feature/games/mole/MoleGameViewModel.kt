package com.example.smartalarm.feature.games.mole

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
import kotlin.random.Random
import kotlinx.coroutines.launch

/**
 * ViewModel игры «Поймай крота».
 *
 * Тонкий слой над чистой логикой [MoleGame]: хранит состояние игры
 * (переживает поворот экрана), ведёт таймер и счёт очков, перезапускает
 * будильник, если игра не пройдена. Тайминги показа крота — во фрагменте.
 */
class MoleGameViewModel(application: Application) : AndroidViewModel(application) {
    private var timeStarted: Long = 0
    private var timeCurrent: Long = 0

    /** Время с начала игры в формате «м.сс». */
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

    /** Состояние игры (норы, попадания, промахи). Создаётся в [ensureGameCreated]. */
    lateinit var game: MoleGame
        private set

    private var score = 0

    /** Уровень сложности 1..3. */
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
     * Загружает будильник из БД, чтобы перезапустить его, если игра не пройдена.
     *
     * @param alarmId id будильника
     */
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
     * Устанавливает время старта игры.
     *
     * @param time время срабатывания будильника или 0 для пробного запуска
     */
    fun setStartTime(time: Long) {
        timeStarted = if (time != 0L)
            time
        else
            System.currentTimeMillis()
    }

    /**
     * Устанавливает уровень сложности.
     *
     * @param difficulty уровень 1..3
     */
    fun setDifficultyLevel(difficulty: Int) {
        this.difficulty = difficulty
    }

    /**
     * Создаёт [game] по текущей сложности, если она ещё не создана.
     * Повторный вызов (после поворота экрана) сохраняет прогресс.
     */
    fun ensureGameCreated() {
        if (!this::game.isInitialized)
            game = MoleGame(MoleDifficulty.forLevel(difficulty), Random.Default)
    }

    /**
     * Итоговый счёт: накопленные штрафы плюс бонус
     * (600 − прошедшие_секунды) × сложность.
     *
     * @return итоговое количество очков
     */
    fun finishScore(): Int {
        score += finishBonus((System.currentTimeMillis() - timeStarted) / 1000, difficulty)
        return score
    }

    /** Штрафует за промах (−10 очков). */
    fun mistake() {
        score -= MoleGame.MISS_PENALTY
    }

    /** Помечает игру пройденной, чтобы будильник не перезапускался. */
    fun setPositiveResult() {
        result = true
    }
}
