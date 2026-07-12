package com.example.smartalarm.feature.games.matrix

import kotlin.random.Random

/**
 * Результат обработки отметки игрока на клетке поля.
 */
enum class MatrixTapResult {
    /** Клетка из набора отмечена верно, но набор ещё не собран целиком. */
    CORRECT,

    /** Клетка уже была отмечена ранее: ничего не меняется, штрафа нет. */
    ALREADY_MARKED,

    /**
     * Отмечена клетка не из набора: начислен штраф, текущий раунд начинается
     * заново с новым случайным набором.
     */
    MISTAKE,

    /** Набор раунда собран целиком, впереди следующий раунд. */
    ROUND_COMPLETE,

    /** Собран набор последнего раунда — победа. */
    WIN
}

/**
 * Чистая логика игры «Запомни клетки» (без зависимостей от Android).
 *
 * В каждом раунде держит случайный набор уникальных клеток квадратного поля
 * ([targetCells]) и множество уже отмеченных игроком ([markedCells]),
 * проверяет отметки, переключает раунды и считает очки. Порядок отметок
 * не важен — этим игра отличается от «Повтори узор», где проверяется
 * последовательность. Вся случайность идёт через [random], поэтому
 * с фиксированным seed поведение детерминировано.
 *
 * Правила подсчёта очков — по образцу арифметики: каждая ошибка —
 * −[MISTAKE_PENALTY]; при победе прибавляется
 * `(600 − прошло_секунд) × сложность` (см. [finalScore]).
 *
 * @param difficulty уровень сложности 1..3 (вне диапазона приводится к границе)
 * @param random источник случайности; в тестах передаётся `Random(seed)`
 */
class MatrixGameLogic(
    difficulty: Int,
    private val random: Random = Random.Default
) {
    /** Настройки игры, вычисленные из уровня сложности. */
    val settings: MatrixGameSettings = MatrixGameSettings.forDifficulty(difficulty)

    /** Номер текущего раунда, начиная с 1. */
    var round: Int = 1
        private set

    /** Накопленные штрафы (0 или меньше); итог считает [finalScore]. */
    var score: Int = 0
        private set

    /** Число совершённых ошибок за игру. */
    var mistakes: Int = 0
        private set

    private var target: Set<Int> = generateTarget()
    private val marked = mutableSetOf<Int>()

    /**
     * Набор клеток текущего раунда — уникальные индексы
     * `0 until settings.cellsTotal` (слева направо, сверху вниз).
     */
    val targetCells: Set<Int>
        get() = target

    /** Клетки, уже верно отмеченные игроком в текущем раунде. */
    val markedCells: Set<Int>
        get() = marked.toSet()

    /**
     * Обрабатывает отметку игрока на клетке [cell]
     * (индекс `0 until settings.cellsTotal`).
     *
     * Уже отмеченная клетка — [MatrixTapResult.ALREADY_MARKED], без штрафа.
     * Клетка не из набора — [MatrixTapResult.MISTAKE]: штраф
     * −[MISTAKE_PENALTY] и перезапуск раунда с новым случайным набором
     * (его нужно показать заново). Верная клетка — [MatrixTapResult.CORRECT],
     * а если ею набор собран целиком — [MatrixTapResult.ROUND_COMPLETE]
     * или, в последнем раунде, [MatrixTapResult.WIN].
     */
    fun onCellTapped(cell: Int): MatrixTapResult {
        require(cell in 0 until settings.cellsTotal) { "Клетка $cell вне поля" }
        if (cell in marked)
            return MatrixTapResult.ALREADY_MARKED
        if (cell !in target) {
            mistakes++
            score -= MISTAKE_PENALTY
            restartRound()
            return MatrixTapResult.MISTAKE
        }
        marked += cell
        if (marked.size < target.size)
            return MatrixTapResult.CORRECT
        return if (round >= settings.roundsCount)
            MatrixTapResult.WIN
        else
            MatrixTapResult.ROUND_COMPLETE
    }

    /**
     * Переходит к следующему раунду: генерируется новый набор
     * (на одну клетку больше, до потолка настроек), отметки сбрасываются.
     *
     * @throws IllegalStateException если текущий раунд был последним
     */
    fun startNextRound() {
        check(round < settings.roundsCount) { "Игра уже завершена" }
        round++
        restartRound()
    }

    /**
     * Итоговый счёт: накопленные штрафы плюс
     * `(600 − [elapsedSeconds]) × сложность`.
     */
    fun finalScore(elapsedSeconds: Long): Int =
        score + ((600 - elapsedSeconds) * settings.difficulty).toInt()

    /** Сбрасывает отметки и генерирует новый набор для текущего раунда. */
    private fun restartRound() {
        marked.clear()
        target = generateTarget()
    }

    /** Случайный набор уникальных клеток размера `cellsForRound(round)`. */
    private fun generateTarget(): Set<Int> =
        (0 until settings.cellsTotal).toMutableList()
            .also { it.shuffle(random) }
            .take(settings.cellsForRound(round))
            .toSet()

    companion object {
        /** Штраф за одну ошибку. */
        const val MISTAKE_PENALTY = 10
    }
}
