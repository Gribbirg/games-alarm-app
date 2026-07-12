package com.example.smartalarm.feature.games.reaction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Тесты [ReactionSettings.forLevel]: параметры уровней, рост сложности
 * (быстрее бегунок, уже зона, больше попаданий) и приведение
 * внедиапазонных значений сложности.
 */
class ReactionSettingsTest {

    @Test
    fun `level 1 parameters`() {
        val settings = ReactionSettings.forLevel(1)
        assertEquals(3, settings.hitsToWin)
        assertEquals(0.20, settings.zoneWidth, EPS)
        assertEquals(2400L, settings.periodMs)
    }

    @Test
    fun `level 2 parameters`() {
        val settings = ReactionSettings.forLevel(2)
        assertEquals(4, settings.hitsToWin)
        assertEquals(0.14, settings.zoneWidth, EPS)
        assertEquals(1800L, settings.periodMs)
    }

    @Test
    fun `level 3 parameters`() {
        val settings = ReactionSettings.forLevel(3)
        assertEquals(5, settings.hitsToWin)
        assertEquals(0.10, settings.zoneWidth, EPS)
        assertEquals(1300L, settings.periodMs)
    }

    @Test
    fun `difficulty grows monotonically`() {
        for (level in 2..3) {
            val easier = ReactionSettings.forLevel(level - 1)
            val harder = ReactionSettings.forLevel(level)
            assertTrue(harder.hitsToWin > easier.hitsToWin)
            assertTrue(harder.zoneWidth < easier.zoneWidth)
            assertTrue(harder.periodMs < easier.periodMs)
        }
    }

    @Test
    fun `out of range difficulty is coerced to nearest level`() {
        assertEquals(ReactionSettings.forLevel(1), ReactionSettings.forLevel(0))
        assertEquals(ReactionSettings.forLevel(1), ReactionSettings.forLevel(-5))
        assertEquals(ReactionSettings.forLevel(3), ReactionSettings.forLevel(4))
        assertEquals(ReactionSettings.forLevel(3), ReactionSettings.forLevel(100))
    }

    @Test
    fun `zone width is a valid scale fraction on every level`() {
        for (level in 1..3) {
            val width = ReactionSettings.forLevel(level).zoneWidth
            assertTrue(width > 0.0 && width < 1.0)
        }
    }

    companion object {
        private const val EPS = 1e-9
    }
}
