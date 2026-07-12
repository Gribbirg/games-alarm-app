package com.example.smartalarm.feature.games.targetsum

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Тесты чистой логики игры «Набери сумму»:
 * генерация раунда, выделение/снятие, проверка суммы, раунды и очки.
 */
class TargetSumLogicTest {

    // ---------- Генерация раунда ----------

    @Test
    fun generatedRoundHasCorrectSizesAndRanges_forEveryDifficulty() {
        for (difficulty in 1..3) {
            val settings = TargetSumSettings.forDifficulty(difficulty)
            for (seed in 0..49) {
                val round = generateTargetSumRound(settings, Random(seed))

                assertEquals(settings.gridSize, round.numbers.size)
                for (number in round.numbers)
                    assertTrue(
                        "число $number вне диапазона ${settings.minNumber}..${settings.maxNumber}",
                        number in settings.minNumber..settings.maxNumber
                    )

                assertEquals(settings.solutionSize, round.solution.size)
                for (index in round.solution)
                    assertTrue("индекс решения $index вне поля", index in round.numbers.indices)
            }
        }
    }

    @Test
    fun targetEqualsSumOfSolutionCells() {
        for (difficulty in 1..3) {
            val settings = TargetSumSettings.forDifficulty(difficulty)
            for (seed in 0..49) {
                val round = generateTargetSumRound(settings, Random(seed))
                assertEquals(round.solution.sumOf { round.numbers[it] }, round.target)
            }
        }
    }

    @Test
    fun subsetWithTargetSumAlwaysExists_bruteForce() {
        for (difficulty in 1..3) {
            val settings = TargetSumSettings.forDifficulty(difficulty)
            for (seed in 0..19) {
                val round = generateTargetSumRound(settings, Random(seed))
                assertTrue(
                    "нет подмножества с суммой ${round.target} (сложность $difficulty, seed $seed)",
                    anySubsetSums(round.numbers, round.target)
                )
            }
        }
    }

    @Test
    fun generationIsDeterministicWithSameSeed() {
        val settings = TargetSumSettings.forDifficulty(2)
        assertEquals(
            generateTargetSumRound(settings, Random(42)),
            generateTargetSumRound(settings, Random(42))
        )
        // Игра с тем же seed тоже воспроизводима.
        assertEquals(
            TargetSumGame(3, Random(7)).currentRound,
            TargetSumGame(3, Random(7)).currentRound
        )
    }

    // ---------- Выделение ----------

    @Test
    fun toggleSelectsAndDeselects_andSumIsLive() {
        val game = TargetSumGame(1, Random(1))
        assertEquals(0, game.selectedSum)
        assertTrue(game.selectedIndices.isEmpty())

        assertTrue(game.toggle(0))
        assertTrue(game.toggle(2))
        assertEquals(setOf(0, 2), game.selectedIndices)
        assertEquals(game.numbers[0] + game.numbers[2], game.selectedSum)

        assertFalse(game.toggle(0))
        assertEquals(setOf(2), game.selectedIndices)
        assertEquals(game.numbers[2], game.selectedSum)
    }

    @Test
    fun toggleOutOfRangeIsIgnored() {
        val game = TargetSumGame(1, Random(1))
        assertFalse(game.toggle(-1))
        assertFalse(game.toggle(game.numbers.size))
        assertTrue(game.selectedIndices.isEmpty())
        assertEquals(0, game.selectedSum)
    }

    @Test
    fun clearSelectionRemovesEverything() {
        val game = TargetSumGame(1, Random(1))
        game.toggle(0)
        game.toggle(1)
        game.clearSelection()
        assertTrue(game.selectedIndices.isEmpty())
        assertEquals(0, game.selectedSum)
    }

    // ---------- Проверка ----------

    @Test
    fun wrongCheckIsMistake_penalizesAndKeepsSelection() {
        val game = TargetSumGame(1, Random(1))
        // Одна ячейка решения — сумма заведомо меньше цели
        // (в решении 3 ячейки со значениями >= 1).
        val index = game.currentRound.solution.first()
        game.toggle(index)
        assertNotEquals(game.target, game.selectedSum)

        assertEquals(CheckResult.MISTAKE, game.check())
        assertEquals(1, game.mistakes)
        assertEquals(-TargetSumGame.MISTAKE_PENALTY, game.score)
        assertEquals("выделение должно сохраниться", setOf(index), game.selectedIndices)
        assertEquals("раунд не должен смениться", 1, game.roundNumber)

        assertEquals(CheckResult.MISTAKE, game.check())
        assertEquals(2, game.mistakes)
        assertEquals(-2 * TargetSumGame.MISTAKE_PENALTY, game.score)
    }

