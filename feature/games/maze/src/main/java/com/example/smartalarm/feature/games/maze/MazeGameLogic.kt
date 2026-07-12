package com.example.smartalarm.feature.games.maze

/** Результат попытки хода игрока. */
enum class MoveResult {
    /** Игрок переместился в соседнюю клетку. */
    MOVED,

    /** Ход невозможен: в этом направлении стена (или игра уже завершена). */
    BLOCKED,

    /** Игрок достиг выхода — игра пройдена. */
    FINISHED
}

/**
 * Чистая игровая логика «Лабиринта»: позиция игрока, ходы и условие победы.
 *
 * Игрок стартует в левой верхней клетке (0, 0); выход — правая нижняя
 * клетка лабиринта. Класс не зависит от Android.
 *
 * @property maze лабиринт, по которому идёт игрок
 */
class MazeGameLogic(val maze: Maze) {

    /** Текущая строка игрока. */
    var playerRow = 0
        private set

    /** Текущий столбец игрока. */
    var playerCol = 0
        private set

    /** Строка клетки выхода (нижний ряд). */
    val exitRow = maze.rows - 1

    /** Столбец клетки выхода (правый столбец). */
    val exitCol = maze.cols - 1

    /** true, когда игрок стоит на клетке выхода. */
    val isFinished: Boolean
        get() = playerRow == exitRow && playerCol == exitCol

    /**
     * Пытается сдвинуть игрока в направлении [direction].
     *
     * Если игра уже завершена, позиция не меняется и возвращается
     * [MoveResult.FINISHED]. Если в направлении стена (или граница) —
     * позиция не меняется, возвращается [MoveResult.BLOCKED].
     * Иначе игрок делает шаг; если он оказался на выходе — возвращается
     * [MoveResult.FINISHED], в остальных случаях [MoveResult.MOVED].
     */
    fun tryMove(direction: Direction): MoveResult {
        if (isFinished) return MoveResult.FINISHED
        if (!maze.canMove(playerRow, playerCol, direction)) return MoveResult.BLOCKED
        playerRow += direction.dRow
        playerCol += direction.dCol
        return if (isFinished) MoveResult.FINISHED else MoveResult.MOVED
    }

    companion object {

        /**
         * Размер стороны лабиринта (в клетках) для уровня сложности:
         * 1 → 7×7, 2 → 9×9, 3 → 11×11; любое другое значение → 7×7.
         */
        fun sizeForDifficulty(difficulty: Int): Int = when (difficulty) {
            1 -> 7
            2 -> 9
            3 -> 11
            else -> 7
        }
    }
}
