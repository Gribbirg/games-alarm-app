package com.example.smartalarm.feature.games.dice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты чистых функций игры «Кубики»: бросок, символы граней,
 * сумма (включая правило удвоения чётных) и текст отображения.
 */
class DiceLogicTest {

    // ---------- Бросок ----------

    @Test
    fun rollProducesRequestedNumberOfDice() {
        for (count in listOf(3, 5, 7))
            assertEquals(count, rollDice(count, Random(1)).size)
    }

    @Test
    fun allRolledValuesAreWithinDiceRange() {
        for (seed in 0L..99L) {
            for (value in rollDice(7, Random(seed)))
                assertTrue(
                    "Значение кости должно быть в 1..6, а не $value (seed=$seed)",
                    value in DICE_MIN_VALUE..DICE_MAX_VALUE
                )
        }
    }

    @Test
    fun rollIsDeterministicWithSameSeed() {
        assertEquals(rollDice(7, Random(42)), rollDice(7, Random(42)))
    }

    @Test
    fun everyFaceValueEventuallyAppears() {
        val seen = mutableSetOf<Int>()
        for (seed in 0L..99L)
            seen.addAll(rollDice(7, Random(seed)))
        assertEquals(
            "За 100 бросков по 7 костей должны выпасть все грани 1..6",
            (DICE_MIN_VALUE..DICE_MAX_VALUE).toSet(), seen
        )
    }

    // ---------- Символы граней ----------

    @Test
    fun symbolsMatchValues() {
        assertEquals('⚀', diceSymbol(1))
        assertEquals('⚁', diceSymbol(2))
        assertEquals('⚂', diceSymbol(3))
        assertEquals('⚃', diceSymbol(4))
        assertEquals('⚄', diceSymbol(5))
        assertEquals('⚅', diceSymbol(6))
    }

    @Test(expected = IllegalArgumentException::class)
    fun symbolForZeroThrows() {
        diceSymbol(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun symbolForSevenThrows() {
        diceSymbol(7)
    }

    // ---------- Сумма ----------

    @Test
    fun plainSumAddsAllValues() {
        assertEquals(6, diceSum(listOf(1, 2, 3), doubleEven = false))
        assertEquals(21, diceSum(listOf(1, 2, 3, 4, 5, 6), doubleEven = false))
        assertEquals(7, diceSum(listOf(1, 1, 1, 1, 1, 1, 1), doubleEven = false))
    }

    @Test
    fun doubleEvenRuleDoublesOnlyEvenValues() {
        // 1 + 2×2 + 3 + 4×2 + 5 + 6×2 = 1 + 4 + 3 + 8 + 5 + 12 = 33
        assertEquals(33, diceSum(listOf(1, 2, 3, 4, 5, 6), doubleEven = true))
        // Нечётные значения не меняются.
        assertEquals(9, diceSum(listOf(1, 3, 5), doubleEven = true))
        // Все чётные — сумма вдвое больше обычной.
        assertEquals(24, diceSum(listOf(2, 4, 6), doubleEven = true))
    }

    // ---------- Текст отображения ----------

    @Test
    fun diceTextShowsSymbolsSeparatedBySpaces() {
        assertEquals("⚀ ⚁ ⚂", diceText(listOf(1, 2, 3)))
    }

    @Test
    fun diceTextWrapsAfterFourDice() {
        assertEquals("⚀ ⚁ ⚂ ⚃\n⚄", diceText(listOf(1, 2, 3, 4, 5)))
        assertEquals(
            "⚅ ⚅ ⚅ ⚅\n⚅ ⚅ ⚅",
            diceText(listOf(6, 6, 6, 6, 6, 6, 6))
        )
    }

    @Test
    fun diceTextForFourDiceHasSingleLine() {
        assertEquals("⚀ ⚀ ⚀ ⚀", diceText(listOf(1, 1, 1, 1)))
    }
}
