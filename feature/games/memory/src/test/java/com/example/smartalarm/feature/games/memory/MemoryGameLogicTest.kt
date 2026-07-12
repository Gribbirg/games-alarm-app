package com.example.smartalarm.feature.games.memory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты чистой логики игры «Повтори узор»: генерация последовательности,
 * пошаговая проверка ввода, рост раундов, условие победы и подсчёт очков.
 */
class MemoryGameLogicTest {

    /** Верно повторяет всю последовательность текущего раунда, возвращает последний результат. */
    private fun completeRound(logic: MemoryGameLogic): TapResult {
        var last = TapResult.CORRECT
        for (cell in logic.sequence)
            last = logic.onCellTapped(cell)
        return last
    }

    /** Ячейка, заведомо не совпадающая с ожидаемой на текущем шаге. */
    private fun wrongCellFor(logic: MemoryGameLogic): Int =
        (logic.sequence[logic.inputProgress] + 1) % MemoryGameLogic.CELLS_COUNT

    // --- Генерация ---

    @Test
    fun initialSequenceLength_matchesStartLengthForEachDifficulty() {
        for (difficulty in 1..3) {
            val logic = MemoryGameLogic(difficulty, Random(42))
            assertEquals(
                MemoryGameSettings.forDifficulty(difficulty).startLength,
                logic.sequence.size
            )
        }
    }

    @Test
    fun sequenceCells_areAlwaysInGridRange() {
        for (seed in 0L..50L) {
            val logic = MemoryGameLogic(3, Random(seed))
            while (true) {
                for (cell in logic.sequence)
                    assertTrue("Ячейка $cell вне 0..8", cell in 0 until MemoryGameLogic.CELLS_COUNT)
                if (logic.round == logic.settings.roundsCount) break
                logic.startNextRound()
            }
        }
    }

    @Test
    fun sequence_hasNoAdjacentDuplicates() {
        for (seed in 0L..50L) {
            val logic = MemoryGameLogic(3, Random(seed))
            while (true) {
                val sequence = logic.sequence
                for (i in 1 until sequence.size)
                    assertNotEquals(
                        "Подряд идущие одинаковые ячейки (seed=$seed)",
                        sequence[i - 1],
                        sequence[i]
                    )
                if (logic.round == logic.settings.roundsCount) break
                logic.startNextRound()
            }
        }
    }

    @Test
    fun sameSeed_producesSameSequences() {
        val first = MemoryGameLogic(2, Random(123))
        val second = MemoryGameLogic(2, Random(123))
        repeat(first.settings.roundsCount - 1) {
            assertEquals(first.sequence, second.sequence)
            first.startNextRound()
            second.startNextRound()
        }
        assertEquals(first.sequence, second.sequence)
    }

    // --- Проверка ввода ---

    @Test
    fun correctTaps_progressThenCompleteRound() {
        val logic = MemoryGameLogic(1, Random(7))
        val sequence = logic.sequence
        for (i in 0 until sequence.size - 1) {
            assertEquals(TapResult.CORRECT, logic.onCellTapped(sequence[i]))
            assertEquals(i + 1, logic.inputProgress)
        }
        assertEquals(TapResult.ROUND_COMPLETE, logic.onCellTapped(sequence.last()))
        assertEquals(0, logic.inputProgress)
    }

    @Test
    fun wrongTap_returnsMistakePenalizesAndResetsProgress() {
        val logic = MemoryGameLogic(1, Random(7))
        assertEquals(TapResult.CORRECT, logic.onCellTapped(logic.sequence[0]))

        assertEquals(TapResult.MISTAKE, logic.onCellTapped(wrongCellFor(logic)))
        assertEquals(1, logic.mistakes)
        assertEquals(-MemoryGameLogic.MISTAKE_PENALTY, logic.score)
        assertEquals(0, logic.inputProgress)

        // После ошибки последовательность повторяется с начала и остаётся проходимой.
        assertEquals(TapResult.ROUND_COMPLETE, completeRound(logic))
    }

    @Test
    fun eachMistake_addsPenalty() {
        val logic = MemoryGameLogic(2, Random(1))
        repeat(3) { logic.onCellTapped(wrongCellFor(logic)) }
        assertEquals(3, logic.mistakes)
        assertEquals(-3 * MemoryGameLogic.MISTAKE_PENALTY, logic.score)
    }

    // --- Рост раундов ---

    @Test
    fun startNextRound_growsSequenceByOneAndKeepsPrefix() {
        val logic = MemoryGameLogic(2, Random(99))
        val before = logic.sequence
        logic.startNextRound()
        val after = logic.sequence

        assertEquals(2, logic.round)
        assertEquals(before.size + 1, after.size)
        assertEquals(before, after.subList(0, before.size))
    }

    @Test
    fun lastRoundSequence_hasFinalLength() {
        val logic = MemoryGameLogic(3, Random(5))
        repeat(logic.settings.roundsCount - 1) { logic.startNextRound() }
        assertEquals(logic.settings.roundsCount, logic.round)
        assertEquals(logic.settings.finalLength, logic.sequence.size)
    }

    // --- Победа ---

    @Test
    fun completingAllRounds_endsWithWin() {
        for (difficulty in 1..3) {
            val logic = MemoryGameLogic(difficulty, Random(11))
            repeat(logic.settings.roundsCount - 1) {
                assertEquals(TapResult.ROUND_COMPLETE, completeRound(logic))
                logic.startNextRound()
            }
            assertEquals(TapResult.WIN, completeRound(logic))
        }
    }

    @Test(expected = IllegalStateException::class)
    fun startNextRoundAfterLastRound_throws() {
        val logic = MemoryGameLogic(1, Random(3))
        repeat(logic.settings.roundsCount) { logic.startNextRound() }
    }

    // --- Очки ---

    @Test
    fun finalScore_withoutMistakes_isTimeBonusTimesDifficulty() {
        val logic = MemoryGameLogic(2, Random(0))
        assertEquals((600 - 100) * 2, logic.finalScore(100))
    }

    @Test
    fun finalScore_subtractsMistakePenalties() {
        val logic = MemoryGameLogic(3, Random(0))
        repeat(2) { logic.onCellTapped(wrongCellFor(logic)) }
        assertEquals(
            (600 - 60) * 3 - 2 * MemoryGameLogic.MISTAKE_PENALTY,
            logic.finalScore(60)
        )
    }

    @Test
    fun finalScore_canBeNegativeForVeryLongGame() {
        val logic = MemoryGameLogic(1, Random(0))
        assertTrue(logic.finalScore(700) < 0)
    }
}
