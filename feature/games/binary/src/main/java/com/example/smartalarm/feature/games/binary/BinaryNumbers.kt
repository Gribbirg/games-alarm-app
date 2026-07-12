package com.example.smartalarm.feature.games.binary

/**
 * Чистый (без Android) конвертер двоичная ↔ десятичная запись для
 * положительных чисел.
 *
 * Двоичная запись — каноническая: только символы `0`/`1` и без ведущих
 * нулей (старший разряд всегда `1`).
 */
object BinaryNumbers {

    /**
     * Возвращает двоичную запись числа без ведущих нулей.
     *
     * @param number положительное число (>= 1)
     * @return строка из `0`/`1`, начинающаяся с `1` (например, 13 → `1101`)
     * @throws IllegalArgumentException если [number] меньше 1
     */
    fun toBinary(number: Int): String {
        require(number >= 1) { "Число должно быть положительным: $number" }
        return number.toString(radix = 2)
    }

    /**
     * Разбирает каноническую двоичную запись.
     *
     * @param binary строка только из `0`/`1` без ведущих нулей
     * @return значение записи (например, `1101` → 13)
     * @throws IllegalArgumentException если строка пуста, содержит
     * посторонние символы или начинается с `0`
     */
    fun fromBinary(binary: String): Int {
        require(binary.isNotEmpty()) { "Пустая двоичная запись" }
        require(binary.all { it == '0' || it == '1' }) {
            "Недопустимые символы в двоичной записи: \"$binary\""
        }
        require(binary.first() == '1') {
            "Ведущие нули в двоичной записи: \"$binary\""
        }
        return binary.toInt(radix = 2)
    }
}
