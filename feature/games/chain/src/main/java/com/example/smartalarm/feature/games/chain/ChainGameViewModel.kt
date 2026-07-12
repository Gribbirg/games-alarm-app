package com.example.smartalarm.feature.games.chain

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
 * ViewModel экрана игры «Цепочка».
 *
 * Тонкий слой над чистой логикой [ChainGame]: ведёт таймер партии, счёт очков,
 * перезапуск будильника при выходе из игры и публикует текущее состояние
 * (показываемый текст, прогресс, фазу ввода) через LiveData. Состояние партии
 * живёт во ViewModel и переживает поворот экрана.
 */
class ChainGameViewModel(application: Application) : AndroidViewModel(application) {
    private var timeStarted: Long = 0
    private var timeCurrent: Long = 0

    /** Время с начала партии в формате «м.сс», обновляется раз в секунду. */
    var timeCurrentString: MutableLiveData<String> = MutableLiveData()

    /** Крупный текст в центре экрана: стартовое число, текущая операция или «= ?». */
    val displayText: MutableLiveData<String> = MutableLiveData()

    /** Текст прогресса вида «Шаг 2 из 5» / «Введите итог · Цепочка 1 из 2». */
    val progressText: MutableLiveData<String> = MutableLiveData()

    /** `true`, когда все операции показаны и нужно показать поле ввода итога. */
    val inputPhase: MutableLiveData<Boolean> = MutableLiveData()

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

    private var game: ChainGame? = null

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
            game = ChainGame(difficulty)
        publishState()
    }

    /** Открывает следующую операцию цепочки (кнопка «Дальше») и обновляет LiveData. */
    fun showNextStep() {
        game?.showNextStep()
        publishState()
    }

    /**
     * Проверяет введённый игроком итог цепочки и обновляет LiveData.
     * За неверный ответ снимается 10 очков.
     *
     * @param input введённая строка.
     * @return результат хода (нет ответа / неверно / цепочка решена / победа).
     */
    fun submitAnswer(input: String): ChainAnswerResult {
        val answerResult = game?.submitAnswer(input) ?: return ChainAnswerResult.EMPTY
        if (answerResult == ChainAnswerResult.WRONG)
            score -= 10
        publishState()
        return answerResult
    }

    private fun publishState() {
        game?.let {
            displayText.value = it.currentDisplay
            progressText.value = buildProgressText(it)
            inputPhase.value = it.isInputPhase
        }
    }

    /** Собирает текст прогресса: текущий шаг и, для сложности 3, номер цепочки. */
    private fun buildProgressText(game: ChainGame): String {
        val stepPart = when {
            game.isInputPhase -> "Введите итог"
            game.shownSteps == 0 -> "Стартовое число"
            else -> "Шаг ${game.shownSteps} из ${game.currentChain.steps.size}"
        }
        return if (game.totalChains > 1)
            "$stepPart · Цепочка ${minOf(game.solvedChains + 1, game.totalChains)} из ${game.totalChains}"
        else
            stepPart
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
