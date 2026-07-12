package com.example.smartalarm.feature.games.equation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты игровой логики: число раундов, прогресс, ошибки, победа и детерминизм.
 */
class EquationGameTest {

    @Test
    fun totalRoundsDependOnDifficulty() {
        assertEquals(3, EquationGame(1, Random(1)).totalRounds)
        assertEquals(4, EquationGame(2, Random(1)).totalRounds)
        assertEquals(5, EquationGame(3, Random(1)).totalRounds)
    }

    @Test
    fun difficultyOutOfRangeIsCoerced() {
        assertEquals(1, EquationGame(0, Random(1)).difficulty)
        assertEquals(1, EquationGame(-5, Random(1)).difficulty)
        assertEquals(3, EquationGame(99, Random(1)).difficulty)
    }

    @Test
    fun correctAnswersLeadToWin() {
        for (difficulty in 1..3) {
            val game = EquationGame(difficulty, Random(difficulty))
            repeat(game.totalRounds - 1) { round ->
                assertEquals(
                    "Промежуточный верный ответ (раунд $round)",
                    AnswerResult.CORRECT,
                    game.submitAnswer(game.currentTask.answer)
                )
                assertEquals(round + 1, game.solvedCount)
                assertFalse(game.isWon)
            }
            assertEquals(
                "Последний верный ответ — победа",
                AnswerResult.WIN,
                game.submitAnswer(game.currentTask.answer)
            )
            assertTrue(game.isWon)
            assertEquals(game.totalRounds, game.solvedCount)
        }
    }

    @Test
    fun wrongAnswerDoesNotAdvanceProgress() {
        val game = EquationGame(1, Random(5))
        val wrongOption = game.currentTask.options.first { it != game.currentTask.answer }
        assertEquals(AnswerResult.WRONG, game.submitAnswer(wrongOption))
        assertEquals(0, game.solvedCount)
        assertFalse(game.isWon)
    }

    @Test
    fun wrongAnswerKeepsEarnedProgress() {
        val game = EquationGame(2, Random(8))
        assertEquals(AnswerResult.CORRECT, game.submitAnswer(game.currentTask.answer))
        assertEquals(1, game.solvedCount)

        val wrongOption = game.currentTask.options.first { it != game.currentTask.answer }
        assertEquals(AnswerResult.WRONG, game.submitAnswer(wrongOption))
        assertEquals("Ошибка не сбрасывает прогресс", 1, game.solvedCount)

        assertEquals(AnswerResult.CORRECT, game.submitAnswer(game.currentTask.answer))
        assertEquals(2, game.solvedCount)
    }

    @Test
    fun submitAfterWinAlwaysReturnsWinAndKeepsState() {
        val game = EquationGame(1, Random(3))
        repeat(game.totalRounds) { game.submitAnswer(game.currentTask.answer) }
        assertTrue(game.isWon)

        val taskAfterWin = game.currentTask
        assertEquals(AnswerResult.WIN, game.submitAnswer("что угодно"))
        assertEquals(game.totalRounds, game.solvedCount)
        assertEquals("Задание после победы не меняется", taskAfterWin, game.currentTask)
    }

    @Test
    fun sameSeedGivesSameGame() {
        fun playedTasks(seed: Long): List<EquationTask> {
            val game = EquationGame(3, Random(seed))
            val tasks = mutableListOf(game.currentTask)
            while (!game.isWon) {
                game.submitAnswer(game.currentTask.answer)
                tasks.add(game.currentTask)
            }
            return tasks
        }
        assertEquals(playedTasks(42), playedTasks(42))
    }
}
