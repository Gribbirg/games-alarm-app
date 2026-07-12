package com.example.smartalarm.feature.games.digits

import kotlin.random.Random

/**
 * Чистая логика игры «Запомни число»: параметры раундов по сложности,
 * генерация числа и проверка ответа. Не зависит от Android — тестируется на JVM.
 */
object DigitsLogic {

    /** Количество раундов в игре. */
    const val TOTAL_ROUNDS = 3

    /**
     * Длины чисел по раундам для заданной сложности.
     *
     * @param difficulty сложность 1..3 (иное значение трактуется как 1)
     * @return список из [TOTAL_ROUNDS] длин: 4-5-6 / 5-6-7 / 6-7-8
     */
    fun roundLengths(difficulty: Int): List<Int> = when (difficulty) {
        2 -> listOf(5, 6, 7)
        3 -> listOf(6, 7, 8)
        else -> listOf(4, 5, 6)
    }

    /**
     * Длина числа в конкретном раунде.
     *
     * @param difficulty сложность 1..3
     * @param round номер раунда, начиная с 0 (значение вне диапазона прижимается к границам)
     * @return количество цифр в числе этого раунда
     */
    fun numberLength(difficulty: Int, round: Int): Int =
        roundLengths(difficulty)[round.coerceIn(0, TOTAL_ROUNDS - 1)]

    /**
     * Время показа числа в миллисекундах для заданной сложности:
     * 3000 / 2500 / 2000 мс (иная сложность трактуется как 1).
     *
     * @param difficulty сложность 1..3
     * @return длительность показа в миллисекундах
     */
    fun showTimeMillis(difficulty: Int): Long = when (difficulty) {
        2 -> 2500L
        3 -> 2000L
        else -> 3000L
    }

    /**
     * Генерирует число как строку из [length] цифр без ведущего нуля:
     * первая цифра 1..9, остальные 0..9.
     *
     * @param length требуемое количество цифр (не меньше 1)
     * @param random источник случайности (передаётся для детерминизма в тестах)
     * @return строка из [length] цифр
     */
    fun generateNumber(length: Int, random: Random): String {
        require(length >= 1) { "Number length must be at least 1, got $length" }
        return buildString {
            append(random.nextInt(1, 10))
            repeat(length - 1) {
                append(random.nextInt(0, 10))
            }
        }
    }

    /**
     * Проверяет ответ игрока: строки должны совпадать посимвольно,
     * поэтому пустой ввод и ввод с ведущими нулями («0123» вместо «123») — неверные.
     *
     * @param number загаданное число (строка цифр)
     * @param answer введённый игроком ответ
     * @return true, если ответ верный
     */
    fun isAnswerCorrect(number: String, answer: String): Boolean = answer == number

    /**
     * Является ли раунд последним.
     *
     * @param round номер раунда, начиная с 0
     * @return true для последнего раунда игры
     */
    fun isLastRound(round: Int): Boolean = round >= TOTAL_ROUNDS - 1
}
