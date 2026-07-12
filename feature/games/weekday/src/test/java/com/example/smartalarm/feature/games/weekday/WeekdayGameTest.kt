package com.example.smartalarm.feature.games.weekday

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты состояния партии [WeekdayGame]: прогресс, ошибки, победа.
 */
class WeekdayGameTest {

    @Test
    fun `число вопросов для победы по сложностям`() {
        assertEquals(3, WeekdayGame.questionsToWin(1))
        assertEquals(4, WeekdayGame.questionsToWin(2))
        assertEquals(5, WeekdayGame.questionsToWin(3))
        // Некорректная сложность трактуется как лёгкая
        assertEquals(3, WeekdayGame.questionsToWin(0))
        assertEquals(3, WeekdayGame.questionsToWin(42))
    }

    @Test
    fun `новая партия не выиграна и без ошибок`() {
        val game = WeekdayGame(1, Random(1))
        assertFalse(game.isWon)
        assertEquals(0, game.correctAnswers)
        assertEquals(0, game.mistakes)
    }

    @Test
    fun `верный ответ увеличивает счётчик`() {
        val game = WeekdayGame(1, Random(1))
        assertTrue(game.answer(game.currentQuestion.correctAnswer))
        assertEquals(1, game.correctAnswers)
        assertEquals(0, game.mistakes)
        assertFalse(game.isWon)
    }

    @Test
    fun `победа после нужного числа верных ответов`() {
        for (difficulty in 1..3) {
            val game = WeekdayGame(difficulty, Random(7))
            repeat(game.questionsToWin - 1) {
                assertTrue(game.answer(game.currentQuestion.correctAnswer))
                assertFalse(game.isWon)
            }
            assertTrue(game.answer(game.currentQuestion.correctAnswer))
            assertTrue(game.isWon)
            assertEquals(game.questionsToWin, game.correctAnswers)
        }
    }

    @Test
    fun `ошибка не меняет счётчик верных и даёт новый вопрос`() {
        val game = WeekdayGame(2, Random(11))
        val wrongAnswers = ArrayList<Weekday>()
        val questionTexts = HashSet<String>()
        repeat(20) {
            questionTexts.add(game.currentQuestion.text)
            val wrong = game.currentQuestion.options
                .first { it != game.currentQuestion.correctAnswer }
            wrongAnswers.add(wrong)
            assertFalse(game.answer(wrong))
        }
        assertEquals(0, game.correctAnswers)
        assertEquals(20, game.mistakes)
        assertFalse(game.isWon)
        // После ошибок генерируются новые вопросы (тексты меняются)
        assertTrue(questionTexts.size > 1)
    }

    @Test
    fun `после ошибки можно выиграть без сброса прогресса`() {
        val game = WeekdayGame(1, Random(3))
        assertTrue(game.answer(game.currentQuestion.correctAnswer))
        val wrong = game.currentQuestion.options
            .first { it != game.currentQuestion.correctAnswer }
        assertFalse(game.answer(wrong))
        assertEquals(1, game.correctAnswers)
        while (!game.isWon) {
            assertTrue(game.answer(game.currentQuestion.correctAnswer))
        }
        assertEquals(game.questionsToWin, game.correctAnswers)
        assertEquals(1, game.mistakes)
    }

    @Test
    fun `ответы после победы игнорируются`() {
        val game = WeekdayGame(1, Random(5))
        repeat(game.questionsToWin) {
            game.answer(game.currentQuestion.correctAnswer)
        }
        assertTrue(game.isWon)
        val before = game.correctAnswers
        assertFalse(game.answer(game.currentQuestion.correctAnswer))
        assertEquals(before, game.correctAnswers)
        assertEquals(0, game.mistakes)
    }

    @Test
    fun `детерминизм партии при одинаковом seed`() {
        val first = playAndCollectTexts(WeekdayGame(3, Random(42)))
        val second = playAndCollectTexts(WeekdayGame(3, Random(42)))
        assertEquals(first, second)
    }

    @Test
    fun `вопросы партии соответствуют её сложности`() {
        for (difficulty in 1..3) {
            val game = WeekdayGame(difficulty, Random(13))
            val range = WeekdayQuestionFactory.offsetRangeFor(difficulty)
            repeat(30) {
                val offset = game.currentQuestion.offset
                val magnitude = if (offset > 0) offset else -offset
                assertTrue(magnitude in range)
                val wrong = game.currentQuestion.options
                    .first { it != game.currentQuestion.correctAnswer }
                game.answer(wrong)
            }
        }
    }

    private fun playAndCollectTexts(game: WeekdayGame): List<String> {
        val texts = ArrayList<String>()
        while (!game.isWon) {
            texts.add(game.currentQuestion.text)
            game.answer(game.currentQuestion.correctAnswer)
        }
        return texts
    }
}
