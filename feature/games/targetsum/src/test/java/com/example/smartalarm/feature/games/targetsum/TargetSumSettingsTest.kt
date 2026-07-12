package com.example.smartalarm.feature.games.targetsum

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Тесты [TargetSumSettings]: параметры по уровням сложности
 * и приведение сложности вне диапазона к границе.
 */
class TargetSumSettingsTest {

    @Test
    fun difficulty1_has6Numbers_solutionOf3_range1to15_2rounds() {
        val settings = TargetSumSettings.forDifficulty(1)
        assertEquals(1, settings.difficulty)
        assertEquals(6, settings.gridSize)
        assertEquals(3, settings.solutionSize)
        assertEquals(1, settings.minNumber)
        assertEquals(15, settings.maxNumber)
        assertEquals(2, settings.roundsCount)
    }

    @Test
    fun difficulty2_has9Numbers_solutionOf4_range1to30_2rounds() {
        val settings = TargetSumSettings.forDifficulty(2)
        assertEquals(2, settings.difficulty)
        assertEquals(9, settings.gridSize)
        assertEquals(4, settings.solutionSize)
        assertEquals(1, settings.minNumber)
        assertEquals(30, settings.maxNumber)
        assertEquals(2, settings.roundsCount)
    }

    @Test
    fun difficulty3_has12Numbers_solutionOf5_range1to50_3rounds() {
        val settings = TargetSumSettings.forDifficulty(3)
        assertEquals(3, settings.difficulty)
        assertEquals(12, settings.gridSize)
        assertEquals(5, settings.solutionSize)
        assertEquals(1, settings.minNumber)
        assertEquals(50, settings.maxNumber)
        assertEquals(3, settings.roundsCount)
    }

    @Test
    fun gridSizeIsDivisibleByColumns_forEveryDifficulty() {
        for (difficulty in 1..3) {
            val settings = TargetSumSettings.forDifficulty(difficulty)
            assertEquals(
                "сложность $difficulty: сетка должна делиться на столбцы без остатка",
                0,
                settings.gridSize % settings.columns
            )
        }
    }

    @Test
    fun outOfRangeDifficultyIsCoercedToBounds() {
        assertEquals(TargetSumSettings.forDifficulty(1), TargetSumSettings.forDifficulty(0))
        assertEquals(TargetSumSettings.forDifficulty(1), TargetSumSettings.forDifficulty(-5))
        assertEquals(TargetSumSettings.forDifficulty(3), TargetSumSettings.forDifficulty(4))
        assertEquals(TargetSumSettings.forDifficulty(3), TargetSumSettings.forDifficulty(100))
    }
}
