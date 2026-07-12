package com.example.smartalarm.feature.games.stroop

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Тесты чистой логики [StroopGame]: генерация раундов, режимы вопроса,
 * прогресс, очки и детерминизм.
 */
class StroopGameTest {

    /** Проигрывает [rounds] раундов, отвечая верно, и отдаёт их для проверок. */
    private fun playRounds(game: StroopGame, rounds: Int): List<StroopRound> {
        val seen = ArrayList<StroopRound>()
        repeat(rounds) {
            seen.add(game.currentRound)
            game.answer(game.currentRound.correctAnswer)
            if (game.isWon) return seen
        }
        return seen
    }

    @Test
    fun `word is never equal to ink color`() {
        for (difficulty in 1..3) {
            val game = StroopGame(difficulty, Random(42))
            repeat(200) {
                val round = game.currentRound
                assertNotEquals("word == ink at difficulty $difficulty", round.word, round.ink)
                // Отвечаем неверно, чтобы игра не закончилась и раунды перегенерировались
                game.answer(game.currentRound.options.first { it != game.currentRound.correctAnswer })
            }
        }
    }

    @Test
    fun `options are unique, from pool, contain correct answer and both word and ink`() {
        for (difficulty in 1..3) {
            val game = StroopGame(difficulty, Random(7))
            val pool = game.settings.colorPool
            repeat(200) {
                val round = game.currentRound
                assertEquals(game.settings.optionsCount, round.options.size)
                assertEquals(round.options.size, round.options.toSet().size)
                assertTrue(round.options.all { it in pool })
                assertTrue(round.options.contains(round.correctAnswer))
                assertTrue(round.options.contains(round.word))
                assertTrue(round.options.contains(round.ink))
                game.answer(round.options.first { it != round.correctAnswer })
            }
        }
    }

    @Test
    fun `correct answer matches question mode`() {
        val inkRound = StroopRound(
            word = StroopColor.RED,
            ink = StroopColor.BLUE,
            mode = StroopQuestionMode.INK,
            options = listOf(StroopColor.RED, StroopColor.BLUE, StroopColor.GREEN, StroopColor.YELLOW)
        )
        assertEquals(StroopColor.BLUE, inkRound.correctAnswer)
        assertTrue(inkRound.isCorrect(StroopColor.BLUE))
        assertFalse(inkRound.isCorrect(StroopColor.RED))

        val meaningRound = inkRound.copy(mode = StroopQuestionMode.MEANING)
        assertEquals(StroopColor.RED, meaningRound.correctAnswer)
        assertTrue(meaningRound.isCorrect(StroopColor.RED))
        assertFalse(meaningRound.isCorrect(StroopColor.BLUE))
    }

    @Test
    fun `difficulty 1 and 2 never generate inverted rounds`() {
        for (difficulty in 1..2) {
            val game = StroopGame(difficulty, Random(13))
            repeat(300) {
                assertEquals(StroopQuestionMode.INK, game.currentRound.mode)
                game.answer(game.currentRound.options.first { it != game.currentRound.correctAnswer })
            }
        }
    }

    @Test
    fun `difficulty 3 generates both normal and inverted rounds`() {
        val game = StroopGame(3, Random(99))
        val modes = HashSet<StroopQuestionMode>()
        repeat(300) {
            modes.add(game.currentRound.mode)
            game.answer(game.currentRound.options.first { it != game.currentRound.correctAnswer })
        }
        assertEquals(setOf(StroopQuestionMode.INK, StroopQuestionMode.MEANING), modes)
    }

    @Test
    fun `same seed produces identical games`() {
        for (difficulty in 1..3) {
            val game1 = StroopGame(difficulty, Random(2024))
            val game2 = StroopGame(difficulty, Random(2024))
            repeat(50) {
                assertEquals(game1.currentRound, game2.currentRound)
                val answer = game1.currentRound.correctAnswer
                assertEquals(game1.answer(answer), game2.answer(answer))
            }
        }
    }

    @Test
    fun `correct answers increase progress and win after roundsToWin`() {
        val game = StroopGame(1, Random(5))
        val rounds = playRounds(game, 100)
        assertEquals(game.settings.roundsToWin, rounds.size)
        assertEquals(game.settings.roundsToWin, game.correctCount)
        assertEquals(0, game.mistakeCount)
        assertTrue(game.isWon)
    }

    @Test
    fun `mistakes do not increase progress and regenerate the round`() {
        val game = StroopGame(2, Random(11))
        val seenRounds = HashSet<StroopRound>()

        repeat(20) {
            seenRounds.add(game.currentRound)
            val wrong = game.currentRound.options.first { it != game.currentRound.correctAnswer }
            assertFalse(game.answer(wrong))
        }

        assertEquals(0, game.correctCount)
        assertEquals(20, game.mistakeCount)
        assertFalse(game.isWon)
        // После ошибки раунд перегенерируется — перебор кнопок не работает
        assertTrue(seenRounds.size > 1)
    }

    @Test
    fun `score is plus 10 per correct and minus 10 per mistake`() {
        val game = StroopGame(1, Random(3))

        val wrong = game.currentRound.options.first { it != game.currentRound.correctAnswer }
        game.answer(wrong)
        assertEquals(StroopGame.SCORE_PER_MISTAKE, game.score)

        game.answer(game.currentRound.correctAnswer)
        assertEquals(StroopGame.SCORE_PER_MISTAKE + StroopGame.SCORE_PER_CORRECT, game.score)
    }

    @Test
    fun `not won until enough correct answers`() {
        val game = StroopGame(3, Random(8))
        repeat(game.settings.roundsToWin - 1) {
            game.answer(game.currentRound.correctAnswer)
            assertFalse(game.isWon)
        }
        game.answer(game.currentRound.correctAnswer)
        assertTrue(game.isWon)
    }
}
