package com.example.smartalarm.feature.games.fifteen

import kotlin.random.Random

/**
 * Партия «Пятнашек»: перемешанное поле, счётчик ходов игрока и бонус за экономность.
 * Чистая логика без Android-зависимостей.
 *
 * Поле создаётся собранным и сразу перемешивается [FifteenDifficulty.shuffleMoves]
 * случайными корректными ходами (см. [FifteenBoard.shuffle]) — решаемость гарантирована,
 * и сразу после создания партия никогда не бывает выигранной.
 *
 * @property difficulty параметры сложности партии
 * @param random источник случайности для перемешивания
 */
class FifteenGame(
    val difficulty: FifteenDifficulty,
    random: Random = Random.Default
) {
    /** Игровое поле партии. */
    val board: FifteenBoard = FifteenBoard(difficulty.boardSize)

    /** Ходы перемешивания в порядке выполнения (обратная последовательность решает поле). */
    val shuffleMovesPerformed: List<FifteenMove> = board.shuffle(difficulty.shuffleMoves, random)

    /** Число выполненных игроком ходов (неудачные нажатия не считаются). */
    var moveCount: Int = 0
        private set

    /** `true`, когда поле собрано и партия выиграна. */
    val isWon: Boolean
        get() = board.isSolved()

    /**
     * Сдвигает плитку в клетке [index], если она соседняя с пустой.
     *
     * @return `true`, если ход выполнен (счётчик ходов увеличен), иначе `false`
     */
    fun moveTile(index: Int): Boolean {
        if (!board.moveTile(index)) return false
        moveCount++
        return true
    }

    /**
     * Бонус к очкам за близость к минимальному числу ходов.
     *
     * Перемешивание из K ходов гарантирует решение не более чем за K ходов,
     * поэтому бонус равен `max(0, 2K - число ходов игрока)`: решение ровно
     * за K ходов даёт K бонусных очков, за 2K ходов и более — ноль.
     */
    fun moveBonus(): Int = maxOf(0, 2 * difficulty.shuffleMoves - moveCount)
}
