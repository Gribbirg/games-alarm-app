package com.example.smartalarm.feature.games.truefalse

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты логики партии: цель серии по сложности, рост серии, победа
 * при N правильных ответах подряд, сброс серии при ошибке.
 */
class TrueFalseGameTest {

    @Test
    fun targetStreakDependsOnDifficulty() {
        assertEquals(5, TrueFalseGame(1, Random(1)).targetStreak)
        assertEquals(7, TrueFalseGame(2, Random(1)).targetStreak)
        assertEquals(10, TrueFalseGame(3, Random(1)).targetStreak)
        // Значения вне диапазона приводятся к границе.
        assertEquals(5, TrueFalseGame(0, Random(1)).targetStreak)
        assertEquals(10, TrueFalseGame(99, Random(1)).targetStreak)
    }

    @Test
    fun correctAnswersGrowStreakAndLeadToWin() {
        val game = TrueFalseGame(2, Random(42))
        repeat(game.targetStreak - 1) { i ->
            assertEquals(AnswerResult.CORRECT, game.answer(game.currentStatement.isTrue))
            assertEquals(i + 1, game.streak)
        }
        assertEquals(AnswerResult.WIN, game.answer(game.currentStatement.isTrue))
        assertEquals(game.targetStreak, game.streak)
    }

    @Test
    fun wrongAnswerResetsStreak() {
        val game = TrueFalseGame(3, Random(7))
        repeat(4) {
            assertEquals(AnswerResult.CORRECT, game.answer(game.currentStatement.isTrue))
        }
        assertEquals(4, game.streak)
        assertEquals(AnswerResult.WRONG, game.answer(!game.currentStatement.isTrue))
        assertEquals(0, game.streak)
    }

    @Test
    fun winIsPossibleAfterMistake() {
        val game = TrueFalseGame(1, Random(9))
        assertEquals(AnswerResult.WRONG, game.answer(!game.currentStatement.isTrue))
        repeat(game.targetStreak - 1) {
            assertEquals(AnswerResult.CORRECT, game.answer(game.currentStatement.isTrue))
        }
        assertEquals(AnswerResult.WIN, game.answer(game.currentStatement.isTrue))
    }

    @Test
    fun mistakeOnLastStepRequiresFullStreakAgain() {
        val game = TrueFalseGame(1, Random(11))
        repeat(game.targetStreak - 1) {
            assertEquals(AnswerResult.CORRECT, game.answer(game.currentStatement.isTrue))
        }
        // Ошибка на последнем шаге — серия обнуляется, победы нет.
        assertEquals(AnswerResult.WRONG, game.answer(!game.currentStatement.isTrue))
        assertEquals(0, game.streak)
        // Для победы снова нужна полная серия.
        repeat(game.targetStreak - 1) {
            assertEquals(AnswerResult.CORRECT, game.answer(game.currentStatement.isTrue))
        }
        assertEquals(AnswerResult.WIN, game.answer(game.currentStatement.isTrue))
    }
}
