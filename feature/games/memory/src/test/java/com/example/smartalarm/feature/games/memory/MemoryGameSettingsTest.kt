package com.example.smartalarm.feature.games.memory

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Тесты параметров сложности игры «Повтори узор».
 */
class MemoryGameSettingsTest {

    @Test
    fun difficulty1_hasExpectedParameters() {
        val settings = MemoryGameSettings.forDifficulty(1)
        assertEquals(1, settings.difficulty)
        assertEquals(3, settings.startLength)
        assertEquals(3, settings.roundsCount)
        assertEquals(650L, settings.showCellMs)
        assertEquals(300L, settings.betweenCellsMs)
        assertEquals(5, settings.finalLength)
    }

    @Test
    fun difficulty2_hasExpectedParameters() {
        val settings = MemoryGameSettings.forDifficulty(2)
        assertEquals(2, settings.difficulty)
        assertEquals(4, settings.startLength)
        assertEquals(4, settings.roundsCount)
        assertEquals(500L, settings.showCellMs)
        assertEquals(250L, settings.betweenCellsMs)
        assertEquals(7, settings.finalLength)
    }

    @Test
    fun difficulty3_hasExpectedParameters() {
        val settings = MemoryGameSettings.forDifficulty(3)
        assertEquals(3, settings.difficulty)
        assertEquals(5, settings.startLength)
        assertEquals(5, settings.roundsCount)
        assertEquals(400L, settings.showCellMs)
        assertEquals(200L, settings.betweenCellsMs)
        assertEquals(9, settings.finalLength)
    }

    @Test
    fun outOfRangeDifficulty_isCoercedToNearestBound() {
        assertEquals(MemoryGameSettings.forDifficulty(1), MemoryGameSettings.forDifficulty(0))
        assertEquals(MemoryGameSettings.forDifficulty(1), MemoryGameSettings.forDifficulty(-5))
        assertEquals(MemoryGameSettings.forDifficulty(3), MemoryGameSettings.forDifficulty(4))
        assertEquals(MemoryGameSettings.forDifficulty(3), MemoryGameSettings.forDifficulty(100))
    }
}
