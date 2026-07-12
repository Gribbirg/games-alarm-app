package com.example.smartalarm.feature.games.maze

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
 * ViewModel игры «Лабиринт»: таймер, очки, перезапуск будильника —
 * тонкий слой над чистой логикой [MazeGameLogic].
 *
 * Устроена по образцу CalcGameViewModel: секундный таймер публикует строку
 * вида «м.сс» в [timeCurrentString]; если игра не пройдена к моменту ухода
 * с экрана, будильник ставится заново через [AlarmCreateRepository].
 */
class MazeGameViewModel(application: Application) : AndroidViewModel(application) {

    private var timeStarted: Long = 0
    private var timeCurrent: Long = 0

    /** Строка таймера в формате «м.сс», обновляется раз в секунду. */
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

    /** Чистая игровая логика: лабиринт, позиция игрока, ходы. */
    lateinit var gameLogic: MazeGameLogic
        private set

    private var score = 0

    /** Уровень сложности 1..3, задаётся аргументом фрагмента. */
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

    /** Загружает будильник из БД, чтобы перезапустить его, если игра не пройдена. */
    fun getAlarm(alarmId: Long) {
        viewModelScope.launch {
            currentAlarm = alarmDbRepository.getAlarmWithGames(alarmId)
            currentAlarm.milisTime = timeStarted
        }
    }

    /** Ставит будильник заново, если игра ещё не пройдена (вызывается из onPause). */
    fun startNewAlarm() {
        if (!result) {
            Log.i("game", "Game not finished!")
            alarmCreateRepository.create(currentAlarm)
        }
    }

    /** Запоминает время старта игры (0 — игра запущена сейчас, не будильником). */
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
     * Генерирует лабиринт под текущую сложность и создаёт игровую логику.
     * Повторные вызовы (например, после поворота экрана) игнорируются —
     * лабиринт и позиция игрока сохраняются.
     */
    fun startGame() {
        if (this::gameLogic.isInitialized) return
        val size = MazeGameLogic.sizeForDifficulty(difficulty)
        gameLogic = MazeGameLogic(Maze.generate(size, size, Random.Default))
    }

    /** Пытается сдвинуть игрока; см. [MazeGameLogic.tryMove]. */
    fun tryMove(direction: Direction): MoveResult = gameLogic.tryMove(direction)

    /**
     * Итоговый счёт при победе: к набранным очкам прибавляется
     * (600 − прошедшие_секунды) × сложность.
     */
    fun finishScore(): Int {
        score += ((600 - ((System.currentTimeMillis() - timeStarted) / 1000)) * difficulty).toInt()
        return score
    }

    /** Штраф за попытку пройти сквозь стену: −10 очков. */
    fun mistake() {
        score -= 10
    }

    /** Помечает игру пройденной — будильник перезапускать не нужно. */
    fun setPositiveResult() {
        result = true
    }

    override fun onCleared() {
        handler.removeCallbacks(runnable)
        super.onCleared()
    }
}
