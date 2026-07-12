package com.example.smartalarm.feature.games.roman

/**
 * Конвертер между арабскими числами и канонической римской записью,
 * включая вычитательную нотацию (IV, IX, XL, XC, CD, CM).
 *
 * Чистая логика без зависимостей от Android.
 */
object RomanNumerals {

    /** Максимальное число, представимое в классической римской записи. */
    const val MAX_VALUE = 3999

    private val VALUES = listOf(
        1000 to "M", 900 to "CM", 500 to "D", 400 to "CD",
        100 to "C", 90 to "XC", 50 to "L", 40 to "XL",
        10 to "X", 9 to "IX", 5 to "V", 4 to "IV",
        1 to "I"
    )

    private val DIGITS = mapOf(
        'I' to 1, 'V' to 5, 'X' to 10, 'L' to 50,
        'C' to 100, 'D' to 500, 'M' to 1000
    )

    /**
     * Преобразует арабское число в каноническую римскую запись.
     *
     * @param number число в диапазоне 1..[MAX_VALUE]
     * @return римская запись, например 1994 -> "MCMXCIV"
     * @throws IllegalArgumentException если число вне диапазона
     */
    fun toRoman(number: Int): String {
        require(number in 1..MAX_VALUE) {
            "Число должно быть в диапазоне 1..$MAX_VALUE, получено: $number"
        }
        val builder = StringBuilder()
        var rest = number
        for ((value, symbol) in VALUES) {
            while (rest >= value) {
                builder.append(symbol)
                rest -= value
            }
        }
        return builder.toString()
    }

    /**
     * Разбирает каноническую римскую запись в арабское число.
     *
     * Разбор идёт по вычитательному правилу (символ меньшего достоинства
     * перед большим вычитается), после чего запись проверяется на
     * каноничность обратным преобразованием — «IIII», «VV», «IL» и т.п.
     * отвергаются.
     *
     * @param roman строка вида "MCMXCIV"
     * @return значение записи в диапазоне 1..[MAX_VALUE]
     * @throws IllegalArgumentException если строка пуста, содержит посторонние
     * символы или не является канонической римской записью
     */
    fun fromRoman(roman: String): Int {
        require(roman.isNotEmpty()) { "Пустая строка не является римской записью" }
        val values = roman.map { char ->
            DIGITS[char]
                ?: throw IllegalArgumentException("Недопустимый символ '$char' в записи \"$roman\"")
        }
        var result = 0
        for (i in values.indices) {
            if (i + 1 < values.size && values[i] < values[i + 1]) {
                result -= values[i]
            } else {
                result += values[i]
            }
        }
        require(result in 1..MAX_VALUE && toRoman(result) == roman) {
            "Неканоническая римская запись: \"$roman\""
        }
        return result
    }
}
