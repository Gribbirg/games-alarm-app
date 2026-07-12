package com.example.smartalarm.feature.games.lights

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Возвращает текущее состояние поля [game] как список для сравнения в тестах. */
private fun snapshot(game: LightsGame): List<Boolean> =
    (0 until game.cellCount).map { game.isOn(it) }

/** Создаёт партию на погашенном поле [size]×[size]. */
private fun emptyGame(size: Int): LightsGame =
    LightsGame(size, List(size * size) { false })

/**
 * Тесты параметров сложности: размер поля, число запутывающих нажатий, пар.
 */
class LightsDifficultyTest {

    @Test
    fun `field is 3x3 with 3 scrambles for difficulty 1`() {
        assertEquals(3, lightsSizeFor(1))
        assertEquals(3, lightsScrambleCountFor(1))
    }

    @Test
    fun `field is 4x4 with 5 scrambles for difficulty 2`() {
        assertEquals(4, lightsSizeFor(2))
        assertEquals(5, lightsScrambleCountFor(2))
    }

    @Test
    fun `field is 5x5 with 7 scrambles for difficulty 3`() {
        assertEquals(5, lightsSizeFor(3))
        assertEquals(7, lightsScrambleCountFor(3))
    }

    @Test
    fun `unknown difficulty falls back to easiest`() {
        assertEquals(3, lightsSizeFor(0))
        assertEquals(3, lightsScrambleCountFor(0))
        assertEquals(3, lightsSizeFor(99))
        assertEquals(3, lightsScrambleCountFor(99))
    }

    @Test
    fun `press par is triple the scramble count`() {
        assertEquals(9, lightsPressParFor(1))
        assertEquals(15, lightsPressParFor(2))
        assertEquals(21, lightsPressParFor(3))
    }
}

/**
 * Тесты хода: нажатие переключает ровно клетку и её ортогональных соседей.
 */
class LightsToggleTest {

    @Test
    fun `center press on 3x3 toggles cell and 4 neighbors`() {
        val game = emptyGame(3)
        assertTrue(game.press(4))
        // Поле 3x3: центр 4, соседи 1 (верх), 3 (лево), 5 (право), 7 (низ).
        assertEquals(
            listOf(false, true, false, true, true, true, false, true, false),
            snapshot(game)
        )
    }

    @Test
    fun `corner press on 3x3 toggles cell and 2 neighbors`() {
        val game = emptyGame(3)
        assertTrue(game.press(0))
        assertEquals(
            listOf(true, true, false, true, false, false, false, false, false),
            snapshot(game)
        )
    }

    @Test
    fun `edge press on 3x3 toggles cell and 3 neighbors`() {
        val game = emptyGame(3)
        assertTrue(game.press(1))
        assertEquals(
            listOf(true, true, true, false, true, false, false, false, false),
            snapshot(game)
        )
    }

    @Test
    fun `bottom right corner press does not wrap around`() {
        val game = emptyGame(3)
        assertTrue(game.press(8))
        assertEquals(
            listOf(false, false, false, false, false, true, false, true, true),
            snapshot(game)
        )
    }

    @Test
    fun `double press restores the state`() {
        for (index in 0 until 9) {
            val game = emptyGame(3)
            game.press(index)
            game.press(index)
            assertEquals("press at $index", List(9) { false }, snapshot(game))
            assertEquals(2, game.pressCount)
        }
    }

    @Test
    fun `double press restores an arbitrary state`() {
        val field = generateLightsField(4, 5, Random(7))
        val game = LightsGame(4, field.lights)
        game.press(6)
        game.press(6)
        assertEquals(field.lights, snapshot(game))
    }

    @Test
    fun `press out of bounds is rejected and not counted`() {
        val game = emptyGame(3)
        assertFalse(game.press(-1))
        assertFalse(game.press(9))
        assertEquals(0, game.pressCount)
        assertEquals(List(9) { false }, snapshot(game))
    }

    @Test
    fun `press count increments per valid press`() {
        val game = emptyGame(3)
        game.press(0)
        game.press(4)
        game.press(8)
        assertEquals(3, game.pressCount)
    }
}

