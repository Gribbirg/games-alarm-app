package com.example.smartalarm.feature.games.binary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Тесты чистой логики [BinaryGame]. */
class BinaryGameTest {

    /** Каноническая двоичная запись: только 0/1, без ведущих нулей. */
    private val binaryFormat = Regex("^1[01]*$")

    /** Индекс любого неверного варианта текущего вопроса. */
    private fun wrongIndex(game: BinaryGame): Int =
        game.currentQuestion.options.indices.first { it != game.currentQuestion.correctIndex }

    @Test
    fun `difficulty sets number range and target`() {
        assertEquals(15, BinaryGame(1, Random(1)).maxNumber)
        assertEquals(3, BinaryGame(1, Random(1)).targetCorrect)
        assertEquals(63, BinaryGame(2, Random(1)).maxNumber)
        assertEquals(4, BinaryGame(2, Random(1)).targetCorrect)
        assertEquals(255, BinaryGame(3, Random(1)).maxNumber)
        assertEquals(5, BinaryGame(3, Random(1)).targetCorrect)
    }

    @Test
    fun `unknown difficulty is treated as the hardest`() {
        assertEquals(255, BinaryGame(0, Random(1)).maxNumber)
        assertEquals(5, BinaryGame(0, Random(1)).targetCorrect)
        assertEquals(255, BinaryGame(4, Random(1)).maxNumber)
        assertEquals(5, BinaryGame(4, Random(1)).targetCorrect)
    }

    @Test
    fun `questions stay in range for every difficulty`() {
        for (difficulty in 1..3) {
            val game = BinaryGame(difficulty, Random(difficulty))
            repeat(200) {
                assertTrue(
                    "число ${game.currentQuestion.number} вне диапазона при сложности $difficulty",
                    game.currentQuestion.number in 1..game.maxNumber
                )
                game.answer(wrongIndex(game)) // форсируем новый вопрос
            }
        }
    }

    @Test
    fun `question is consistent with its direction`() {
        for (difficulty in 1..3) {
            val game = BinaryGame(difficulty, Random(difficulty * 7))
            repeat(200) {
                val question = game.currentQuestion
                if (question.direction == QuestionDirection.BINARY_TO_DECIMAL) {
                    // Вопрос — двоичная запись загаданного числа.
                    assertEquals(question.number, BinaryNumbers.fromBinary(question.prompt))
                    // Варианты — десятичные числа в диапазоне сложности.
                    question.options.forEach { option ->
                        assertTrue(
                            "вариант \"$option\" не десятичный или вне диапазона",
                            option.toInt() in 1..game.maxNumber
                        )
                    }
                    assertEquals(question.number.toString(), question.options[question.correctIndex])
                } else {
                    // Вопрос — десятичная запись загаданного числа.
                    assertEquals(question.number, question.prompt.toInt())
                    // Варианты — канонические двоичные записи в диапазоне сложности.
                    question.options.forEach { option ->
                        assertTrue(
                            "вариант \"$option\" не каноническая двоичная запись",
                            option.matches(binaryFormat)
                        )
                        assertTrue(
                            "вариант \"$option\" вне диапазона",
                            BinaryNumbers.fromBinary(option) in 1..game.maxNumber
                        )
                    }
                    assertEquals(
                        BinaryNumbers.toBinary(question.number),
                        question.options[question.correctIndex]
                    )
                }
                game.answer(wrongIndex(game))
            }
        }
    }

    @Test
    fun `options are four, unique and contain the correct answer`() {
        for (difficulty in 1..3) {
            val game = BinaryGame(difficulty, Random(difficulty * 100))
            repeat(200) {
                val question = game.currentQuestion
                assertEquals(4, question.options.size)
                assertEquals(4, question.options.toSet().size)
                assertTrue(question.correctIndex in 0..3)
                game.answer(wrongIndex(game))
            }
        }
    }

    @Test
    fun `consecutive questions use different numbers`() {
        val game = BinaryGame(1, Random(5))
        repeat(100) {
            val previous = game.currentQuestion.number
            game.answer(wrongIndex(game))
            assertNotEquals(previous, game.currentQuestion.number)
        }
    }

    @Test
    fun `same seed produces the same game`() {
        val first = BinaryGame(2, Random(42))
        val second = BinaryGame(2, Random(42))
        repeat(50) {
            assertEquals(first.currentQuestion, second.currentQuestion)
            // отвечаем неверно, чтобы вопросы продолжали генерироваться
            val result = first.answer(wrongIndex(first))
            assertEquals(result, second.answer(wrongIndex(second)))
        }
    }

    @Test
    fun `correct answers advance progress and reach win`() {
        val game = BinaryGame(1, Random(3))
        repeat(game.targetCorrect - 1) { step ->
            assertEquals(AnswerResult.CORRECT, game.answer(game.currentQuestion.correctIndex))
            assertEquals(step + 1, game.correctCount)
        }
        assertEquals(AnswerResult.WIN, game.answer(game.currentQuestion.correctIndex))
        assertEquals(game.targetCorrect, game.correctCount)
    }

    @Test
    fun `wrong answer keeps progress, counts mistake and changes question`() {
        val game = BinaryGame(2, Random(9))
        assertEquals(AnswerResult.CORRECT, game.answer(game.currentQuestion.correctIndex))
        val progressBefore = game.correctCount
        val questionBefore = game.currentQuestion

        assertEquals(AnswerResult.WRONG, game.answer(wrongIndex(game)))
        assertEquals(progressBefore, game.correctCount)
        assertEquals(1, game.mistakeCount)
        assertNotEquals(questionBefore, game.currentQuestion)

        // после ошибки победа по-прежнему достижима
        var result: AnswerResult
        do {
            result = game.answer(game.currentQuestion.correctIndex)
        } while (result == AnswerResult.CORRECT)
        assertEquals(AnswerResult.WIN, result)
        assertEquals(game.targetCorrect, game.correctCount)
    }

    @Test
    fun `answer rejects invalid option index`() {
        val game = BinaryGame(1, Random(11))
        assertThrows(IllegalArgumentException::class.java) { game.answer(-1) }
        assertThrows(IllegalArgumentException::class.java) { game.answer(4) }
    }
}
