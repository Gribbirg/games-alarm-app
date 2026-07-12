package com.example.smartalarm.feature.games.fifteen

import kotlin.random.Random

/**
 * Один ход на поле «Пятнашек»: плитка передвинулась с клетки [fromIndex]
 * на клетку [toIndex] (бывшую пустую).
 *
 * Обратный ход — вызов [FifteenBoard.moveTile] с аргументом [toIndex].
 *
 * @property fromIndex индекс клетки, где плитка стояла до хода
 * @property toIndex индекс клетки, куда плитка встала (пустая клетка до хода)
 */
data class FifteenMove(val fromIndex: Int, val toIndex: Int)

/**
 * Игровое поле «Пятнашек» размера [size] x [size]. Чистая логика без Android-зависимостей.
 *
 * Клетки нумеруются построчно индексами `0 until size * size`.
 * Плитки хранят значения `1..size*size-1`, пустая клетка — значение `0`.
 * Собранное состояние: `1, 2, ..., size*size-1, 0` (пустая клетка в правом нижнем углу).
 * Новое поле создаётся собранным.
 *
 * @property size длина стороны поля (например, 3 для поля 3x3)
 */
class FifteenBoard(val size: Int) {

    init {
        require(size >= 2) { "Размер поля должен быть не меньше 2, получен $size" }
    }

    /** Общее число клеток поля (`size * size`). */
    val cellCount: Int = size * size

    private val tiles: IntArray = IntArray(cellCount) { (it + 1) % cellCount }

    /** Индекс пустой клетки. */
    var emptyIndex: Int = cellCount - 1
        private set

    /**
     * Возвращает значение плитки в клетке [index] (`0` — пустая клетка).
     */
    fun tileAt(index: Int): Int = tiles[index]

    /**
     * Возвращает неизменяемый снимок текущего состояния поля построчно.
     */
    fun snapshot(): List<Int> = tiles.toList()

    /**
     * Проверяет, собрано ли поле (плитки по порядку `1..N`, пустая клетка в конце).
     */
    fun isSolved(): Boolean = tiles.withIndex().all { (i, v) -> v == (i + 1) % cellCount }

    /**
     * Проверяет, можно ли сдвинуть плитку в клетке [index]:
     * плитка должна быть соседней с пустой клеткой по горизонтали или вертикали.
     */
    fun canMove(index: Int): Boolean {
        if (index !in 0 until cellCount || index == emptyIndex) return false
        val row = index / size
        val col = index % size
        val emptyRow = emptyIndex / size
        val emptyCol = emptyIndex % size
        return (row == emptyRow && kotlin.math.abs(col - emptyCol) == 1) ||
                (col == emptyCol && kotlin.math.abs(row - emptyRow) == 1)
    }

    /**
     * Возвращает индексы всех плиток, которые сейчас можно сдвинуть
     * (соседи пустой клетки), в порядке возрастания индекса.
     */
    fun movableIndices(): List<Int> = (0 until cellCount).filter { canMove(it) }

    /**
     * Сдвигает плитку из клетки [index] в пустую клетку.
     *
     * @return `true`, если ход допустим и выполнен; `false` — состояние поля не изменилось
     */
    fun moveTile(index: Int): Boolean {
        if (!canMove(index)) return false
        tiles[emptyIndex] = tiles[index]
        tiles[index] = 0
        emptyIndex = index
        return true
    }

    /**
     * Возвращает поле в собранное состояние.
     */
    fun reset() {
        for (i in 0 until cellCount) tiles[i] = (i + 1) % cellCount
        emptyIndex = cellCount - 1
    }

    /**
     * Перемешивает поле [movesCount] случайными корректными ходами из собранного состояния.
     *
     * Свойства перемешивания:
     * - каждый ход допустим, поэтому поле гарантированно решаемо — обратная
     *   последовательность ходов возвращает его в собранное состояние не более
     *   чем за [movesCount] ходов;
     * - ход, немедленно отменяющий предыдущий, никогда не выбирается;
     * - если после всех ходов поле случайно оказалось собранным, перемешивание
     *   начинается заново — результат никогда не бывает собранным.
     *
     * @param movesCount число случайных ходов (не меньше 1)
     * @param random источник случайности (передайте [Random] с seed для воспроизводимости)
     * @return список выполненных ходов в порядке выполнения
     */
    fun shuffle(movesCount: Int, random: Random): List<FifteenMove> {
        require(movesCount >= 1) { "Число ходов перемешивания должно быть не меньше 1" }
        var moves: List<FifteenMove>
        do {
            reset()
            moves = performRandomMoves(movesCount, random)
        } while (isSolved())
        return moves
    }

    private fun performRandomMoves(movesCount: Int, random: Random): List<FifteenMove> {
        val moves = ArrayList<FifteenMove>(movesCount)
        var undoIndex = -1
        repeat(movesCount) {
            val candidates = movableIndices().filter { it != undoIndex }
            val from = candidates[random.nextInt(candidates.size)]
            val to = emptyIndex
            moveTile(from)
            moves.add(FifteenMove(from, to))
            // Сдвинутая плитка теперь стоит в клетке to; её ход назад отменил бы предыдущий.
            undoIndex = to
        }
        return moves
    }
}
