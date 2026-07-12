package com.example.smartalarm.feature.games.digits

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты чистой логики игры «Запомни число» ([DigitsLogic]).
 */
class DigitsLogicTest {

    @Test
    fun `round lengths by difficulty`() {
        assertEquals(listOf(4, 5, 6), DigitsLogic.roundLengths(1))
        assertEquals(listOf(5, 6, 7), DigitsLogic.roundLengths(2))
        assertEquals(listOf(6, 7, 8), DigitsLogic.roundLengths(3))
    }

    @Test
    fun `unknown difficulty falls back to easiest lengths`() {
        assertEquals(listOf(4, 5, 6), DigitsLogic.roundLengths(0))
        assertEquals(listOf(4, 5, 6), DigitsLogic.roundLengths(99))
    }

    @Test
    fun `number length grows by one each round`() {
        for (difficulty in 1..3) {
            for (round in 1 until DigitsLogic.TOTAL_ROUNDS) {
                assertEquals(
                    DigitsLogic.numberLength(difficulty, round - 1) + 1,
                    DigitsLogic.numberLength(difficulty, round)
                )
            }
        }
    }

    @Test
    fun `number length coerces round out of range`() {
        assertEquals(4, DigitsLogic.numberLength(1, -5))
        assertEquals(6, DigitsLogic.numberLength(1, 100))
    }

    @Test
    fun `show time by difficulty`() {
        assertEquals(3000L, DigitsLogic.showTimeMillis(1))
        assertEquals(2500L, DigitsLogic.showTimeMillis(2))
        assertEquals(2000L, DigitsLogic.showTimeMillis(3))
    }

    @Test
    fun `unknown difficulty falls back to easiest show time`() {
        assertEquals(3000L, DigitsLogic.showTimeMillis(0))
        assertEquals(3000L, DigitsLogic.showTimeMillis(42))
    }

    @Test
    fun `generated number has requested length`() {
        val random = Random(1)
        for (length in 1..10) {
            assertEquals(length, DigitsLogic.generateNumber(length, random).length)
        }
    }

    @Test
    fun `generated number never starts with zero`() {
        val random = Random(2)
        repeat(1000) {
            val number = DigitsLogic.generateNumber(4, random)
            assertNotEquals('0', number.first())
        }
    }

    @Test
    fun `generated number contains only digits`() {
        val random = Random(3)
        repeat(1000) {
            val number = DigitsLogic.generateNumber(8, random)
            assertTrue("Not only digits: $number", number.all { it.isDigit() })
        }
    }

    @Test
    fun `generation is deterministic with same seed`() {
        val first = List(100) { DigitsLogic.generateNumber(6, Random(42 + it.toLong())) }
        val second = List(100) { DigitsLogic.generateNumber(6, Random(42 + it.toLong())) }
        assertEquals(first, second)
    }

    @Test
    fun `correct answer is accepted`() {
        assertTrue(DigitsLogic.isAnswerCorrect("1234", "1234"))
    }

    @Test
    fun `wrong answer is rejected`() {
        assertFalse(DigitsLogic.isAnswerCorrect("1234", "1235"))
        assertFalse(DigitsLogic.isAnswerCorrect("1234", "123"))
        assertFalse(DigitsLogic.isAnswerCorrect("1234", "12345"))
    }

    @Test
    fun `answer with leading zero is rejected`() {
        assertFalse(DigitsLogic.isAnswerCorrect("1234", "01234"))
    }

    @Test
    fun `empty answer is rejected`() {
        assertFalse(DigitsLogic.isAnswerCorrect("1234", ""))
    }

    @Test
    fun `last round detection`() {
        assertFalse(DigitsLogic.isLastRound(0))
        assertFalse(DigitsLogic.isLastRound(1))
        assertTrue(DigitsLogic.isLastRound(2))
    }
}
