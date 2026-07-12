package com.example.smartalarm.feature.games.fifteen

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
 * ViewModel экрана «Пятнашек»: секундомер партии, перезапуск будильника при уходе
 * с экрана и подсчёт очков. Экземпляр партии [FifteenGame] живёт здесь,
 * поэтому переживает поворот экрана.
 */
class FifteenGameViewModel(application: Application) : AndroidViewModel(application) {
    private var timeStarted: Long = 0
    private var timeCurrent: Long = 0

    /** Время с начала партии в формате «м.сс», обновляется каждую секунду. */
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

    /** Текущая партия; создаётся один раз в [ensureGame]. */
    var game: FifteenGame? = null
        private set

    private var score = 0

    /** Уровень сложности партии (1..3). */
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
     * Возвращает текущую партию, создавая её при первом вызове
     * для уровня сложности [difficultyLevel]. При повороте экрана
     * повторный вызов вернёт ту же партию.
     */
    fun ensureGame(difficultyLevel: Int): FifteenGame {
        difficulty = difficultyLevel
        return game ?: FifteenGame(FifteenDifficulty.forLevel(difficultyLevel)).also { game = it }
    }

    /**
     * Загружает будильник [alarmId] из БД, чтобы его можно было перезапустить
     * в [startNewAlarm], если игра не пройдена.
     */
    fun getAlarm(alarmId: Long) {
        viewModelScope.launch {
            currentAlarm = alarmDbRepository.getAlarmWithGames(alarmId)
            currentAlarm.milisTime = timeStarted
        }
    }

    /**
     * Перезапускает будильник, если партия ещё не выиграна
     * (вызывается из `onPause` фрагмента).
     */
    fun startNewAlarm() {
        if (!result) {
            Log.i("game", "Game not finished!")
            alarmCreateRepository.create(currentAlarm)
        }
    }

    /**
     * Задаёт момент старта секундомера: [time] из аргументов экрана
     * либо текущее время, когда аргумент равен нулю (пробный запуск).
     */
    fun setStartTime(time: Long) {
        timeStarted = if (time != 0L)
            time
        else
            System.currentTimeMillis()
    }

    /**
     * Итоговые очки: `(600 - секунды с начала партии) * сложность`
     * плюс бонус за экономность ходов ([FifteenGame.moveBonus]).
     */
    fun finishScore(): Int {
        score += ((600 - ((System.currentTimeMillis() - timeStarted) / 1000)) * difficulty).toInt()
        score += game?.moveBonus() ?: 0
        return score
    }

    /**
     * Помечает партию выигранной — будильник больше не перезапускается.
     */
    fun setPositiveResult() {
        result = true
    }
}
