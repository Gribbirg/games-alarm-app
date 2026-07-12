package com.example.smartalarm.feature.games.sorting

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
 * ViewModel игры «По порядку»: тонкий слой над чистой логикой [SortingGame] —
 * таймер, очки, перезапуск будильника при выходе из непройденной игры.
 *
 * Очки: ошибка — −10; за победу добавляется (600 − прошло_секунд) × сложность.
 */
class SortingGameViewModel(application: Application) : AndroidViewModel(application) {
    private var timeStarted: Long = 0
    private var timeCurrent: Long = 0

    /** Строка таймера в формате "м.сс", обновляется раз в секунду. */
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

    /** Текущая партия игры; создаётся в [startGame] и переживает поворот экрана. */
    lateinit var game: SortingGame
        private set

    private var score = 0

    /** Сложность игры 1..3. */
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
     * Загружает будильник из БД, чтобы его можно было перезапустить,
     * если игрок свернёт игру, не пройдя её.
     *
     * @param alarmId id будильника из аргументов фрагмента
     */
    fun getAlarm(alarmId: Long) {
        viewModelScope.launch {
            currentAlarm = alarmDbRepository.getAlarmWithGames(alarmId)
            currentAlarm.milisTime = timeStarted
        }
    }

    /**
     * Перезапускает будильник, если игра ещё не пройдена
     * (вызывается из onPause фрагмента вне режима пробы).
     */
    fun startNewAlarm() {
        if (!result) {
            Log.i("game", "Game not finished!")
            alarmCreateRepository.create(currentAlarm)
        }
    }

    /**
     * Устанавливает момент старта игры для таймера.
     *
     * @param time время старта в миллисекундах; 0 — использовать текущее время
     */
    fun setStartTime(time: Long) {
        timeStarted = if (time != 0L)
            time
        else
            System.currentTimeMillis()
    }

    /**
     * Создаёт партию для заданной сложности. Повторный вызов (например,
     * после поворота экрана) не пересоздаёт уже идущую партию.
     *
     * @param difficulty сложность 1..3
     */
    fun startGame(difficulty: Int) {
        this.difficulty = difficulty
        if (!::game.isInitialized)
            game = SortingGame(SortingNumbersGenerator.generate(difficulty))
    }

    /**
     * Обрабатывает нажатие числа [value]; при ошибке снимает 10 очков.
     *
     * @return результат нажатия из чистой логики
     */
    fun onNumberPressed(value: Int): PressResult {
        val pressResult = game.press(value)
        if (pressResult == PressResult.WRONG)
            score -= 10
        return pressResult
    }

    /**
     * Начисляет финальный бонус (600 − прошло_секунд) × сложность
     * и возвращает итоговые очки за партию.
     */
    fun finishScore(): Int {
        score += ((600 - ((System.currentTimeMillis() - timeStarted) / 1000)) * difficulty).toInt()
        return score
    }

    /**
     * Помечает игру пройденной, чтобы onPause не перезапустил будильник.
     */
    fun setPositiveResult() {
        result = true
    }
}
