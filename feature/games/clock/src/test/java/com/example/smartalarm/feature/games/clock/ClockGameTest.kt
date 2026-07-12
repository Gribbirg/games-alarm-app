package com.example.smartalarm.feature.games.clock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import kotlin.random.Random
import org.junit.Test

/**
 * Тесты чистой логики [ClockGame]: параметры сложности, генерация раундов
 * и вариантов ответа, детерминизм по seed, прохождение и обработка ошибок.
 */
class ClockGameTest {

    @Test
    fun difficultyDefinesMinuteStepAndRounds() {
        assertEquals(30, ClockGame(1, Random(0)).minuteStep)
        assertEquals(3, ClockGame(1, Random(0)).totalRounds)
        assertEquals(15, ClockGame(2, Random(0)).minuteStep)
        assertEquals(4, ClockGame(2, Random(0)).totalRounds)
        assertEquals(5, ClockGame(3, Random(0)).minuteStep)
        assertEquals(5, ClockGame(3, Random(0)).totalRounds)
    }

    @Test
    fun unknownDifficultyFallsBackToEasiest() {
        val game = ClockGame(0, Random(0))
        assertEquals(30, game.minuteStep)
        assertEquals(3, game.totalRounds)
    }

    @Test
    fun correctTimeMinutesAreMultipleOfStepInEveryRound() {
        for (difficulty in 1..3) {
            for (seed in 0 until 30) {
                val game = ClockGame(difficulty, Random(seed))
                while (!game.isFinished) {
                    assertEquals(
                        "difficulty=$difficulty seed=$seed time=${game.currentRound.time}",
                        0, game.currentRound.time.minutes % game.minuteStep
                    )
                    game.answer(game.currentRound.time)
                }
            }
        }
    }

    @Test
    fun optionsAreUniqueValidAndContainCorrectAnswer() {
        for (difficulty in 1..3) {
            for (seed in 0 until 30) {
                val game = ClockGame(difficulty, Random(seed))
                while (!game.isFinished) {
                    val round = game.currentRound
                    assertEquals(ClockGame.OPTIONS_COUNT, round.options.size)
                    assertEquals(
                        "options must be unique: ${round.options}",
                        ClockGame.OPTIONS_COUNT, round.options.toSet().size
                    )
                    assertTrue(
                        "options must contain the correct time",
                        round.time in round.options
                    )
                    for (option in round.options) {
                        assertTrue(option.hours in 1..12)
                        assertTrue(option.minutes in 0..59)
                    }
                    game.answer(round.time)
                }
            }
        }
    }

    @Test
    fun sameSeedProducesSameGame() {
        val first = ClockGame(3, Random(42))
        val second = ClockGame(3, Random(42))
        while (!first.isFinished) {
            assertEquals(first.currentRound, second.currentRound)
            first.answer(first.currentRound.time)
            second.answer(second.currentRound.time)
        }
        assertTrue(second.isFinished)
    }

    @Test
    fun correctAnswersAdvanceRoundsUpToVictory() {
        val game = ClockGame(1, Random(7))
        assertEquals(1, game.roundNumber)
        assertFalse(game.isFinished)

        assertTrue(game.answer(game.currentRound.time))
        assertEquals(2, game.roundNumber)
        assertFalse(game.isFinished)

        assertTrue(game.answer(game.currentRound.time))
        assertEquals(3, game.roundNumber)
        assertFalse(game.isFinished)

        assertTrue(game.answer(game.currentRound.time))
        assertEquals(4, game.roundNumber)
        assertTrue(game.isFinished)
    }

    @Test
    fun wrongAnswerKeepsRoundNumberAndReplacesRound() {
        val game = ClockGame(2, Random(11))
        val oldTime = game.currentRound.time
        val wrongOption = game.currentRound.options.first { it != oldTime }

        assertFalse(game.answer(wrongOption))

        assertEquals(1, game.roundNumber)
        assertFalse(game.isFinished)
        assertNotEquals("a new task must be generated", oldTime, game.currentRound.time)
    }

    @Test
    fun consecutiveRoundTimesDiffer() {
        for (seed in 0 until 30) {
            val game = ClockGame(1, Random(seed))
            var previous = game.currentRound.time
            while (!game.isFinished) {
                game.answer(game.currentRound.time)
                if (!game.isFinished) {
                    assertNotEquals(previous, game.currentRound.time)
                    previous = game.currentRound.time
                }
            }
        }
    }

    @Test(expected = IllegalStateException::class)
    fun answerAfterVictoryIsRejected() {
        val game = ClockGame(1, Random(3))
        while (!game.isFinished) {
            game.answer(game.currentRound.time)
        }
        game.answer(ClockTime(12, 0))
    }
}
