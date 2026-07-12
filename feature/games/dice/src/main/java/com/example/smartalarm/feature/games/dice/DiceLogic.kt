package com.example.smartalarm.feature.games.dice

import kotlin.random.Random

/** Минимальное значение грани игральной кости. */
const val DICE_MIN_VALUE = 1

/** Максимальное значение грани игральной кости. */
const val DICE_MAX_VALUE = 6

/**
 * Юникод-символы граней игральной кости по возрастанию значения:
 * индекс 0 — ⚀ (единица), индекс 5 — ⚅ (шестёрка).
 */
const val DICE_FACES = "⚀⚁⚂⚃⚄⚅"

/** Максимум костей в одной строке при отображении броска (см. [diceText]). */
const val DICE_PER_ROW = 4

/**
 * Возвращает юникод-символ грани кости для значения [value].
 *
 * @param value значение грани в диапазоне [DICE_MIN_VALUE]..[DICE_MAX_VALUE].
 * @throws IllegalArgumentException если [value] вне диапазона 1..6.
 */
fun diceSymbol(value: Int): Char {
    require(value in DICE_MIN_VALUE..DICE_MAX_VALUE) {
        "Значение кости должно быть в диапазоне 1..6, а не $value"
    }
    return DICE_FACES[value - 1]
}

/**
 * Бросает [count] игральных костей.
 *
 * @param count сколько костей бросить (положительное число).
 * @param random источник случайности; подставьте [Random] с seed для
 * детерминированных тестов.
 * @return список из [count] значений, каждое в диапазоне 1..6.
 */
fun rollDice(count: Int, random: Random): List<Int> =
    List(count) { random.nextInt(DICE_MIN_VALUE, DICE_MAX_VALUE + 1) }

/**
 * Считает сумму броска [values].
 *
 * @param values значения костей (каждое 1..6).
 * @param doubleEven специальное правило сложности 3: если true, кости
 * с чётным значением считаются удвоенными (2 → 4, 4 → 8, 6 → 12).
 */
fun diceSum(values: List<Int>, doubleEven: Boolean): Int =
    values.sumOf { if (doubleEven && it % 2 == 0) it * 2 else it }

/**
 * Строит многострочный текст для отображения броска: символы костей
 * через пробел, не больше [DICE_PER_ROW] костей в строке (7 костей
 * разбиваются на строки 4 + 3, чтобы крупные символы помещались на экране).
 */
fun diceText(values: List<Int>): String =
    values.chunked(DICE_PER_ROW)
        .joinToString("\n") { row -> row.joinToString(" ") { diceSymbol(it).toString() } }
