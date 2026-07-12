package com.example.smartalarm.feature.games.roman

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
 * ViewModel игры «Римские числа»: таймер, очки, перезапуск будильника
 * и тонкая обёртка над чистой логикой [RomanGame].
 *
 * Очки: −10 за каждую ошибку; при победе добавляется
 * (600 − прошло_секунд) × сложность, как в игре «Калькулятор».
 */
class RomanGameViewModel(application: Application) : AndroidViewModel(application) {
    private var timeStarted: Long = 0
    private var timeCurrent: Long = 0

    /** Прошедшее время игры строкой в формате «м.сс», обновляется раз в секунду. */
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

    /** Текущий вопрос, который нужно показать игроку. */
    val question: MutableLiveData<RomanQuestion> = MutableLiveData()

    /** Число верных ответов, данных к текущему моменту. */
    val correctCount: MutableLiveData<Int> = MutableLiveData(0)

    private lateinit var game: RomanGame

    /** Сколько верных ответов нужно для победы (4/5/6 по сложности). */
    val targetCorrect: Int
        get() = game.targetCorrect

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
     * Загружает будильник из БД, чтобы его можно было перезапустить,
     * если игрок свернёт игру, не пройдя её.
     *
     * @param alarmId id будильника из аргумента «alarm id»
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
     * @param time значение аргумента «start time»; 0 означает пробный запуск —
     * тогда берётся текущее время
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
     * @param difficulty значение аргумента «difficulty» (1..3)
     */
    fun setDifficultyLevel(difficulty: Int) {
        this.difficulty = difficulty
    }

    /**
     * Однократно создаёт партию [RomanGame] (после установки сложности)
     * и публикует первый вопрос. Повторные вызовы (например, после
     * пересоздания фрагмента) игнорируются — партия продолжается.
     */
    fun startGame() {
        if (!::game.isInitialized) {
            game = RomanGame(difficulty)
            question.value = game.currentQuestion
            correctCount.value = game.correctCount
        }
    }

    /**
     * Обрабатывает выбор игроком варианта ответа: передаёт его в [RomanGame],
     * при ошибке снимает 10 очков и публикует новый вопрос и прогресс.
     *
     * @param optionIndex индекс выбранного варианта (0..3)
     * @return исход ответа (правильно / ошибка / победа)
     */
    fun answer(optionIndex: Int): AnswerResult {
        val answerResult = game.answer(optionIndex)
        if (answerResult == AnswerResult.WRONG) score -= 10
        question.value = game.currentQuestion
        correctCount.value = game.correctCount
        return answerResult
    }

    /**
     * Подсчитывает финальные очки: к накопленным штрафам добавляется
     * (600 − прошло_секунд) × сложность.
     *
     * @return итоговый счёт игры
     */
    fun finishScore(): Int {
        score += ((600 - ((System.currentTimeMillis() - timeStarted) / 1000)) * difficulty).toInt()
        return score
    }

    /** Помечает игру пройденной, чтобы будильник не перезапускался в onPause. */
    fun setPositiveResult() {
        result = true
    }

    override fun onCleared() {
        handler.removeCallbacks(runnable)
        super.onCleared()
    }
}
