package com.example.smartalarm.feature.games.equation

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
 * ViewModel экрана игры «Уравнение».
 *
 * Тонкий слой над чистой логикой [EquationGame]: ведёт таймер партии,
 * счёт очков, перезапуск будильника при выходе из игры и публикует
 * текущее задание/прогресс через LiveData.
 */
class EquationGameViewModel(application: Application) : AndroidViewModel(application) {
    private var timeStarted: Long = 0
    private var timeCurrent: Long = 0

    /** Время с начала партии в формате «м.сс», обновляется раз в секунду. */
    var timeCurrentString: MutableLiveData<String> = MutableLiveData()

    /** Текущее уравнение с вариантами ответа. */
    val currentTask: MutableLiveData<EquationTask> = MutableLiveData()

    /** Текст прогресса вида «Решено: 1 / 4». */
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

    private var game: EquationGame? = null

    private var score = 0

    /** Текущая сложность игры (1..3). */
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
     * Загружает будильник из БД, чтобы его можно было перезапустить,
     * если игра не будет пройдена.
     *
     * @param alarmId id сработавшего будильника.
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
     * Запоминает время старта партии.
     *
     * @param time время срабатывания будильника в миллисекундах; 0 — пробный запуск,
     * тогда берётся текущее время.
     */
    fun setStartTime(time: Long) {
        timeStarted = if (time != 0L)
            time
        else
            System.currentTimeMillis()
    }

    /**
     * Задаёт сложность игры.
     *
     * @param difficulty сложность 1..3.
     */
    fun setDifficultyLevel(difficulty: Int) {
        this.difficulty = difficulty
    }

    /**
     * Создаёт партию (если она ещё не создана — например, после поворота экрана
     * партия сохраняется) и публикует состояние в LiveData.
     */
    fun startGame() {
        if (game == null)
            game = EquationGame(difficulty)
        publishState()
    }

    /**
     * Обрабатывает выбранный игроком вариант ответа и обновляет LiveData.
     * За ошибку снимается 10 очков.
     *
     * @param option текст выбранного варианта.
     * @return результат хода (верно / неверно / победа).
     */
    fun submitAnswer(option: String): AnswerResult {
        val answerResult = game?.submitAnswer(option) ?: return AnswerResult.WRONG
        if (answerResult == AnswerResult.WRONG)
            score -= 10
        publishState()
        return answerResult
    }

    private fun publishState() {
        game?.let {
            currentTask.value = it.currentTask
            progressText.value = "Решено: ${it.solvedCount} / ${it.totalRounds}"
        }
    }

    /**
     * Считает итоговые очки: к набранным добавляется
     * (600 − прошедшие_секунды) × сложность.
     *
     * @return итоговый счёт партии.
     */
    fun finishScore(): Int {
        score += ((600 - ((System.currentTimeMillis() - timeStarted) / 1000)) * difficulty).toInt()
        return score
    }

    /** Помечает игру пройденной — будильник перезапускаться не будет. */
    fun setPositiveResult() {
        result = true
    }
}
