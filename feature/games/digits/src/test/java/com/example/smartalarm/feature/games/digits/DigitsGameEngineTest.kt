package com.example.smartalarm.feature.games.digits

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты машины состояний партии «Запомни число» ([DigitsGameEngine]).
 */
class DigitsGameEngineTest {

    @Test
    fun `game starts at round 0 with starting length`() {
        assertEquals(0, DigitsGameEngine(1, Random(1)).round)
        assertEquals(4, DigitsGameEngine(1, Random(1)).currentNumber.length)
        assertEquals(5, DigitsGameEngine(2, Random(1)).currentNumber.length)
        assertEquals(6, DigitsGameEngine(3, Random(1)).currentNumber.length)
    }

    @Test
    fun `correct answer advances round and grows number length`() {
        val engine = DigitsGameEngine(1, Random(7))

        val result = engine.submitAnswer(engine.currentNumber)

        assertEquals(AnswerResult.NEXT_ROUND, result)
        assertEquals(1, engine.round)
        assertEquals(5, engine.currentNumber.length)
        assertFalse(engine.isWon)
    }

    @Test
    fun `wrong answer keeps round and gives new number of same length`() {
        val engine = DigitsGameEngine(2, Random(8))
        val numberBefore = engine.currentNumber

        val result = engine.submitAnswer("0")

        assertEquals(AnswerResult.WRONG, result)
        assertEquals(0, engine.round)
        assertEquals(numberBefore.length, engine.currentNumber.length)
        assertFalse(engine.isWon)
    }

    @Test
    fun `wrong answer regenerates the number`() {
        val engine = DigitsGameEngine(3, Random(9))
        val numberBefore = engine.currentNumber

        // Среди нескольких перегенераций хотя бы одна отличается от исходной
        // (совпадение всех пяти шестизначных чисел практически невозможно).
        val regenerated = List(5) {
            engine.submitAnswer("")
            engine.currentNumber
        }

        assertTrue(regenerated.any { it != numberBefore })
        assertEquals(0, engine.round)
    }

    @Test
    fun `three correct answers win the game`() {
        val engine = DigitsGameEngine(1, Random(10))

        assertEquals(AnswerResult.NEXT_ROUND, engine.submitAnswer(engine.currentNumber))
        assertEquals(AnswerResult.NEXT_ROUND, engine.submitAnswer(engine.currentNumber))
        assertEquals(AnswerResult.WIN, engine.submitAnswer(engine.currentNumber))
        assertTrue(engine.isWon)
        assertEquals(DigitsLogic.TOTAL_ROUNDS - 1, engine.round)
    }

    @Test
    fun `mistakes do not prevent winning`() {
        val engine = DigitsGameEngine(3, Random(11))

        engine.submitAnswer("123")
        assertEquals(AnswerResult.NEXT_ROUND, engine.submitAnswer(engine.currentNumber))
        engine.submitAnswer("456")
        assertEquals(AnswerResult.NEXT_ROUND, engine.submitAnswer(engine.currentNumber))
        engine.submitAnswer("789")
        assertEquals(AnswerResult.WIN, engine.submitAnswer(engine.currentNumber))
        assertTrue(engine.isWon)
    }

    @Test
    fun `round lengths follow difficulty progression`() {
        for (difficulty in 1..3) {
            val engine = DigitsGameEngine(difficulty, Random(12))
            val expectedLengths = DigitsLogic.roundLengths(difficulty)

            assertEquals(expectedLengths[0], engine.currentNumber.length)
            engine.submitAnswer(engine.currentNumber)
            assertEquals(expectedLengths[1], engine.currentNumber.length)
            engine.submitAnswer(engine.currentNumber)
            assertEquals(expectedLengths[2], engine.currentNumber.length)
        }
    }

    @Test
    fun `engine is deterministic with same seed`() {
        val first = DigitsGameEngine(2, Random(13))
        val second = DigitsGameEngine(2, Random(13))

        assertEquals(first.currentNumber, second.currentNumber)

        first.submitAnswer("0")
        second.submitAnswer("0")
        assertEquals(first.currentNumber, second.currentNumber)

        first.submitAnswer(first.currentNumber)
        second.submitAnswer(second.currentNumber)
        assertEquals(first.currentNumber, second.currentNumber)
    }

    @Test
    fun `submit after win keeps won state`() {
        val engine = DigitsGameEngine(1, Random(14))
        repeat(DigitsLogic.TOTAL_ROUNDS) { engine.submitAnswer(engine.currentNumber) }
        val numberAfterWin = engine.currentNumber

        assertEquals(AnswerResult.WIN, engine.submitAnswer("что угодно"))
        assertTrue(engine.isWon)
        assertEquals(numberAfterWin, engine.currentNumber)
    }
}
