package com.example.smartalarm.feature.games.hanoi

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
 * ViewModel игры «Ханойская башня».
 *
 * Тонкий слой над чистой [HanoiGameLogic]: хранит состояние игры и
 * выбранный стержень (переживают поворот экрана), ведёт счёт, таймер
 * в формате «м.сс» и, как в остальных играх, перезапускает будильник,
 * если экран покинули, не пройдя игру.
 */
class HanoiGameViewModel(application: Application) : AndroidViewModel(application) {

    private var timeStarted: Long = 0
    private var timeCurrent: Long = 0

    /** Строка таймера «м.сс», обновляется раз в секунду. */
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

    /** Логика текущей партии; создаётся один раз в [ensureGame]. */
    var game: HanoiGameLogic? = null
        private set

    /** Индекс выбранного стержня-источника (0..2) или `null`. */
    var selectedRod: Int? = null

    private var score = 0

    /** Уровень сложности 1..3 (задаёт число дисков). */
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
     * Возвращает логику партии, создавая её при первом вызове с числом
     * дисков по текущему [difficulty]. Повторные вызовы (например, после
     * поворота экрана) возвращают ту же партию.
     */
    fun ensureGame(): HanoiGameLogic =
        game ?: HanoiGameLogic(HanoiGameLogic.disksForDifficulty(difficulty)).also { game = it }

    /** Загружает будильник [alarmId] для возможного перезапуска. */
    fun getAlarm(alarmId: Long) {
        viewModelScope.launch {
            currentAlarm = alarmDbRepository.getAlarmWithGames(alarmId)
            currentAlarm.milisTime = timeStarted
        }
    }

    /** Перезапускает будильник, если игра ещё не пройдена. */
    fun startNewAlarm() {
        if (!result) {
            Log.i("game", "Game not finished!")
            alarmCreateRepository.create(currentAlarm)
        }
    }

    /** Задаёт время старта игры (0 — начать отсчёт с текущего момента). */
    fun setStartTime(time: Long) {
        timeStarted = if (time != 0L)
            time
        else
            System.currentTimeMillis()
    }

    /** Задаёт уровень сложности 1..3. */
    fun setDifficultyLevel(difficulty: Int) {
        this.difficulty = difficulty
    }

    /**
     * Начисляет финальные очки за скорость — `(600 − секунд) × сложность` —
     * и возвращает итоговый счёт (штрафы за ошибки уже учтены).
     */
    fun finishScore(): Int {
        score += ((600 - ((System.currentTimeMillis() - timeStarted) / 1000)) * difficulty).toInt()
        return score
    }

    /** Штраф за недопустимый ход: −10 очков. */
    fun mistake() {
        score -= 10
    }

    /** Помечает игру пройденной, чтобы будильник не перезапускался. */
    fun setPositiveResult() {
        result = true
    }
}
