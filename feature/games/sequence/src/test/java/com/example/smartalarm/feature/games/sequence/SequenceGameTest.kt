package com.example.smartalarm.feature.games.sequence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты чистого состояния партии [SequenceGame]: прогресс, ошибки, победа.
 */
class SequenceGameTest {

    @Test
    fun totalRounds_matchesDifficulty() {
        assertEquals(3, SequenceGame(1, Random(1)).totalRounds)
        assertEquals(4, SequenceGame(2, Random(1)).totalRounds)
        assertEquals(5, SequenceGame(3, Random(1)).totalRounds)
    }

    @Test
    fun newGame_startsAtFirstRoundNotFinished() {
        val game = SequenceGame(2, Random(7))
        assertEquals(0, game.roundsSolved)
        assertEquals(0, game.mistakes)
        assertEquals(1, game.currentRoundNumber)
        assertFalse(game.isFinished)
    }

    @Test
    fun correctAnswer_advancesRoundAndChangesTask() {
        val game = SequenceGame(1, Random(42))
        val firstTask = game.currentTask
        assertTrue(game.submitAnswer(firstTask.answer))
        assertEquals(1, game.roundsSolved)
        assertEquals(2, game.currentRoundNumber)
        assertEquals(0, game.mistakes)
        assertFalse(game.isFinished)
        // После верного ответа (партия не окончена) выдано новое задание.
        assertFalse(firstTask === game.currentTask)
    }

    @Test
    fun wrongAnswer_countsMistakeAndKeepsTask() {
        val game = SequenceGame(3, Random(5))
        val task = game.currentTask
        val wrong = task.options.first { it != task.answer }
        assertFalse(game.submitAnswer(wrong))
        assertEquals(0, game.roundsSolved)
        assertEquals(1, game.mistakes)
        assertTrue("задание не меняется после ошибки", task === game.currentTask)
        assertFalse(game.isFinished)
        // Ошибка не мешает затем ответить верно на тот же ряд.
        assertTrue(game.submitAnswer(task.answer))
        assertEquals(1, game.roundsSolved)
    }

    @Test
    fun solvingAllRounds_finishesGame() {
        for (difficulty in 1..3) {
            val game = SequenceGame(difficulty, Random(difficulty.toLong()))
            repeat(game.totalRounds) {
                assertFalse(game.isFinished)
                assertTrue(game.submitAnswer(game.currentTask.answer))
            }
            assertTrue(game.isFinished)
            assertEquals(game.totalRounds, game.roundsSolved)
            assertEquals(game.totalRounds, game.currentRoundNumber)
        }
    }

    @Test
    fun answersAfterFinish_areIgnored() {
        val game = SequenceGame(1, Random(3))
        repeat(game.totalRounds) { game.submitAnswer(game.currentTask.answer) }
        assertTrue(game.isFinished)
        val solved = game.roundsSolved
        val mistakes = game.mistakes
        assertFalse(game.submitAnswer(game.currentTask.answer))
        assertEquals(solved, game.roundsSolved)
        assertEquals(mistakes, game.mistakes)
    }

    @Test
    fun sameSeed_producesSameTaskSequence() {
        val first = SequenceGame(3, Random(123))
        val second = SequenceGame(3, Random(123))
        repeat(first.totalRounds) {
            assertEquals(first.currentTask, second.currentTask)
            first.submitAnswer(first.currentTask.answer)
            second.submitAnswer(second.currentTask.answer)
        }
        assertTrue(first.isFinished && second.isFinished)
    }

    @Test
    fun difficultyOneGame_generatesOnlyArithmetic() {
        for (seed in 0..49) {
            val game = SequenceGame(1, Random(seed))
            repeat(game.totalRounds) {
                assertEquals(SequenceType.ARITHMETIC, game.currentTask.type)
                game.submitAnswer(game.currentTask.answer)
            }
        }
    }

    @Test
    fun outOfRangeDifficulty_isCoerced() {
        assertEquals(3, SequenceGame(-1, Random(1)).totalRounds)
        assertEquals(5, SequenceGame(100, Random(1)).totalRounds)
    }
}
