package com.example.smartalarm.feature.games.dice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты чистой логики игры «Кубики»: параметры сложности, генерация броска,
 * проверка ответа (верный/неверный/пустой), раунды, победа и счёт.
 */
class DiceGameTest {

    /** Отвечает верно на текущий бросок. */
    private fun answerCorrectly(game: DiceGame): AnswerResult =
        game.answer(game.currentRound.sum.toString())

    /** Отвечает заведомо неверно на текущий бросок. */
    private fun answerWrongly(game: DiceGame): AnswerResult =
        game.answer((game.currentRound.sum + 1).toString())

    /** Проходит игру до победы правильными ответами. */
    private fun playToWin(game: DiceGame) {
        repeat(game.totalRounds - 1) {
            assertEquals(AnswerResult.NEXT_ROUND, answerCorrectly(game))
        }
        assertEquals(AnswerResult.WIN, answerCorrectly(game))
    }

    // ---------- Параметры сложности ----------

    @Test
    fun difficultyOneUsesThreeDiceAndThreeRounds() {
        val game = DiceGame(1, Random(1))
        assertEquals(3, game.diceCount)
        assertEquals(3, game.totalRounds)
        assertFalse(game.doubleEven)
    }

    @Test
    fun difficultyTwoUsesFiveDiceAndFourRounds() {
        val game = DiceGame(2, Random(1))
        assertEquals(5, game.diceCount)
        assertEquals(4, game.totalRounds)
        assertFalse(game.doubleEven)
    }

    @Test
    fun difficultyThreeUsesSevenDiceFiveRoundsAndDoubleEvenRule() {
        val game = DiceGame(3, Random(1))
        assertEquals(7, game.diceCount)
        assertEquals(5, game.totalRounds)
        assertTrue(game.doubleEven)
    }

    @Test
    fun outOfRangeDifficultyIsCoerced() {
        assertEquals(1, DiceGame(0, Random(1)).difficulty)
        assertEquals(1, DiceGame(-5, Random(1)).difficulty)
        assertEquals(3, DiceGame(7, Random(1)).difficulty)
    }

    // ---------- Генерация броска ----------

    @Test
    fun roundHasDiceCountValuesWithinRange() {
        for (difficulty in 1..3) {
            for (seed in 0L..49L) {
                val game = DiceGame(difficulty, Random(seed))
                assertEquals(game.diceCount, game.currentRound.values.size)
                for (value in game.currentRound.values)
                    assertTrue(
                        "Значение кости должно быть в 1..6, а не $value " +
                                "(difficulty=$difficulty, seed=$seed)",
                        value in DICE_MIN_VALUE..DICE_MAX_VALUE
                    )
            }
        }
    }

    @Test
    fun declaredSumMatchesValuesWithoutRuleOnLowDifficulties() {
        for (difficulty in 1..2) {
            for (seed in 0L..49L) {
                val round = DiceGame(difficulty, Random(seed)).currentRound
                assertEquals(
                    "Сумма броска должна совпадать с суммой значений " +
                            "(difficulty=$difficulty, seed=$seed)",
                    round.values.sum(), round.sum
                )
            }
        }
    }

    @Test
    fun declaredSumUsesDoubleEvenRuleOnDifficultyThree() {
        for (seed in 0L..49L) {
            val round = DiceGame(3, Random(seed)).currentRound
            assertEquals(
                "На сложности 3 чётные значения удваиваются (seed=$seed)",
                round.values.sumOf { if (it % 2 == 0) it * 2 else it },
                round.sum
            )
        }
    }

    // ---------- Детерминизм ----------

    @Test
    fun sameSeedProducesSameGame() {
        for (difficulty in 1..3) {
            val first = DiceGame(difficulty, Random(42))
            val second = DiceGame(difficulty, Random(42))
            repeat(first.totalRounds) {
                assertEquals(first.currentRound, second.currentRound)
                answerCorrectly(first)
                answerCorrectly(second)
            }
        }
    }

    // ---------- Проверка ответа ----------

    @Test
    fun correctAnswerAdvancesToNextRound() {
        val game = DiceGame(1, Random(7))
        assertEquals(1, game.roundNumber)
        assertEquals(AnswerResult.NEXT_ROUND, answerCorrectly(game))
        assertEquals(2, game.roundNumber)
        assertFalse(game.isFinished)
        assertEquals(0, game.mistakes)
    }

    @Test
    fun wrongAnswerCountsMistakeAndDoesNotAdvance() {
        val game = DiceGame(1, Random(7))
        assertEquals(AnswerResult.WRONG, answerWrongly(game))
        assertEquals(1, game.mistakes)
        assertEquals("Номер раунда не должен меняться после ошибки", 1, game.roundNumber)
        assertFalse(game.isFinished)
    }

    @Test
    fun wrongAnswerRerollsDiceOfTheSameRound() {
        for (seed in 0L..29L) {
            val game = DiceGame(1, Random(seed))
            val valuesBefore = game.currentRound.values
            answerWrongly(game)
            assertNotEquals(
                "После ошибки кости перебрасываются, чтобы подбор суммы " +
                        "не работал (seed=$seed)",
                valuesBefore, game.currentRound.values
            )
            assertEquals(1, game.roundNumber)
        }
    }

    @Test
    fun emptyAnswerIsNotAMistakeAndKeepsTheRoll() {
        val game = DiceGame(2, Random(9))
        val roundBefore = game.currentRound

        assertEquals(AnswerResult.EMPTY, game.answer(""))
        assertEquals(AnswerResult.EMPTY, game.answer("   "))
        assertEquals(AnswerResult.EMPTY, game.answer("abc"))

        assertEquals(0, game.mistakes)
        assertEquals(1, game.roundNumber)
        assertEquals("Пустой ввод не должен менять бросок", roundBefore, game.currentRound)
    }

    @Test
    fun answerWithSurroundingSpacesIsAccepted() {
        val game = DiceGame(1, Random(21))
        assertEquals(
            AnswerResult.NEXT_ROUND,
            game.answer(" ${game.currentRound.sum} ")
        )
    }

    @Test
    fun mistakesAccumulateAcrossRounds() {
        val game = DiceGame(1, Random(17))
        answerWrongly(game)
        answerCorrectly(game)
        answerWrongly(game)
        assertEquals(2, game.mistakes)
        assertEquals(2, game.roundNumber)
    }

    // ---------- Победа ----------

    @Test
    fun winAfterAllRoundsCompleted() {
        for (difficulty in 1..3) {
            val game = DiceGame(difficulty, Random(11))
            playToWin(game)
            assertTrue(game.isFinished)
            assertEquals(game.totalRounds, game.roundNumber)
        }
    }

    @Test
    fun answersAfterWinAreIgnored() {
        val game = DiceGame(1, Random(13))
        playToWin(game)

        val roundBefore = game.currentRound
        assertEquals(AnswerResult.WIN, answerWrongly(game))
        assertEquals(0, game.mistakes)
        assertEquals(roundBefore, game.currentRound)
    }

    // ---------- Счёт ----------

    @Test
    fun scoreWithoutMistakesMatchesCalcFormula() {
        assertEquals(600, computeDiceScore(0, 0, 1))
        assertEquals((600 - 100) * 3, computeDiceScore(0, 100, 3))
    }

    @Test
    fun eachMistakeCostsTenPoints() {
        assertEquals(
            computeDiceScore(0, 100, 2) - 30,
            computeDiceScore(3, 100, 2)
        )
    }

    @Test
    fun scoreCanBeNegativeForVeryLongGame() {
        assertTrue(computeDiceScore(0, 700, 1) < 0)
    }
}
