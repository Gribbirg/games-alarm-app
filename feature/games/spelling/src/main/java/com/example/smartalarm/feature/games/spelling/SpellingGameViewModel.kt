package com.example.smartalarm.feature.games.spelling

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
 * ViewModel игры «Как пишется?»: таймер, очки и перезапуск будильника —
 * тонкий слой над чистой логикой [SpellingGame].
 */
class SpellingGameViewModel(application: Application) : AndroidViewModel(application) {
    private var timeStarted: Long = 0
    private var timeCurrent: Long = 0

    /** Текущее время игры в виде строки «м.сс». */
    var timeCurrentString: MutableLiveData<String> = MutableLiveData()

    /** Текущий вопрос — варианты написания одного слова. */
    val question: MutableLiveData<SpellingQuestion> = MutableLiveData()

    /** Прогресс игры: (дано верных ответов, нужно для победы). */
    val progress: MutableLiveData<Pair<Int, Int>> = MutableLiveData()

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

    private var game: SpellingGame? = null

    private var score = 0

    /** Уровень сложности игры (1..3). */
    var difficulty = 0
        private set

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
     * Создаёт игру для уровня сложности, если она ещё не создана
     * (при повороте экрана игра сохраняется).
     *
     * @param difficulty уровень сложности 1..3
     * @param random источник случайности
     */
    fun initGame(difficulty: Int, random: Random = Random.Default) {
        if (game != null) return
        this.difficulty = difficulty
        val newGame = SpellingGame(
            SpellingData.entriesForDifficulty(difficulty),
            SpellingGame.targetCorrectFor(difficulty),
            random
        )
        game = newGame
        question.value = newGame.currentQuestion
        progress.value = newGame.correctCount to newGame.targetCorrect
    }

    /**
     * Обрабатывает выбор варианта игроком: обновляет очки, прогресс
     * и текущий вопрос.
     *
     * @param optionIndex индекс выбранного варианта
     * @return результат ответа
     */
    fun answer(optionIndex: Int): AnswerResult {
        val currentGame = game ?: return AnswerResult.WRONG
        val answerResult = currentGame.answer(optionIndex)
        if (answerResult == AnswerResult.WRONG)
            score -= SpellingGame.MISTAKE_PENALTY
        if (answerResult != AnswerResult.WIN)
            question.value = currentGame.currentQuestion
        progress.value = currentGame.correctCount to currentGame.targetCorrect
        return answerResult
    }

    /**
     * Загружает будильник из БД, чтобы его можно было перезапустить,
     * если игра не пройдена.
     *
     * @param alarmId id будильника
     */
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

    /**
     * Запоминает время начала игры.
     *
     * @param time время старта в миллисекундах; 0 — начать отсчёт с текущего
     * момента (пробный запуск)
     */
    fun setStartTime(time: Long) {
        timeStarted = if (time != 0L)
            time
        else
            System.currentTimeMillis()
    }

    /**
     * Начисляет финальный бонус и возвращает итоговый счёт игры.
     *
     * @return итоговые очки
     */
    fun finishScore(): Int {
        score += SpellingGame.finishBonus(
            (System.currentTimeMillis() - timeStarted) / 1000,
            difficulty
        )
        return score
    }

    /** Отмечает игру пройденной — будильник перезапускать не нужно. */
    fun setPositiveResult() {
        result = true
    }
}
