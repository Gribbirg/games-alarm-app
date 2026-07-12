package com.example.smartalarm.feature.games.sorting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты состояния партии [SortingGame]: правильная и неправильная
 * последовательность нажатий, победа, подсчёт ошибок.
 */
class SortingGameTest {

    @Test
    fun `pressing numbers in ascending order wins the game`() {
        val game = SortingGame(listOf(30, -5, 12, 7))
        val ascending = listOf(-5, 7, 12, 30)

        for ((index, value) in ascending.withIndex()) {
            val expected =
                if (index == ascending.size - 1) PressResult.WIN else PressResult.CORRECT
            assertEquals(expected, game.press(value))
        }
        assertTrue(game.isWon)
        assertEquals(0, game.mistakes)
        assertNull(game.nextExpected())
    }

    @Test
    fun `wrong press counts mistake and keeps state`() {
        val game = SortingGame(listOf(5, 1, 9))

        assertEquals(PressResult.WRONG, game.press(9))
        assertEquals(1, game.mistakes)
        assertEquals(1, game.nextExpected())
        assertFalse(game.isPressed(9))
        assertFalse(game.isWon)
    }

    @Test
    fun `progress is not reset after a mistake`() {
        val game = SortingGame(listOf(5, 1, 9))

        assertEquals(PressResult.CORRECT, game.press(1))
        assertEquals(PressResult.WRONG, game.press(9))
        // Прогресс сохранён: 1 всё ещё погашена, следующей ждём 5.
        assertTrue(game.isPressed(1))
        assertEquals(5, game.nextExpected())
        assertEquals(PressResult.CORRECT, game.press(5))
        assertEquals(PressResult.WIN, game.press(9))
        assertEquals(1, game.mistakes)
    }

    @Test
    fun `each wrong press increments mistakes`() {
        val game = SortingGame(listOf(2, 4, 6))

        game.press(6)
        game.press(4)
        game.press(6)
        assertEquals(3, game.mistakes)
        assertEquals(2, game.nextExpected())
    }

    @Test
    fun `isPressed tracks only correctly pressed numbers`() {
        val game = SortingGame(listOf(10, -3, 25))

        assertFalse(game.isPressed(-3))
        game.press(-3)
        assertTrue(game.isPressed(-3))
        assertFalse(game.isPressed(10))
        assertFalse(game.isPressed(25))
        // Числа, которых нет на поле, никогда не считаются нажатыми.
        assertFalse(game.isPressed(999))
    }

    @Test
    fun `press after win is safe and changes nothing`() {
        val game = SortingGame(listOf(1, 2))
        game.press(1)
        game.press(2)
        assertTrue(game.isWon)

        assertEquals(PressResult.WIN, game.press(1))
        assertEquals(PressResult.WIN, game.press(999))
        assertEquals(0, game.mistakes)
        assertTrue(game.isWon)
    }

    @Test
    fun `pressing value not on the field is a mistake`() {
        val game = SortingGame(listOf(1, 2, 3))

        assertEquals(PressResult.WRONG, game.press(999))
        assertEquals(1, game.mistakes)
        assertEquals(1, game.nextExpected())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `duplicate numbers are rejected`() {
        SortingGame(listOf(1, 2, 2))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty numbers are rejected`() {
        SortingGame(emptyList())
    }

    @Test
    fun `full round on generated numbers for every difficulty`() {
        for (difficulty in 1..3) {
            val game = SortingGame(SortingNumbersGenerator.generate(difficulty, Random(99)))
            val ascending = game.numbers.sorted()

            for ((index, value) in ascending.withIndex()) {
                assertEquals(value, game.nextExpected())
                val expected =
                    if (index == ascending.size - 1) PressResult.WIN else PressResult.CORRECT
                assertEquals(expected, game.press(value))
            }
            assertTrue(game.isWon)
            assertEquals(0, game.mistakes)
        }
    }
}
