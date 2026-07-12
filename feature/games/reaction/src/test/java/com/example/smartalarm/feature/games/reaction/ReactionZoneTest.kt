package com.example.smartalarm.feature.games.reaction

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Тесты [ReactionZone]: генерация в границах шкалы и нужной ширины,
 * детерминизм с seed, попадание/промах на граничных значениях.
 */
class ReactionZoneTest {

    @Test
    fun `generated zone lies within scale and has requested width`() {
        val random = Random(42)
        for (difficulty in 1..3) {
            val width = ReactionSettings.forLevel(difficulty).zoneWidth
            repeat(500) {
                val zone = ReactionZone.generate(width, random)
                assertTrue("start out of range: ${zone.start}", zone.start >= 0.0)
                assertTrue("end out of range: ${zone.end}", zone.end <= 1.0)
                assertEquals(width, zone.width, EPS)
            }
        }
    }

    @Test
    fun `same seed produces identical zones`() {
        val zones1 = generateSequenceWithSeed(2024)
        val zones2 = generateSequenceWithSeed(2024)
        assertEquals(zones1, zones2)
    }

    @Test
    fun `different seeds produce different zone sequences`(){
        assertFalse(generateSequenceWithSeed(1) == generateSequenceWithSeed(2))
    }

    private fun generateSequenceWithSeed(seed: Int): List<ReactionZone> {
        val random = Random(seed)
        return List(50) { ReactionZone.generate(0.15, random) }
    }

    @Test
    fun `boundaries are inclusive hits`() {
        val zone = ReactionZone(0.3, 0.5)
        assertTrue(zone.contains(0.3))
        assertTrue(zone.contains(0.5))
        assertTrue(zone.contains(0.4))
    }

    @Test
    fun `positions just outside boundaries are misses`() {
        val zone = ReactionZone(0.3, 0.5)
        assertFalse(zone.contains(0.3 - 1e-9))
        assertFalse(zone.contains(0.5 + 1e-9))
        assertFalse(zone.contains(0.0))
        assertFalse(zone.contains(1.0))
    }

    @Test
    fun `width is end minus start`() {
        assertEquals(0.2, ReactionZone(0.3, 0.5).width, EPS)
        assertEquals(1.0, ReactionZone(0.0, 1.0).width, EPS)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zone with end before start is rejected`() {
        ReactionZone(0.5, 0.3)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zone outside scale is rejected`() {
        ReactionZone(0.9, 1.1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zero width generation is rejected`() {
        ReactionZone.generate(0.0, Random(1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `width above 1 generation is rejected`() {
        ReactionZone.generate(1.1, Random(1))
    }

    companion object {
        private const val EPS = 1e-9
    }
}
