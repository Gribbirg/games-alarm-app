package com.example.smartalarm.feature.games.targetsum

/**
 * Настройки игры «Набери сумму», вычисленные из уровня сложности.
 *
 * @property difficulty уровень сложности 1..3 (уже приведённый к диапазону)
 * @property gridSize число ячеек-чисел на поле
 * @property solutionSize размер подмножества, из которого строится цель
 * @property minNumber минимальное значение числа в ячейке (включительно)
 * @property maxNumber максимальное значение числа в ячейке (включительно)
 * @property columns число столбцов сетки
 * @property roundsCount сколько раундов нужно пройти до победы
 */
data class TargetSumSettings(
    val difficulty: Int,
    val gridSize: Int,
    val solutionSize: Int,
    val minNumber: Int,
    val maxNumber: Int,
    val columns: Int,
    val roundsCount: Int
) {
    companion object {
        /**
         * Возвращает настройки для уровня сложности [difficulty] (1..3).
         *
         * Значение вне диапазона приводится к ближайшей границе:
         * - 1 — 6 чисел (решение из 3), значения 1..15, 2 раунда;
         * - 2 — 9 чисел (решение из 4), значения 1..30, 2 раунда;
         * - 3 — 12 чисел (решение из 5), значения 1..50, 3 раунда.
         */
        fun forDifficulty(difficulty: Int): TargetSumSettings =
            when (difficulty.coerceIn(1, 3)) {
                1 -> TargetSumSettings(
                    difficulty = 1,
                    gridSize = 6,
                    solutionSize = 3,
                    minNumber = 1,
                    maxNumber = 15,
                    columns = 3,
                    roundsCount = 2
                )

                2 -> TargetSumSettings(
                    difficulty = 2,
                    gridSize = 9,
                    solutionSize = 4,
                    minNumber = 1,
                    maxNumber = 30,
                    columns = 3,
                    roundsCount = 2
                )

                else -> TargetSumSettings(
                    difficulty = 3,
                    gridSize = 12,
                    solutionSize = 5,
                    minNumber = 1,
                    maxNumber = 50,
                    columns = 3,
                    roundsCount = 3
                )
            }
    }
}
