package com.example.smartalarm.feature.games.maze

import kotlin.random.Random

/**
 * Направление движения по решётке лабиринта.
 *
 * @property dRow смещение по строкам при шаге в этом направлении
 * @property dCol смещение по столбцам при шаге в этом направлении
 */
enum class Direction(val dRow: Int, val dCol: Int) {
    UP(-1, 0),
    DOWN(1, 0),
    LEFT(0, -1),
    RIGHT(0, 1);

    /** Противоположное направление (для проверки согласованности стен). */
    val opposite: Direction
        get() = when (this) {
            UP -> DOWN
            DOWN -> UP
            LEFT -> RIGHT
            RIGHT -> LEFT
        }
}

/**
 * Модель лабиринта на прямоугольной решётке клеток.
 *
 * Стены хранятся в двух общих массивах (горизонтальные — между строками,
 * вертикальные — между столбцами), поэтому стена между двумя соседними
 * клетками по построению одна и та же с обеих сторон. Внешние границы
 * лабиринта — всегда стены, убрать их нельзя.
 *
 * Класс не зависит от Android: генерация и проверки — чистый Kotlin,
 * случайность передаётся параметром [Random].
 *
 * @property rows число строк (клеток по вертикали), больше нуля
 * @property cols число столбцов (клеток по горизонтали), больше нуля
 */
class Maze(val rows: Int, val cols: Int) {

    init {
        require(rows > 0 && cols > 0) { "Размеры лабиринта должны быть положительными" }
    }

    /** horizontalWalls[r][c] — стена НАД клеткой (r, c); строка rows — нижняя граница. */
    private val horizontalWalls = Array(rows + 1) { BooleanArray(cols) { true } }

    /** verticalWalls[r][c] — стена СЛЕВА от клетки (r, c); столбец cols — правая граница. */
    private val verticalWalls = Array(rows) { BooleanArray(cols + 1) { true } }

    /** Возвращает true, если клетка (row, col) находится внутри лабиринта. */
    fun isInside(row: Int, col: Int): Boolean =
        row in 0 until rows && col in 0 until cols

    /**
     * Есть ли стена у клетки (row, col) со стороны [direction].
     *
     * @throws IllegalArgumentException если клетка вне лабиринта
     */
    fun hasWall(row: Int, col: Int, direction: Direction): Boolean {
        requireInside(row, col)
        return when (direction) {
            Direction.UP -> horizontalWalls[row][col]
            Direction.DOWN -> horizontalWalls[row + 1][col]
            Direction.LEFT -> verticalWalls[row][col]
            Direction.RIGHT -> verticalWalls[row][col + 1]
        }
    }

    /**
     * Убирает стену между клеткой (row, col) и её соседом в направлении [direction].
     *
     * @throws IllegalArgumentException если клетка вне лабиринта или стена внешняя
     *   (у соседа нет клетки — границу лабиринта убирать нельзя)
     */
    fun removeWall(row: Int, col: Int, direction: Direction) {
        requireInside(row, col)
        require(isInside(row + direction.dRow, col + direction.dCol)) {
            "Нельзя убрать внешнюю стену лабиринта"
        }
        when (direction) {
            Direction.UP -> horizontalWalls[row][col] = false
            Direction.DOWN -> horizontalWalls[row + 1][col] = false
            Direction.LEFT -> verticalWalls[row][col] = false
            Direction.RIGHT -> verticalWalls[row][col + 1] = false
        }
    }

    /**
     * Можно ли шагнуть из клетки (row, col) в направлении [direction]:
     * сосед существует и стены между ними нет.
     */
    fun canMove(row: Int, col: Int, direction: Direction): Boolean {
        val nRow = row + direction.dRow
        val nCol = col + direction.dCol
        return isInside(nRow, nCol) && !hasWall(row, col, direction)
    }

    private fun requireInside(row: Int, col: Int) {
        require(isInside(row, col)) { "Клетка ($row, $col) вне лабиринта ${rows}x$cols" }
    }

    companion object {

        /**
         * Генерирует идеальный лабиринт (без циклов, все клетки достижимы)
         * алгоритмом recursive backtracker в итеративной форме.
         *
         * Начиная со стартовой клетки (0, 0), алгоритм случайно углубляется
         * в непосещённых соседей, убирая стены, и откатывается по стеку,
         * когда соседей не осталось. Результат — остовное дерево решётки:
         * ровно rows*cols − 1 проходов и единственный путь между любыми
         * двумя клетками.
         *
         * @param rows число строк
         * @param cols число столбцов
         * @param random источник случайности; один и тот же seed даёт
         *   одинаковый лабиринт (детерминизм)
         */
        fun generate(rows: Int, cols: Int, random: Random = Random.Default): Maze {
            val maze = Maze(rows, cols)
            val visited = Array(rows) { BooleanArray(cols) }
            val stack = ArrayDeque<Pair<Int, Int>>()

            visited[0][0] = true
            stack.addLast(0 to 0)

            while (stack.isNotEmpty()) {
                val (row, col) = stack.last()
                val candidates = Direction.entries.filter { direction ->
                    val nRow = row + direction.dRow
                    val nCol = col + direction.dCol
                    maze.isInside(nRow, nCol) && !visited[nRow][nCol]
                }
                if (candidates.isEmpty()) {
                    stack.removeLast()
                } else {
                    val direction = candidates.random(random)
                    maze.removeWall(row, col, direction)
                    val nRow = row + direction.dRow
                    val nCol = col + direction.dCol
                    visited[nRow][nCol] = true
                    stack.addLast(nRow to nCol)
                }
            }
            return maze
        }
    }
}
