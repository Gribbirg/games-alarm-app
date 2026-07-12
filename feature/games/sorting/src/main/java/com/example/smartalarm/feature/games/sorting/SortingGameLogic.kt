package com.example.smartalarm.feature.games.sorting

import kotlin.random.Random

/**
 * Результат нажатия на число в игре «По порядку».
 */
enum class PressResult {
    /** Нажато правильное (наименьшее из оставшихся) число, игра продолжается. */
    CORRECT,

    /** Нажато не то число — начисляется ошибка, состояние поля не меняется. */
    WRONG,

    /** Нажато последнее правильное число — игра пройдена. */
    WIN
}

/**
 * Генератор наборов чисел для игры «По порядку».
 *
 * Параметры по сложностям:
 * - 1 — 6 уникальных чисел из диапазона 1..50;
 * - 2 — 9 уникальных чисел из диапазона 1..199;
 * - 3 — 12 уникальных чисел из диапазона -99..199 без нуля,
 *   из них ровно 3 отрицательных (гарантированно).
 *
 * Неизвестная сложность трактуется как 1.
 */
object SortingNumbersGenerator {

    /** Количество чисел на поле для сложности 1. */
    const val COUNT_EASY = 6

    /** Количество чисел на поле для сложности 2. */
    const val COUNT_MEDIUM = 9

    /** Количество чисел на поле для сложности 3. */
    const val COUNT_HARD = 12

    /** Количество гарантированно отрицательных чисел на сложности 3. */
    const val NEGATIVE_COUNT_HARD = 3

    /** Диапазон чисел для сложности 1. */
    val RANGE_EASY = 1..50

    /** Диапазон положительных чисел для сложностей 2 и 3. */
    val RANGE_POSITIVE = 1..199

    /** Диапазон отрицательных чисел для сложности 3. */
    val RANGE_NEGATIVE = -99..-1

    /**
     * Возвращает количество чисел на поле для заданной сложности.
     *
     * @param difficulty сложность 1..3 (иное значение трактуется как 1)
     */
    fun countFor(difficulty: Int): Int = when (difficulty) {
        2 -> COUNT_MEDIUM
        3 -> COUNT_HARD
        else -> COUNT_EASY
    }

    /**
     * Генерирует набор уникальных чисел для игры в случайном порядке показа.
     *
     * @param difficulty сложность 1..3 (иное значение трактуется как 1)
     * @param random источник случайности (передаётся явно для детерминизма в тестах)
     * @return список уникальных чисел в порядке отображения на поле
     */
    fun generate(difficulty: Int, random: Random = Random.Default): List<Int> = when (difficulty) {
        2 -> uniqueFrom(RANGE_POSITIVE, COUNT_MEDIUM, random)
        3 -> (
            uniqueFrom(RANGE_NEGATIVE, NEGATIVE_COUNT_HARD, random) +
                uniqueFrom(RANGE_POSITIVE, COUNT_HARD - NEGATIVE_COUNT_HARD, random)
            ).shuffled(random)

        else -> uniqueFrom(RANGE_EASY, COUNT_EASY, random)
    }

    /**
     * Выбирает [count] уникальных чисел из диапазона [range] в случайном порядке.
     */
    private fun uniqueFrom(range: IntRange, count: Int, random: Random): List<Int> =
        range.shuffled(random).take(count)
}

/**
 * Чистое состояние одной партии игры «По порядку»: игрок должен нажать
 * все числа строго по возрастанию.
 *
 * Правила:
 * - правильное нажатие (наименьшее из ещё не нажатых чисел) выводит число из игры;
 * - неправильное нажатие увеличивает счётчик ошибок [mistakes],
 *   раунд НЕ сбрасывается — прогресс сохраняется;
 * - после нажатия последнего числа игра считается пройденной ([isWon]).
 *
 * @property numbers числа на поле в порядке отображения; должны быть уникальными
 * @throws IllegalArgumentException если список пуст или содержит дубликаты
 */
class SortingGame(val numbers: List<Int>) {

    init {
        require(numbers.isNotEmpty()) { "Список чисел не должен быть пустым" }
        require(numbers.distinct().size == numbers.size) { "Числа должны быть уникальными" }
    }

    /** Числа в порядке возрастания — порядок, в котором их надо нажимать. */
    private val order = numbers.sorted()

    /** Индекс в [order] следующего числа, которое нужно нажать. */
    private var nextIndex = 0

    /** Количество ошибочных нажатий за партию. */
    var mistakes = 0
        private set

    /** true, если все числа нажаты и игра пройдена. */
    val isWon: Boolean
        get() = nextIndex == order.size

    /**
     * Следующее число, которое нужно нажать, или null, если игра уже пройдена.
     */
    fun nextExpected(): Int? = order.getOrNull(nextIndex)

    /**
     * Проверяет, было ли число [value] уже правильно нажато (погашено).
     *
     * @param value число с поля
     * @return true, если это число уже выведено из игры
     */
    fun isPressed(value: Int): Boolean {
        val index = order.indexOf(value)
        return index in 0 until nextIndex
    }

    /**
     * Обрабатывает нажатие числа [value].
     *
     * @param value нажатое число
     * @return [PressResult.CORRECT] — верное нажатие, игра продолжается;
     * [PressResult.WIN] — верное нажатие последнего числа (или игра уже была
     * пройдена — повторный вызов безопасен и не меняет состояние);
     * [PressResult.WRONG] — ошибка, счётчик [mistakes] увеличен
     */
    fun press(value: Int): PressResult {
        if (isWon) return PressResult.WIN
        return if (value == order[nextIndex]) {
            nextIndex++
            if (isWon) PressResult.WIN else PressResult.CORRECT
        } else {
            mistakes++
            PressResult.WRONG
        }
    }
}