    @Test
    fun emptySelectionIsMistake_targetIsAlwaysPositive() {
        for (difficulty in 1..3) {
            val game = TargetSumGame(difficulty, Random(difficulty))
            assertTrue(game.target > 0)
            assertEquals(CheckResult.MISTAKE, game.check())
        }
    }

    @Test
    fun correctCheckCompletesRound() {
        val game = TargetSumGame(1, Random(3))
        selectSolution(game)
        assertEquals(game.target, game.selectedSum)
        assertEquals(CheckResult.ROUND_COMPLETE, game.check())
        assertEquals("верная проверка не меняет счёт", 0, game.score)
    }

    @Test
    fun alternativeSubsetWithExactSumIsAccepted() {
        // Перебираем seed'ы, пока не найдём раунд, где точную сумму даёт
        // подмножество, отличное от построенного решения, — оно тоже
        // должно приниматься.
        for (seed in 0..2000) {
            val game = TargetSumGame(1, Random(seed))
            val round = game.currentRound
            val alternative = findSubset(round.numbers, round.target) { it != round.solution }
                ?: continue

            for (index in alternative)
                game.toggle(index)
            assertEquals(round.target, game.selectedSum)
            assertEquals(CheckResult.ROUND_COMPLETE, game.check())
            assertEquals(0, game.mistakes)
            return
        }
        fail("среди 2001 seed'а не нашлось раунда с альтернативным решением")
    }

    // ---------- Раунды и победа ----------

    @Test
    fun playingAllRoundsLeadsToWin_forEveryDifficulty() {
        for (difficulty in 1..3) {
            val game = TargetSumGame(difficulty, Random(difficulty * 100))
            val rounds = game.settings.roundsCount

            for (round in 1 until rounds) {
                assertEquals(round, game.roundNumber)
                selectSolution(game)
                assertEquals(CheckResult.ROUND_COMPLETE, game.check())
                game.startNextRound()
                assertTrue("новый раунд начинается без выделения", game.selectedIndices.isEmpty())
            }

            assertEquals(rounds, game.roundNumber)
            selectSolution(game)
            assertEquals(CheckResult.WIN, game.check())
        }
    }

    @Test
    fun startNextRoundGeneratesNewRoundAndResetsSelection() {
        val game = TargetSumGame(1, Random(5))
        val firstRound = game.currentRound
        game.toggle(0)

        game.startNextRound()

        assertEquals(2, game.roundNumber)
        assertTrue(game.selectedIndices.isEmpty())
        assertEquals(0, game.selectedSum)
        assertNotEquals("должен сгенерироваться новый раунд", firstRound, game.currentRound)
    }

    @Test
    fun startNextRoundAfterLastRoundThrows() {
        val game = TargetSumGame(1, Random(6))
        game.startNextRound() // раунд 2 из 2 — последний
        try {
            game.startNextRound()
            fail("ожидался IllegalStateException")
        } catch (expected: IllegalStateException) {
            // ок
        }
    }

    // ---------- Очки ----------

    @Test
    fun finalScoreUsesCalcFormulaWithPenalties() {
        val game = TargetSumGame(2, Random(8))
        // Две ошибки: −20.
        game.check()
        game.check()
        assertEquals(-20 + (600 - 100) * 2, game.finalScore(100))
    }

    @Test
    fun finalScoreWithoutMistakes() {
        val game = TargetSumGame(3, Random(9))
        assertEquals((600 - 60) * 3, game.finalScore(60))
    }

    // ---------- Вспомогательные ----------

    /** Выделяет в [game] построенное решение текущего раунда. */
    private fun selectSolution(game: TargetSumGame) {
        game.clearSelection()
        for (index in game.currentRound.solution)
            game.toggle(index)
    }

    /** Есть ли непустое подмножество [numbers] с суммой [target] (полный перебор). */
    private fun anySubsetSums(numbers: List<Int>, target: Int): Boolean =
        findSubset(numbers, target) { true } != null

    /**
     * Ищет полным перебором непустое подмножество индексов [numbers]
     * с суммой [target], удовлетворяющее [predicate]; `null`, если такого нет.
     */
    private fun findSubset(
        numbers: List<Int>,
        target: Int,
        predicate: (Set<Int>) -> Boolean
    ): Set<Int>? {
        for (mask in 1 until (1 shl numbers.size)) {
            val indices = numbers.indices.filter { mask and (1 shl it) != 0 }.toSet()
            if (indices.sumOf { numbers[it] } == target && predicate(indices))
                return indices
        }
        return null
    }
}
