package com.example.smartalarm.feature.games.weekday

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
 * ViewModel игры «День недели».
 *
 * Тонкий слой над чистой логикой [WeekdayGame]: держит таймер партии,
 * счёт (ошибка −10 очков, финиш +(600 − секунд) × сложность), доступ к Room
 * и перезапуск будильника, если игра не пройдена.
 */
class WeekdayGameViewModel(application: Application) : AndroidViewModel(application) {
    private var timeStarted: Long = 0
    private var timeCurrent: Long = 0

    /** Время с начала партии в формате «м.сс»; обновляется раз в секунду. */
    var timeCurrentString: MutableLiveData<String> = MutableLiveData()

    /** Текст текущего вопроса. */
    val questionText: MutableLiveData<String> = MutableLiveData()

    /** Варианты ответа текущего вопроса (4 уникальных дня недели). */
    val options: MutableLiveData<List<Weekday>> = MutableLiveData()

    /** Строка прогресса вида «Верных ответов: 1 из 3». */
    val progressText: MutableLiveData<String> = MutableLiveData()

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

    private lateinit var game: WeekdayGame

    private var score = 0
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

    /** Устанавливает время старта партии (0 — «сейчас», пробный запуск). */
    fun setStartTime(time: Long) {
        timeStarted = if (time != 0L)
            time
        else
            System.currentTimeMillis()
    }

    /**
     * Начинает партию на сложности [difficulty], если она ещё не начата
     * (повторные вызовы после пересоздания фрагмента игру не сбрасывают).
     */
    fun startGame(difficulty: Int) {
        if (!::game.isInitialized) {
            this.difficulty = difficulty
            game = WeekdayGame(difficulty)
        }
        publishState()
    }

    /** true, когда набрано нужное число верных ответов. */
    val isWon: Boolean
        get() = game.isWon

    /**
     * Принимает ответ игрока: обновляет игру, снимает 10 очков за ошибку
     * и публикует новое состояние (вопрос/варианты/прогресс).
     *
     * @return true, если ответ верный
     */
    fun answer(day: Weekday): Boolean {
        val correct = game.answer(day)
        if (!correct) score -= 10
        publishState()
        return correct
    }

    /** Итоговый счёт: к набранному прибавляется (600 − секунд партии) × сложность. */
    fun finishScore(): Int {
        score += ((600 - ((System.currentTimeMillis() - timeStarted) / 1000)) * difficulty).toInt()
        return score
    }

    /** Помечает партию выигранной, чтобы onPause не перезапускал будильник. */
    fun setPositiveResult() {
        result = true
    }

    private fun publishState() {
        questionText.value = game.currentQuestion.text
        options.value = game.currentQuestion.options
        progressText.value =
            "Верных ответов: ${game.correctAnswers} из ${game.questionsToWin}"
    }
}
