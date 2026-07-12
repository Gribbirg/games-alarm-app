package com.example.smartalarm.feature.games.chain

/**
 * Арифметическая операция одного шага цепочки устного счёта.
 *
 * Чистая логика без зависимостей от Android.
 *
 * @property symbol символ операции, как он показывается игроку («+», «−», «×»).
 */
enum class ChainOp(val symbol: String) {
    PLUS("+"),
    MINUS("−"),
    MULTIPLY("×");

    /**
     * Применяет операцию к текущему значению цепочки.
     *
     * @param value текущее значение цепочки.
     * @param operand операнд шага.
     * @return новое значение цепочки.
     */
    fun apply(value: Int, operand: Int): Int = when (this) {
        PLUS -> value + operand
        MINUS -> value - operand
        MULTIPLY -> value * operand
    }
}
