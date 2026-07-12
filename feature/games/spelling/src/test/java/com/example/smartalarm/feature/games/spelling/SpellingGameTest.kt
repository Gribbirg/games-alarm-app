package com.example.smartalarm.feature.games.spelling

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты чистой логики игры «Как пишется?» ([SpellingGame]).
 */
class SpellingGameTest {

    private val testEntries = listOf(
        SpellingEntry("корова", listOf("карова")),
        SpellingEntry("молоко", listOf("малоко", "молако", "малако")),
        SpellingEntry("собака", listOf("сабака", "собако")),
        SpellingEntry("заяц", listOf("заец", "заиц")),
        SpellingEntry("мороз", listOf("мароз"))
    )

    private fun correctOf(question: SpellingQuestion): String =
        question.options[question.correctIndex]

    private fun entryFor(question: SpellingQuestion): SpellingEntry =
        testEntries.single { it.correct == correctOf(question) }

    @Test
    fun `question contains exactly one correct option taken from one entry`() {
        val game = SpellingGame(testEntries, 5, Random(1))
        repeat(5) {
            val question = game.currentQuestion
            val entry = entryFor(question)

            // Все варианты вопроса — это варианты одной записи, без лишних и без потерь
            assertEquals(
                (entry.wrong + entry.correct).sorted(),
                question.options.sorted()
            )
            // Правильный вариант ровно один
            assertEquals(1, question.options.count { it == entry.correct })
            assertEquals(entry.correct, question.options[question.correctIndex])

            game.answer(question.correctIndex)
        }
    }

    @Test
    fun `shuffling is deterministic with a fixed seed`() {
        val gameA = SpellingGame(testEntries, 5, Random(42))
        val gameB = SpellingGame(testEntries, 5, Random(42))
        repeat(4) {
            assertEquals(gameA.currentQuestion, gameB.currentQuestion)
            gameA.answer(gameA.currentQuestion.correctIndex)
            gameB.answer(gameB.currentQuestion.correctIndex)
        }
    }

    @Test
    fun `entries do not repeat during a game`() {
        val game = SpellingGame(testEntries, testEntries.size, Random(7))
        val seenWords = mutableListOf<String>()
        repeat(testEntries.size) {
            seenWords.add(correctOf(game.currentQuestion))
            game.answer(game.currentQuestion.correctIndex)
        }
        assertEquals(seenWords.size, seenWords.toSet().size)
    }

    @Test
    fun `correct answers increase progress and finish with WIN`() {
        val game = SpellingGame(testEntries, 3, Random(3))

        assertEquals(AnswerResult.CORRECT, game.answer(game.currentQuestion.correctIndex))
        assertEquals(1, game.correctCount)

        assertEquals(AnswerResult.CORRECT, game.answer(game.currentQuestion.correctIndex))
        assertEquals(2, game.correctCount)

        assertEquals(AnswerResult.WIN, game.answer(game.currentQuestion.correctIndex))
        assertEquals(3, game.correctCount)
        assertTrue(game.isWon)
        assertEquals(0, game.mistakeCount)
    }

    @Test
    fun `wrong answer counts a mistake and shows a new word`() {
        val game = SpellingGame(testEntries, 3, Random(11))
        val firstQuestion = game.currentQuestion
        val wrongIndex = (firstQuestion.correctIndex + 1) % firstQuestion.options.size

        assertEquals(AnswerResult.WRONG, game.answer(wrongIndex))
        assertEquals(1, game.mistakeCount)
        assertEquals(0, game.correctCount)
        // Показано другое слово
        assertNotEquals(correctOf(firstQuestion), correctOf(game.currentQuestion))
    }

    @Test
    fun `pool is recycled when exhausted by mistakes`() {
        val twoEntries = testEntries.take(2)
        val game = SpellingGame(twoEntries, 4, Random(5))
        // Ошибаемся больше раз, чем записей в пуле — игра не должна падать
        repeat(6) {
            val question = game.currentQuestion
            val wrongIndex = (question.correctIndex + 1) % question.options.size
            assertEquals(AnswerResult.WRONG, game.answer(wrongIndex))
        }
        assertEquals(6, game.mistakeCount)
        // И победа после этого всё ещё достижима
        repeat(4) {
            game.answer(game.currentQuestion.correctIndex)
        }
        assertTrue(game.isWon)
    }

    @Test
    fun `answers after the win do not change the state`() {
        val game = SpellingGame(testEntries, 1, Random(9))
        assertEquals(AnswerResult.WIN, game.answer(game.currentQuestion.correctIndex))
        val questionAfterWin = game.currentQuestion

        assertEquals(AnswerResult.WIN, game.answer(0))
        assertEquals(1, game.correctCount)
        assertEquals(0, game.mistakeCount)
        assertEquals(questionAfterWin, game.currentQuestion)
    }

    @Test
    fun `targetCorrectFor maps difficulty to 4-6 and clamps out-of-range values`() {
        assertEquals(4, SpellingGame.targetCorrectFor(1))
        assertEquals(5, SpellingGame.targetCorrectFor(2))
        assertEquals(6, SpellingGame.targetCorrectFor(3))
        assertEquals(4, SpellingGame.targetCorrectFor(0))
        assertEquals(6, SpellingGame.targetCorrectFor(10))
    }

    @Test
    fun `finishBonus follows the calc formula`() {
        assertEquals(600 - 100, SpellingGame.finishBonus(100, 1))
        assertEquals((600 - 60) * 3, SpellingGame.finishBonus(60, 3))
        assertEquals(-(700 - 600) * 2, SpellingGame.finishBonus(700, 2))
    }

    @Test
    fun `real database levels can be won`() {
        for (difficulty in 1..3) {
            val game = SpellingGame(
                SpellingData.entriesForDifficulty(difficulty),
                SpellingGame.targetCorrectFor(difficulty),
                Random(difficulty.toLong())
            )
            while (!game.isWon) {
                game.answer(game.currentQuestion.correctIndex)
            }
            assertEquals(SpellingGame.targetCorrectFor(difficulty), game.correctCount)
        }
    }
}