/**
 * Тесты генерации поля: непустота, решаемость теми же нажатиями, детерминизм.
 */
class LightsGenerationTest {

    @Test
    fun `generated field is never already solved`() {
        for (seed in 0L until 50L) {
            for (difficulty in 1..3) {
                val size = lightsSizeFor(difficulty)
                val field = generateLightsField(
                    size, lightsScrambleCountFor(difficulty), Random(seed)
                )
                assertTrue(
                    "seed $seed difficulty $difficulty",
                    field.lights.any { it }
                )
            }
        }
    }

    @Test
    fun `scramble cells are distinct and match requested count`() {
        for (difficulty in 1..3) {
            val size = lightsSizeFor(difficulty)
            val scrambleCount = lightsScrambleCountFor(difficulty)
            val field = generateLightsField(size, scrambleCount, Random(11))
            assertEquals(scrambleCount, field.scramble.size)
            assertEquals(scrambleCount, field.scramble.distinct().size)
            assertTrue(field.scramble.all { it in 0 until size * size })
        }
    }

    @Test
    fun `field sizes follow difficulty`() {
        for (difficulty in 1..3) {
            val size = lightsSizeFor(difficulty)
            val field = generateLightsField(
                size, lightsScrambleCountFor(difficulty), Random(3)
            )
            assertEquals(size, field.size)
            assertEquals(size * size, field.lights.size)
        }
    }

    @Test
    fun `repeating the scramble presses solves the field`() {
        for (seed in 0L until 20L) {
            for (difficulty in 1..3) {
                val size = lightsSizeFor(difficulty)
                val field = generateLightsField(
                    size, lightsScrambleCountFor(difficulty), Random(seed)
                )
                val game = LightsGame(size, field.lights)
                for (index in field.scramble)
                    assertTrue(game.press(index))
                assertTrue("seed $seed difficulty $difficulty", game.isWon)
            }
        }
    }

    @Test
    fun `solution works in any order because presses commute`() {
        val field = generateLightsField(5, 7, Random(21))
        val game = LightsGame(5, field.lights)
        for (index in field.scramble.sortedDescending())
            game.press(index)
        assertTrue(game.isWon)
    }

    @Test
    fun `same seed produces same field`() {
        for (difficulty in 1..3) {
            val size = lightsSizeFor(difficulty)
            val scrambleCount = lightsScrambleCountFor(difficulty)
            assertEquals(
                generateLightsField(size, scrambleCount, Random(123)),
                generateLightsField(size, scrambleCount, Random(123))
            )
        }
    }
}

/**
 * Тесты состояния партии: победа, счётчики, доступ к клеткам.
 */
class LightsGameStateTest {

    @Test
    fun `game on fully lit 1x1 field is won by single press`() {
        val game = LightsGame(1, listOf(true))
        assertFalse(game.isWon)
        assertTrue(game.press(0))
        assertTrue(game.isWon)
    }

    @Test
    fun `winning press turns off the last lights`() {
        // Поле — результат одного нажатия в центр 3x3: гасится тем же нажатием.
        val start = emptyGame(3).also { it.press(4) }
        val game = LightsGame(3, snapshot(start))
        assertFalse(game.isWon)
        assertEquals(5, game.litCount)
        game.press(4)
        assertTrue(game.isWon)
        assertEquals(0, game.litCount)
    }

    @Test
    fun `empty field is won and any press breaks it`() {
        val game = emptyGame(3)
        assertTrue(game.isWon)
        game.press(0)
        assertFalse(game.isWon)
    }

    @Test
    fun `lit count matches lit cells`() {
        val field = generateLightsField(4, 5, Random(5))
        val game = LightsGame(4, field.lights)
        assertEquals(field.lights.count { it }, game.litCount)
        assertEquals(16, game.cellCount)
    }

    @Test
    fun `isOn is false outside the field`() {
        val game = LightsGame(1, listOf(true))
        assertTrue(game.isOn(0))
        assertFalse(game.isOn(-1))
        assertFalse(game.isOn(1))
    }
}
