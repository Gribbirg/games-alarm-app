package com.example.smartalarm.feature.games.matrix

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты чистой логики игры «Запомни клетки»: генерация набора, приём отметок
 * (верная / повторная / ошибочная), перезапуск раунда после ошибки,
 * переключение раундов, условие победы и подсчёт очков.
 */
class MatrixGameLogicTest {

    /** Верно отмечает весь набор текущего раунда, возвращает последний результат. */
    private fun completeRound(logic: MatrixGameLogic): MatrixTapResult {
        var last = MatrixTapResult.CORRECT
        for (cell in logic.targetCells.toList())
            last = logic.onCellTapped(cell)
        return last
    }

    /** Клетка поля, заведомо не входящая в набор текущего раунда. */
    private fun wrongCellFor(logic: MatrixGameLogic): Int =
        (0 until logic.settings.cellsTotal).first { it !in logic.targetCells }

    // --- Генерация набора ---

    @Test
    fun initialTargetSize_matchesStartCellsForEachDifficulty() {
        for (difficulty in 1..3) {
            val logic = MatrixGameLogic(difficulty, Random(42))
            assertEquals(
                MatrixGameSettings.forDifficulty(difficulty).startCellsCount,
                logic.targetCells.size
            )
        }
    }

    @Test
    fun targetCells_areUniqueInRangeAndOfExpectedSize_acrossAllRounds() {
        for (difficulty in 1..3) {
            for (seed in 0L..30L) {
                val logic = MatrixGameLogic(difficulty, Random(seed))
                while (true) {
                    val target = logic.targetCells
                    // Set гарантирует уникальность — проверяем размер и диапазон.
                    assertEquals(
                        "Размер набора (difficulty=$difficulty, round=${logic.round}, seed=$seed)",
                        logic.settings.cellsForRound(logic.round),
                        target.size
                    )
                    for (cell in target)
                        assertTrue(
                            "Клетка $cell вне поля (difficulty=$difficulty, seed=$seed)",
                            cell in 0 until logic.settings.cellsTotal
                        )
                    if (logic.round == logic.settings.roundsCount) break
                    logic.startNextRound()
                }
            }
        }
    }

    @Test
    fun sameSeed_producesSameTargets() {
        val first = MatrixGameLogic(2, Random(123))
        val second = MatrixGameLogic(2, Random(123))
        repeat(first.settings.roundsCount - 1) {
            assertEquals(first.targetCells, second.targetCells)
            first.startNextRound()
            second.startNextRound()
        }
        assertEquals(first.targetCells, second.targetCells)
    }

    // --- Приём отметок ---

    @Test
    fun correctTaps_markCellsThenCompleteRound() {
        val logic = MatrixGameLogic(1, Random(7))
        val target = logic.targetCells.toList()
        for (i in 0 until target.size - 1) {
            assertEquals(MatrixTapResult.CORRECT, logic.onCellTapped(target[i]))
            assertEquals(i + 1, logic.markedCells.size)
            assertTrue(target[i] in logic.markedCells)
        }
        assertEquals(MatrixTapResult.ROUND_COMPLETE, logic.onCellTapped(target.last()))
    }

    @Test
    fun alreadyMarkedTap_isIgnoredWithoutPenalty() {
        val logic = MatrixGameLogic(1, Random(7))
        val cell = logic.targetCells.first()
        assertEquals(MatrixTapResult.CORRECT, logic.onCellTapped(cell))

        assertEquals(MatrixTapResult.ALREADY_MARKED, logic.onCellTapped(cell))
        assertEquals(0, logic.mistakes)
        assertEquals(0, logic.score)
        assertEquals(setOf(cell), logic.markedCells)
    }

    @Test
    fun wrongTap_penalizesClearsMarksAndStartsRoundWithNewSet() {
        val logic = MatrixGameLogic(2, Random(7))
        logic.onCellTapped(logic.targetCells.first())

        assertEquals(MatrixTapResult.MISTAKE, logic.onCellTapped(wrongCellFor(logic)))
        assertEquals(1, logic.mistakes)
        assertEquals(-MatrixGameLogic.MISTAKE_PENALTY, logic.score)
        assertTrue("Отметки не сброшены", logic.markedCells.isEmpty())
        // Раунд тот же, набор — того же размера, и игра остаётся проходимой.
        assertEquals(1, logic.round)
        assertEquals(logic.settings.cellsForRound(1), logic.targetCells.size)
        assertEquals(MatrixTapResult.ROUND_COMPLETE, completeRound(logic))
    }

    @Test
    fun setAfterMistake_isDeterministicWithSameSeed() {
        val first = MatrixGameLogic(3, Random(55))
        val second = MatrixGameLogic(3, Random(55))
        first.onCellTapped(wrongCellFor(first))
        second.onCellTapped(wrongCellFor(second))
        assertEquals(first.targetCells, second.targetCells)
    }

    @Test
    fun eachMistake_addsPenalty() {
        val logic = MatrixGameLogic(2, Random(1))
        repeat(3) { logic.onCellTapped(wrongCellFor(logic)) }
        assertEquals(3, logic.mistakes)
        assertEquals(-3 * MatrixGameLogic.MISTAKE_PENALTY, logic.score)
    }

    @Test(expected = IllegalArgumentException::class)
    fun tapOutsideField_throws() {
        MatrixGameLogic(1, Random(0)).onCellTapped(9)
    }

    // --- Раунды и победа ---

    @Test
    fun startNextRound_resetsMarksAndGrowsTarget() {
        val logic = MatrixGameLogic(2, Random(99))
        val firstRoundSize = logic.targetCells.size
        completeRound(logic)
        logic.startNextRound()

        assertEquals(2, logic.round)
        assertTrue(logic.markedCells.isEmpty())
        assertEquals(firstRoundSize + 1, logic.targetCells.size)
    }

    @Test
    fun completingAllRounds_endsWithWin() {
        for (difficulty in 1..3) {
            val logic = MatrixGameLogic(difficulty, Random(11))
            repeat(logic.settings.roundsCount - 1) {
                assertEquals(MatrixTapResult.ROUND_COMPLETE, completeRound(logic))
                logic.startNextRound()
            }
            assertEquals(MatrixTapResult.WIN, completeRound(logic))
        }
    }

    @Test(expected = IllegalStateException::class)
    fun startNextRoundAfterLastRound_throws() {
        val logic = MatrixGameLogic(1, Random(3))
        repeat(logic.settings.roundsCount) { logic.startNextRound() }
    }

    // --- Очки ---

    @Test
    fun finalScore_withoutMistakes_isTimeBonusTimesDifficulty() {
        val logic = MatrixGameLogic(2, Random(0))
        assertEquals((600 - 100) * 2, logic.finalScore(100))
    }

    @Test
    fun finalScore_subtractsMistakePenalties() {
        val logic = MatrixGameLogic(3, Random(0))
        repeat(2) { logic.onCellTapped(wrongCellFor(logic)) }
        assertEquals(
            (600 - 60) * 3 - 2 * MatrixGameLogic.MISTAKE_PENALTY,
            logic.finalScore(60)
        )
    }

    @Test
    fun finalScore_canBeNegativeForVeryLongGame() {
        val logic = MatrixGameLogic(1, Random(0))
        assertTrue(logic.finalScore(700) < 0)
    }
}
