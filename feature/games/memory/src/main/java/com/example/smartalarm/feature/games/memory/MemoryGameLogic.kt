package com.example.smartalarm.feature.games.memory

import kotlin.random.Random

/**
 * Результат обработки нажатия игрока на ячейку поля.
 */
enum class TapResult {
    /** Нажатие верное, последовательность ещё не повторена до конца. */
    CORRECT,

    /** Нажатие неверное: начислен штраф, ввод раунда начинается заново. */
    MISTAKE,

    /** Последовательность раунда повторена целиком, впереди следующий раунд. */
    ROUND_COMPLETE,

    /** Повторена последовательность последнего раунда — победа. */
    WIN
}

/**
 * Чистая логика игры «Повтори узор» (без зависимостей от Android).
 *
 * Держит последовательность ячеек текущего раунда, проверяет нажатия игрока
 * по шагам, переключает раунды и считает очки. Вся случайность идёт через
 * [random], поэтому с фиксированным seed поведение детерминировано.
 *
 * Правила подсчёта очков — по образцу арифметики:
 * каждая ошибка — −[MISTAKE_PENALTY]; при победе прибавляется
 * `(600 − прошло_секунд) × сложность` (см. [finalScore]).
 *
 * @param difficulty уровень сложности 1..3 (вне диапазона приводится к границе)
 * @param random источник случайности; в тестах передаётся `Random(seed)`
 */
class MemoryGameLogic(
    difficulty: Int,
    private val random: Random = Random.Default
) {
    /** Настройки игры, вычисленные из уровня сложности. */
    val settings: MemoryGameSettings = MemoryGameSettings.forDifficulty(difficulty)

    private val sequenceInternal = mutableListOf<Int>()

    /** Номер текущего раунда, начиная с 1. */
    var round: Int = 1
        private set

    /** Накопленные штрафы (0 или меньше); итог считает [finalScore]. */
    var score: Int = 0
        private set

    /** Число совершённых ошибок за игру. */
    var mistakes: Int = 0
        private set

    private var inputIndex = 0

    init {
        repeat(settings.startLength) { appendRandomCell() }
    }

    /**
     * Последовательность текущего раунда — индексы ячеек `0..8`
     * поля 3×3 (слева направо, сверху вниз).
     */
    val sequence: List<Int>
        get() = sequenceInternal.toList()

    /** Сколько ячеек игрок уже верно повторил в текущей попытке. */
    val inputProgress: Int
        get() = inputIndex

    /**
     * Обрабатывает нажатие игрока на ячейку [cell] (индекс `0..8`).
     *
     * Неверная ячейка — [TapResult.MISTAKE]: штраф −[MISTAKE_PENALTY]
     * и сброс прогресса ввода (последовательность нужно повторить с начала).
     * Верная — [TapResult.CORRECT], а если она была последней в
     * последовательности — [TapResult.ROUND_COMPLETE] или, в последнем
     * раунде, [TapResult.WIN].
     */
    fun onCellTapped(cell: Int): TapResult {
        if (cell != sequenceInternal[inputIndex]) {
            mistakes++
            score -= MISTAKE_PENALTY
            inputIndex = 0
            return TapResult.MISTAKE
        }
        inputIndex++
        if (inputIndex < sequenceInternal.size)
            return TapResult.CORRECT
        inputIndex = 0
        return if (round >= settings.roundsCount)
            TapResult.WIN
        else
            TapResult.ROUND_COMPLETE
    }

    /**
     * Переходит к следующему раунду: последовательность растёт на одну
     * случайную ячейку, прогресс ввода сбрасывается.
     *
     * @throws IllegalStateException если текущий раунд был последним
     */
    fun startNextRound() {
        check(round < settings.roundsCount) { "Игра уже завершена" }
        round++
        inputIndex = 0
        appendRandomCell()
    }

    /**
     * Итоговый счёт: накопленные штрафы плюс
     * `(600 − [elapsedSeconds]) × сложность`.
     */
    fun finalScore(elapsedSeconds: Long): Int =
        score + ((600 - elapsedSeconds) * settings.difficulty).toInt()

    /**
     * Добавляет в конец последовательности случайную ячейку, не совпадающую
     * с предыдущей, — иначе двойную подсветку одной ячейки легко не заметить.
     */
    private fun appendRandomCell() {
        var cell = random.nextInt(CELLS_COUNT)
        while (sequenceInternal.isNotEmpty() && cell == sequenceInternal.last())
            cell = random.nextInt(CELLS_COUNT)
        sequenceInternal += cell
    }

    companion object {
        /** Число ячеек поля 3×3. */
        const val CELLS_COUNT = 9

        /** Штраф за одну ошибку. */
        const val MISTAKE_PENALTY = 10
    }
}
