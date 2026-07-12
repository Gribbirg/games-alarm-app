package com.example.smartalarm.feature.games.stroop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Тесты параметров сложности [StroopSettings].
 */
class StroopSettingsTest {

    @Test
    fun `difficulty 1 has 6 rounds and 4 base colors`() {
        val settings = StroopSettings.forLevel(1)
        assertEquals(6, settings.roundsToWin)
        assertEquals(
            listOf(StroopColor.RED, StroopColor.BLUE, StroopColor.GREEN, StroopColor.YELLOW),
            settings.colorPool
        )
        assertEquals(0.0, settings.meaningChance, 0.0)
        assertEquals(4, settings.optionsCount)
    }

    @Test
    fun `difficulty 2 has 8 rounds and 5 colors with purple`() {
        val settings = StroopSettings.forLevel(2)
        assertEquals(8, settings.roundsToWin)
        assertEquals(5, settings.colorPool.size)
        assertTrue(settings.colorPool.contains(StroopColor.PURPLE))
        assertEquals(0.0, settings.meaningChance, 0.0)
    }

    @Test
    fun `difficulty 3 has 10 rounds, all 6 colors and inverted rounds chance`() {
        val settings = StroopSettings.forLevel(3)
        assertEquals(10, settings.roundsToWin)
        assertEquals(StroopColor.entries.toList(), settings.colorPool)
        assertTrue(settings.meaningChance > 0.0)
    }

    @Test
    fun `out of range difficulty is coerced to nearest level`() {
        assertEquals(StroopSettings.forLevel(1), StroopSettings.forLevel(0))
        assertEquals(StroopSettings.forLevel(1), StroopSettings.forLevel(-5))
        assertEquals(StroopSettings.forLevel(3), StroopSettings.forLevel(4))
        assertEquals(StroopSettings.forLevel(3), StroopSettings.forLevel(100))
    }

    @Test
    fun `color pool never smaller than options count`() {
        for (difficulty in 1..3) {
            val settings = StroopSettings.forLevel(difficulty)
            assertTrue(settings.colorPool.size >= settings.optionsCount)
        }
    }
}
