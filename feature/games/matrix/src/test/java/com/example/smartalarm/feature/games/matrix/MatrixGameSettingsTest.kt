package com.example.smartalarm.feature.games.matrix

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Тесты параметров сложности игры «Запомни клетки»: значения таблицы
 * сложностей, приведение уровня к диапазону и рост набора по раундам.
 */
class MatrixGameSettingsTest {

    @Test
    fun difficulty1_is3x3With3Rounds() {
        val settings = MatrixGameSettings.forDifficulty(1)
        assertEquals(3, settings.gridSize)
        assertEquals(9, settings.cellsTotal)
        assertEquals(3, settings.roundsCount)
        assertEquals(3, settings.startCellsCount)
        assertEquals(5, settings.maxCellsCount)
    }

    @Test
    fun difficulty2_is4x4With4Rounds() {
        val settings = MatrixGameSettings.forDifficulty(2)
        assertEquals(4, settings.gridSize)
        assertEquals(16, settings.cellsTotal)
        assertEquals(4, settings.roundsCount)
        assertEquals(5, settings.startCellsCount)
        assertEquals(8, settings.maxCellsCount)
    }

    @Test
    fun difficulty3_is5x5With5Rounds() {
        val settings = MatrixGameSettings.forDifficulty(3)
        assertEquals(5, settings.gridSize)
        assertEquals(25, settings.cellsTotal)
        assertEquals(5, settings.roundsCount)
        assertEquals(7, settings.startCellsCount)
        assertEquals(10, settings.maxCellsCount)
    }

    @Test
    fun difficultyOutOfRange_isCoercedToBounds() {
        assertEquals(MatrixGameSettings.forDifficulty(1), MatrixGameSettings.forDifficulty(0))
        assertEquals(MatrixGameSettings.forDifficulty(1), MatrixGameSettings.forDifficulty(-5))
        assertEquals(MatrixGameSettings.forDifficulty(3), MatrixGameSettings.forDifficulty(4))
        assertEquals(MatrixGameSettings.forDifficulty(3), MatrixGameSettings.forDifficulty(100))
    }

    @Test
    fun cellsForRound_growsByOnePerRoundUpToCap() {
        val settings = MatrixGameSettings.forDifficulty(3)
        assertEquals(7, settings.cellsForRound(1))
        assertEquals(8, settings.cellsForRound(2))
        assertEquals(9, settings.cellsForRound(3))
        assertEquals(10, settings.cellsForRound(4))
        // Пятый раунд упирается в потолок maxCellsCount.
        assertEquals(10, settings.cellsForRound(5))
    }

    @Test
    fun cellsForRound_neverExceedsFieldSize() {
        for (difficulty in 1..3) {
            val settings = MatrixGameSettings.forDifficulty(difficulty)
            for (round in 1..settings.roundsCount)
                assertTrue(
                    "Набор больше поля (difficulty=$difficulty, round=$round)",
                    settings.cellsForRound(round) < settings.cellsTotal
                )
        }
    }

    @Test
    fun showTime_isWithinSpecifiedRange() {
        for (difficulty in 1..3) {
            val settings = MatrixGameSettings.forDifficulty(difficulty)
            assertTrue(settings.showTimeMs in 1500..2000)
        }
    }
}
