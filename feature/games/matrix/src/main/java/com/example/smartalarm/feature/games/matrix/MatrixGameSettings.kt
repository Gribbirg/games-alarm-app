package com.example.smartalarm.feature.games.matrix

/**
 * Параметры игры «Запомни клетки», вычисленные из уровня сложности.
 *
 * Игра идёт раундами: приложение одновременно подсвечивает набор клеток
 * квадратного поля, затем гасит их, и игрок отмечает по памяти те же клетки
 * (порядок не важен). С каждым раундом набор растёт на одну клетку,
 * но не больше [maxCellsCount].
 *
 * @property difficulty уровень сложности 1..3 (используется в формуле итогового счёта)
 * @property gridSize сторона квадратного поля (поле [gridSize] × [gridSize])
 * @property roundsCount число раундов до победы
 * @property startCellsCount размер набора клеток в первом раунде
 * @property maxCellsCount потолок размера набора при росте по раундам
 * @property showTimeMs сколько миллисекунд набор горит в фазе показа
 */
data class MatrixGameSettings(
    val difficulty: Int,
    val gridSize: Int,
    val roundsCount: Int,
    val startCellsCount: Int,
    val maxCellsCount: Int,
    val showTimeMs: Long
) {
    /** Общее число клеток поля. */
    val cellsTotal: Int
        get() = gridSize * gridSize

    /**
     * Размер набора клеток в раунде [round] (нумерация с 1):
     * `startCellsCount + (round − 1)`, но не больше [maxCellsCount].
     */
    fun cellsForRound(round: Int): Int =
        (startCellsCount + round - 1).coerceAtMost(maxCellsCount)

    companion object {
        /**
         * Возвращает настройки для уровня сложности [difficulty].
         *
         * Значения вне диапазона 1..3 приводятся к ближайшей границе:
         * - 1 — поле 3×3, 3 раунда, набор 3 → 5 клеток, показ 1500 мс;
         * - 2 — поле 4×4, 4 раунда, набор 5 → 8 клеток, показ 1800 мс;
         * - 3 — поле 5×5, 5 раундов, набор 7 → 10 клеток, показ 2000 мс.
         */
        fun forDifficulty(difficulty: Int): MatrixGameSettings =
            when (difficulty.coerceIn(1, 3)) {
                1 -> MatrixGameSettings(
                    difficulty = 1,
                    gridSize = 3,
                    roundsCount = 3,
                    startCellsCount = 3,
                    maxCellsCount = 5,
                    showTimeMs = 1500
                )

                2 -> MatrixGameSettings(
                    difficulty = 2,
                    gridSize = 4,
                    roundsCount = 4,
                    startCellsCount = 5,
                    maxCellsCount = 8,
                    showTimeMs = 1800
                )

                else -> MatrixGameSettings(
                    difficulty = 3,
                    gridSize = 5,
                    roundsCount = 5,
                    startCellsCount = 7,
                    maxCellsCount = 10,
                    showTimeMs = 2000
                )
            }
    }
}
