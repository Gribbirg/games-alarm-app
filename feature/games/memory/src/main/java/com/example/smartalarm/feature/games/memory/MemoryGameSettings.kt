package com.example.smartalarm.feature.games.memory

/**
 * Параметры игры «Повтори узор», вычисленные из уровня сложности.
 *
 * Игра идёт раундами: в каждом раунде приложение подсвечивает
 * последовательность ячеек поля 3×3, а игрок повторяет её нажатиями.
 * После каждого раунда последовательность растёт на одну ячейку.
 *
 * @property difficulty уровень сложности 1..3 (используется в формуле итогового счёта)
 * @property startLength длина последовательности в первом раунде
 * @property roundsCount число раундов до победы
 * @property showCellMs сколько миллисекунд ячейка горит при показе последовательности
 * @property betweenCellsMs пауза в миллисекундах между подсветкой соседних ячеек
 */
data class MemoryGameSettings(
    val difficulty: Int,
    val startLength: Int,
    val roundsCount: Int,
    val showCellMs: Long,
    val betweenCellsMs: Long
) {
    /** Длина последовательности в последнем раунде. */
    val finalLength: Int
        get() = startLength + roundsCount - 1

    companion object {
        /**
         * Возвращает настройки для уровня сложности [difficulty].
         *
         * Значения вне диапазона 1..3 приводятся к ближайшей границе:
         * - 1 — старт с 3 ячеек, 3 раунда (финал — 5 ячеек), показ 650/300 мс;
         * - 2 — старт с 4 ячеек, 4 раунда (финал — 7 ячеек), показ 500/250 мс;
         * - 3 — старт с 5 ячеек, 5 раундов (финал — 9 ячеек), показ 400/200 мс.
         */
        fun forDifficulty(difficulty: Int): MemoryGameSettings =
            when (difficulty.coerceIn(1, 3)) {
                1 -> MemoryGameSettings(
                    difficulty = 1,
                    startLength = 3,
                    roundsCount = 3,
                    showCellMs = 650,
                    betweenCellsMs = 300
                )

                2 -> MemoryGameSettings(
                    difficulty = 2,
                    startLength = 4,
                    roundsCount = 4,
                    showCellMs = 500,
                    betweenCellsMs = 250
                )

                else -> MemoryGameSettings(
                    difficulty = 3,
                    startLength = 5,
                    roundsCount = 5,
                    showCellMs = 400,
                    betweenCellsMs = 200
                )
            }
    }
}
