package com.example.smartalarm.feature.games.equation

/**
 * Арифметический оператор, используемый в уравнениях игры «Уравнение».
 *
 * Чистая логика без зависимостей от Android.
 *
 * @property symbol символ оператора, как он отображается игроку и в вариантах ответа.
 */
enum class Operator(val symbol: String) {
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("×"),
    DIVIDE("÷");

    /**
     * Применяет оператор к паре целых чисел.
     *
     * @param a левый операнд.
     * @param b правый операнд.
     * @return результат операции или `null`, если операция невыполнима в целых числах
     * (деление на ноль или деление с остатком).
     */
    fun apply(a: Int, b: Int): Int? = when (this) {
        PLUS -> a + b
        MINUS -> a - b
        MULTIPLY -> a * b
        DIVIDE -> if (b != 0 && a % b == 0) a / b else null
    }

    companion object {
        /**
         * Находит оператор по его символу.
         *
         * @param symbol символ оператора (см. [symbol]).
         * @return соответствующий оператор или `null`, если символ неизвестен.
         */
        fun fromSymbol(symbol: String): Operator? = entries.find { it.symbol == symbol }
    }
}
