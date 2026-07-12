package com.example.smartalarm.feature.games.fifteen

/**
 * Параметры одного уровня сложности «Пятнашек».
 *
 * @property level уровень сложности (1..3)
 * @property boardSize длина стороны поля (3 — поле 3x3, 4 — поле 4x4)
 * @property shuffleMoves число случайных ходов перемешивания K; оно же —
 * верхняя граница минимального числа ходов до решения
 */
data class FifteenDifficulty(
    val level: Int,
    val boardSize: Int,
    val shuffleMoves: Int
) {
    companion object {
        /**
         * Возвращает параметры для уровня сложности [level].
         *
         * Соответствие: 1 — поле 3x3, K = 15; 2 — поле 3x3, K = 40; 3 — поле 4x4, K = 60.
         * Значения вне диапазона 1..3 приводятся к ближайшей границе.
         */
        fun forLevel(level: Int): FifteenDifficulty = when (level.coerceIn(1, 3)) {
            1 -> FifteenDifficulty(1, 3, 15)
            2 -> FifteenDifficulty(2, 3, 40)
            else -> FifteenDifficulty(3, 4, 60)
        }
    }
}
