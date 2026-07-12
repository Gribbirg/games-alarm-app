package com.example.smartalarm.feature.games.oddoneout

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
 * ViewModel экрана игры «Найди лишнее».
 *
 * Тонкий слой над чистой логикой [OddOneOutGame]: ведёт таймер игры
 * (строка вида «м.сс» в [timeCurrentString]), загружает будильник из БД
 * и перезапускает его через [AlarmCreateRepository], если игра не пройдена,
 * а также считает итоговый счёт по формуле [computeOddOneOutScore].
 */
class OddOneOutGameViewModel(application: Application) : AndroidViewModel(application) {
    private var timeStarted: Long = 0
    private var timeCurrent: Long = 0

    /** Время с начала игры в формате «м.сс» (минуты без ведущего нуля). */
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

    /** Чистая логика текущей игры; создаётся в [startGame]. */
    lateinit var game: OddOneOutGame
        private set

    /** Сложность игры 1..3, задаётся в [setDifficultyLevel]. */
    var difficulty = 1
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

    /**
     * Задаёт момент старта игры: [time] из аргументов фрагмента или
     * текущее время, если пришёл 0 (проба).
     */
    fun setStartTime(time: Long) {
        timeStarted = if (time != 0L)
            time
        else
            System.currentTimeMillis()
    }

    /** Запоминает сложность (1..3) для генерации игры и подсчёта очков. */
    fun setDifficultyLevel(difficulty: Int) {
        this.difficulty = difficulty
    }

    /** Создаёт новую игру [OddOneOutGame] с текущей сложностью. */
    fun startGame() {
        game = OddOneOutGame(difficulty, Random.Default)
    }

    /** Передаёт нажатие на ячейку [index] в игру и возвращает её вердикт. */
    fun onCellClicked(index: Int): ClickResult = game.onCellClicked(index)

    /**
     * Итоговый счёт: −10 за каждую ошибку плюс
     * (600 − прошло секунд) × сложность.
     */
    fun finishScore(): Int = computeOddOneOutScore(
        game.mistakes,
        (System.currentTimeMillis() - timeStarted) / 1000,
        difficulty
    )

    /** Помечает игру пройденной — будильник перезапускать не нужно. */
    fun setPositiveResult() {
        result = true
    }

    override fun onCleared() {
        handler.removeCallbacks(runnable)
        super.onCleared()
    }
}
