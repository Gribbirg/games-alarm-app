package com.example.smartalarm.feature.games.reaction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Тесты треугольной волны [ReactionWave.position]: опорные точки,
 * монотонность на полупериодах, периодичность и диапазон значений.
 */
class ReactionWaveTest {

    private val period = 2000L

    @Test
    fun `position is 0 at start of period`() {
        assertEquals(0.0, ReactionWave.position(0, period), EPS)
    }

    @Test
    fun `position is 1 at half period`() {
        assertEquals(1.0, ReactionWave.position(period / 2, period), EPS)
    }

    @Test
    fun `position is 0 at full period`() {
        assertEquals(0.0, ReactionWave.position(period, period), EPS)
    }

    @Test
    fun `position is one half at quarter and three quarters of period`() {
        assertEquals(0.5, ReactionWave.position(period / 4, period), EPS)
        assertEquals(0.5, ReactionWave.position(period * 3 / 4, period), EPS)
    }

    @Test
    fun `position strictly increases on first half period`() {
        var previous = ReactionWave.position(0, period)
        for (elapsed in 10..period / 2 step 10) {
            val current = ReactionWave.position(elapsed, period)
            assertTrue("not increasing at $elapsed ms", current > previous)
            previous = current
        }
    }

    @Test
    fun `position strictly decreases on second half period`() {
        var previous = ReactionWave.position(period / 2, period)
        for (elapsed in period / 2 + 10 until period step 10) {
            val current = ReactionWave.position(elapsed, period)
            assertTrue("not decreasing at $elapsed ms", current < previous)
            previous = current
        }
    }

    @Test
    fun `wave is periodic`() {
        for (elapsed in 0 until period step 37) {
            val base = ReactionWave.position(elapsed, period)
            assertEquals(base, ReactionWave.position(elapsed + period, period), EPS)
            assertEquals(base, ReactionWave.position(elapsed + 5 * period, period), EPS)
        }
    }

    @Test
    fun `position always stays in 0 to 1`() {
        for (elapsed in 0L..10_000L step 7) {
            val position = ReactionWave.position(elapsed, period)
            assertTrue("position $position out of range at $elapsed ms", position in 0.0..1.0)
        }
    }

    @Test
    fun `negative elapsed is treated as 0`() {
        assertEquals(0.0, ReactionWave.position(-1, period), EPS)
        assertEquals(0.0, ReactionWave.position(-period, period), EPS)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `non-positive period is rejected`() {
        ReactionWave.position(0, 0)
    }

    @Test
    fun `works for all difficulty periods`() {
        for (difficulty in 1..3) {
            val p = ReactionSettings.forLevel(difficulty).periodMs
            assertEquals(0.0, ReactionWave.position(0, p), EPS)
            assertEquals(1.0, ReactionWave.position(p / 2, p), EPS)
            assertEquals(0.0, ReactionWave.position(p, p), EPS)
        }
    }

    companion object {
        private const val EPS = 1e-9
    }
}
