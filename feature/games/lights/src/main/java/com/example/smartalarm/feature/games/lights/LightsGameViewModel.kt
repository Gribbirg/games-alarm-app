package com.example.smartalarm.feature.games.lights

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
 * ViewModel игры «Погаси свет».
 *
 * Тонкий слой над чистой логикой [LightsGame]: владеет таймером партии,
 * счётом и перезапуском будильника, если игра не пройдена (по образцу
 * CalcGameViewModel). Сама партия создаётся один раз и переживает
 * пересоздание фрагмента (поворот экрана).
 *
 * @param application приложение для доступа к БД и `AlarmManager`
 */
class LightsGameViewModel(application: Application) : AndroidViewModel(application) {
    private var timeStarted: Long = 0
    private var timeCurrent: Long = 0

    /** Строка таймера партии в формате «м.сс», обновляется раз в секунду. */
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

    /** Состояние партии: поле лампочек и счётчик нажатий. Создаётся в [startGame]. */
    lateinit var game: LightsGame
        private set

    private var score = 0

    /** Текущий уровень сложности (1..3), задаётся в [setDifficultyLevel]. */
    var difficulty = 0
        private set

    /** Число нажатий, укладываясь в которое игрок не получает штрафа (3×K). */
    val pressPar: Int
        get() = lightsPressParFor(difficulty)

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
     * Загружает будильник из БД, чтобы уметь перезапустить его в [startNewAlarm].
     *
     * @param alarmId id будильника из аргументов фрагмента
     */
    fun getAlarm(alarmId: Long) {
        viewModelScope.launch {
            currentAlarm = alarmDbRepository.getAlarmWithGames(alarmId)
            currentAlarm.milisTime = timeStarted
        }
    }

    /**
     * Перезапускает будильник, если игра ещё не пройдена.
     *
     * Вызывается из `onPause` фрагмента (кроме пробного запуска), чтобы
     * будильник нельзя было выключить сворачиванием приложения.
     */
    fun startNewAlarm() {
        if (!result) {
            Log.i("game", "Game not finished!")
            alarmCreateRepository.create(currentAlarm)
        }
    }

    /**
     * Задаёт момент старта партии для таймера и счёта.
     *
     * @param time время срабатывания будильника в миллисекундах;
     * 0 — пробный запуск, отсчёт от текущего момента
     */
    fun setStartTime(time: Long) {
        timeStarted = if (time != 0L)
            time
        else
            System.currentTimeMillis()
    }

    /**
     * Задаёт уровень сложности.
     *
     * @param difficulty уровень (1..3) из аргументов фрагмента
     */
    fun setDifficultyLevel(difficulty: Int) {
        this.difficulty = difficulty
    }

    /**
     * Создаёт партию для текущей сложности, если она ещё не создана.
     *
     * Повторный вызов (например, после поворота экрана) сохраняет
     * уже идущую партию.
     *
     * @param random источник случайности для генерации поля
     */
    fun startGame(random: Random = Random.Default) {
        if (!this::game.isInitialized) {
            val size = lightsSizeFor(difficulty)
            val field = generateLightsField(size, lightsScrambleCountFor(difficulty), random)
            game = LightsGame(size, field.lights)
        }
    }

    /**
     * Считает финальный счёт: бонус за скорость плюс накопленные штрафы.
     *
     * @return итоговые очки партии
     */
    fun finishScore(): Int {
        score += ((600 - ((System.currentTimeMillis() - timeStarted) / 1000)) * difficulty).toInt()
        return score
    }

    /** Штрафует за ошибку — нажатие сверх пара [pressPar] (−10 очков). */
    fun mistake() {
        score -= 10
    }

    /** Помечает игру пройденной: будильник не будет перезапущен в `onPause`. */
    fun setPositiveResult() {
        result = true
    }
}
