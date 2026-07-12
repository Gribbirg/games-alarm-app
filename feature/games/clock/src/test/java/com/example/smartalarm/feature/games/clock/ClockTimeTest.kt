package com.example.smartalarm.feature.games.clock

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Тесты чистой модели времени [ClockTime]: углы стрелок, формат строки,
 * арифметика на циферблате и валидация аргументов.
 */
class ClockTimeTest {

    private val delta = 1e-4f

    @Test
    fun anglesAtTwelveOClockAreZero() {
        val time = ClockTime(12, 0)
        assertEquals(0f, time.hourHandAngle, delta)
        assertEquals(0f, time.minuteHandAngle, delta)
    }

    @Test
    fun anglesAtThreeOClock() {
        val time = ClockTime(3, 0)
        assertEquals(90f, time.hourHandAngle, delta)
        assertEquals(0f, time.minuteHandAngle, delta)
    }

    @Test
    fun anglesAtHalfPastSix() {
        val time = ClockTime(6, 30)
        assertEquals(195f, time.hourHandAngle, delta)
        assertEquals(180f, time.minuteHandAngle, delta)
    }

    @Test
    fun anglesAtQuarterToTen() {
        val time = ClockTime(9, 45)
        assertEquals(292.5f, time.hourHandAngle, delta)
        assertEquals(270f, time.minuteHandAngle, delta)
    }

    @Test
    fun hourHandShiftsProportionallyToMinutes() {
        assertEquals(232.5f, ClockTime(7, 45).hourHandAngle, delta)
        assertEquals(5f, ClockTime(12, 10).hourHandAngle, delta)
    }

    @Test
    fun toStringUsesLeadingZeros() {
        assertEquals("07:05", ClockTime(7, 5).toString())
        assertEquals("12:00", ClockTime(12, 0).toString())
        assertEquals("11:45", ClockTime(11, 45).toString())
        assertEquals("01:30", ClockTime(1, 30).toString())
    }

    @Test
    fun fromTotalMinutesNormalizes() {
        assertEquals(ClockTime(12, 0), ClockTime.fromTotalMinutes(0))
        assertEquals(ClockTime(11, 59), ClockTime.fromTotalMinutes(719))
        assertEquals(ClockTime(12, 0), ClockTime.fromTotalMinutes(720))
        assertEquals(ClockTime(11, 30), ClockTime.fromTotalMinutes(-30))
        assertEquals(ClockTime(1, 15), ClockTime.fromTotalMinutes(75))
    }

    @Test
    fun totalMinutesIsInverseOfFromTotalMinutes() {
        for (total in 0 until ClockTime.HALF_DAY_MINUTES) {
            assertEquals(total, ClockTime.fromTotalMinutes(total).totalMinutes)
        }
    }

    @Test
    fun shiftedByWrapsAroundTheDial() {
        assertEquals(ClockTime(11, 45), ClockTime(12, 0).shiftedBy(-15))
        assertEquals(ClockTime(12, 15), ClockTime(11, 45).shiftedBy(30))
        assertEquals(ClockTime(7, 50), ClockTime(7, 45).shiftedBy(5))
    }

    @Test
    fun withSwappedHandsExchangesHandPositions() {
        assertEquals(ClockTime(3, 45), ClockTime(9, 15).withSwappedHands())
        assertEquals(ClockTime(6, 0), ClockTime(12, 30).withSwappedHands())
        assertEquals(ClockTime(12, 15), ClockTime(3, 0).withSwappedHands())
    }

    @Test
    fun mirroredReflectsBothHands() {
        assertEquals(ClockTime(7, 20), ClockTime(4, 40).mirrored())
        assertEquals(ClockTime(9, 0), ClockTime(3, 0).mirrored())
        assertEquals(ClockTime(12, 0), ClockTime(12, 0).mirrored())
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroHoursIsRejected() {
        ClockTime(0, 30)
    }

    @Test(expected = IllegalArgumentException::class)
    fun thirteenHoursIsRejected() {
        ClockTime(13, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun sixtyMinutesIsRejected() {
        ClockTime(6, 60)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeMinutesAreRejected() {
        ClockTime(6, -5)
    }
}
