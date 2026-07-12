package com.example.smartalarm.feature.games.digits

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
 * Фаза экрана игры «Запомни число».
 */
enum class DigitsPhase {
    /** Число показано на экране, идёт отсчёт времени запоминания. */
    SHOWING,

    /** Число скрыто, игрок вводит его по памяти. */
    INPUT
}

/**
 * ViewModel игры «Запомни число»: хранит состояние партии ([DigitsGameEngine]),
 * текущую фазу и момент начала показа (чтобы после поворота экрана число
 * показывалось лишь остаток времени), а также счёт, игровой таймер и работу
 * с будильником — по образцу CalcGameViewModel. Сам тайминг показа
 * (Handler.postDelayed) живёт во фрагменте.
 */
class DigitsGameViewModel(application: Application) : AndroidViewModel(application) {
    private var timeStarted: Long = 0
    private var timeCurrent: Long = 0

    /** Игровой таймер в формате «м.сс» для отображения и записи рекорда. */
    var timeCurrentString: MutableLiveData<String> = MutableLiveData()

    /** Текущая фаза экрана: показ числа или ввод ответа. */
    val phase: MutableLiveData<DigitsPhase> = MutableLiveData()

    /** Момент начала текущего показа числа (System.currentTimeMillis). */
    var showPhaseStartMillis: Long = 0
        private set

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

    private var engine: DigitsGameEngine? = null

    private var score = 0

    /** Сложность игры 1..3, задаётся из аргументов фрагмента. */
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
     * @param alarmId id будильника из аргументов фрагмента
     */
    fun getAlarm(alarmId: Long) {
        viewModelScope.launch {
            currentAlarm = alarmDbRepository.getAlarmWithGames(alarmId)
            currentAlarm.milisTime = timeStarted
        }
    }

    /** Перезапускает будильник, если игра не была пройдена (вызывается из onPause). */
    fun startNewAlarm() {
        if (!result) {
            Log.i("game", "Game not finished!")
            alarmCreateRepository.create(currentAlarm)
        }
    }

    /**
     * Задаёт время старта игры для таймера и подсчёта очков.
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
     * Задаёт уровень сложности. Должен вызываться до [startGame].
     *
     * @param difficulty сложность 1..3
     */
    fun setDifficultyLevel(difficulty: Int) {
        this.difficulty = difficulty
    }

    /**
     * Начинает партию: создаёт [DigitsGameEngine] и запускает показ первого числа.
     * Повторные вызовы (после поворота экрана) игнорируются — состояние сохраняется.
     */
    fun startGame() {
        if (engine != null)
            return
        engine = DigitsGameEngine(difficulty)
        startShowPhase()
    }

    /** Загаданное число текущего раунда. */
    val currentNumber: String
        get() = engine?.currentNumber ?: ""

    /** Номер текущего раунда для отображения, начиная с 1. */
    val roundNumber: Int
        get() = (engine?.round ?: 0) + 1

    /** Общее количество раундов. */
    val totalRounds: Int
        get() = DigitsLogic.TOTAL_ROUNDS

    /**
     * Сколько миллисекунд осталось показывать число (не меньше 0).
     * После поворота экрана фрагмент по этому значению планирует скрытие,
     * так что суммарное время показа не растёт.
     *
     * @return остаток времени показа в миллисекундах
     */
    fun remainingShowTimeMillis(): Long =
        (DigitsLogic.showTimeMillis(difficulty) -
                (System.currentTimeMillis() - showPhaseStartMillis)).coerceAtLeast(0)

    /** Скрывает число и переводит экран в фазу ввода (вызывается таймером фрагмента). */
    fun finishShowPhase() {
        if (phase.value == DigitsPhase.SHOWING)
            phase.value = DigitsPhase.INPUT
    }

    /**
     * Проверяет ответ игрока. При ошибке снимает 10 очков; при ошибке и при
     * переходе к следующему раунду заново запускает фазу показа нового числа.
     *
     * @param answer введённый игроком ответ
     * @return результат проверки
     */
    fun submitAnswer(answer: String): AnswerResult {
        val answerResult = engine!!.submitAnswer(answer)
        when (answerResult) {
            AnswerResult.WRONG -> {
                score -= 10
                startShowPhase()
            }

            AnswerResult.NEXT_ROUND -> startShowPhase()

            AnswerResult.WIN -> Unit
        }
        return answerResult
    }

    /**
     * Считает финальный счёт: к набранным очкам прибавляется
     * (600 − прошло_секунд) × сложность, как в остальных играх.
     *
     * @return итоговый счёт
     */
    fun finishScore(): Int {
        score += ((600 - ((System.currentTimeMillis() - timeStarted) / 1000)) * difficulty).toInt()
        return score
    }

    /** Помечает игру пройденной, чтобы будильник не перезапускался в onPause. */
    fun setPositiveResult() {
        result = true
    }

    private fun startShowPhase() {
        showPhaseStartMillis = System.currentTimeMillis()
        phase.value = DigitsPhase.SHOWING
    }

    override fun onCleared() {
        handler.removeCallbacks(runnable)
        super.onCleared()
    }
}
