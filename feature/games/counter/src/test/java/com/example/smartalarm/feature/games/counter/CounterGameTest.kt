package com.example.smartalarm.feature.games.counter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты чистой логики игры «Сосчитай»: пул групп эмодзи, параметры сложности,
 * генерация поля и вариантов ответа, ответы, раунды, победа и счёт.
 */
class CounterGameTest {

    /** Номера групп пула, которым принадлежат виды [kinds]. */
    private fun groupsOf(kinds: Collection<String>): Set<Int> =
        kinds.map { emoji ->
            COUNTER_EMOJI_GROUPS.indexOfFirst { emoji in it.emojis }
        }.toSet()

    /** Любой вариант ответа текущего раунда, кроме правильного. */
    private fun wrongOption(game: CounterGame): Int =
        game.currentRound.options.first { it != game.currentRound.correctCount }

    /** Проходит игру до победы правильными ответами. */
    private fun playToWin(game: CounterGame) {
        repeat(game.totalRounds - 1) {
            assertEquals(AnswerResult.NEXT_ROUND, game.answer(game.currentRound.correctCount))
        }
        assertEquals(AnswerResult.WIN, game.answer(game.currentRound.correctCount))
    }

    // ---------- Пул групп ----------

    @Test
    fun poolHasAtLeastThreeGroups() {
        assertTrue(
            "Нужно минимум 3 группы (на сложности 1 виды берутся из трёх разных групп)",
            COUNTER_EMOJI_GROUPS.size >= 3
        )
    }

    @Test
    fun everyGroupHasEnoughDistinctEmojis() {
        for (group in COUNTER_EMOJI_GROUPS) {
            assertTrue(
                "Группа «${group.name}» должна содержать минимум " +
                        "$KINDS_ON_MAX_DIFFICULTY эмодзи (столько видов на сложности 3)",
                group.emojis.size >= KINDS_ON_MAX_DIFFICULTY
            )
            assertEquals(
                "Эмодзи внутри группы «${group.name}» должны быть уникальны",
                group.emojis.size, group.emojis.distinct().size
            )
        }
    }

    @Test
    fun allEmojisAreGloballyUnique() {
        val all = COUNTER_EMOJI_GROUPS.flatMap { it.emojis }
        assertEquals(
            "Эмодзи не должны повторяться между группами",
            all.size, all.distinct().size
        )
    }

    // ---------- Параметры сложности ----------

    @Test
    fun difficultyOneUsesFourByFourThreeRoundsThreeKinds() {
        val game = CounterGame(1, Random(1))
        assertEquals(4, game.gridSide)
        assertEquals(3, game.totalRounds)
        assertEquals(3, game.kindCount)
    }

    @Test
    fun difficultyTwoUsesFiveByFiveFourRoundsFourKinds() {
        val game = CounterGame(2, Random(1))
        assertEquals(5, game.gridSide)
        assertEquals(4, game.totalRounds)
        assertEquals(4, game.kindCount)
    }

    @Test
    fun difficultyThreeUsesSixBySixFiveRoundsFiveKinds() {
        val game = CounterGame(3, Random(1))
        assertEquals(6, game.gridSide)
        assertEquals(5, game.totalRounds)
        assertEquals(5, game.kindCount)
    }

    @Test
    fun outOfRangeDifficultyIsCoerced() {
        assertEquals(1, CounterGame(0, Random(1)).difficulty)
        assertEquals(1, CounterGame(-5, Random(1)).difficulty)
        assertEquals(3, CounterGame(7, Random(1)).difficulty)
    }

    // ---------- Генерация поля ----------

    @Test
    fun declaredAnswerMatchesActualTargetCountOnField() {
        for (difficulty in 1..3) {
            for (seed in 0L..49L) {
                val round = CounterGame(difficulty, Random(seed)).currentRound
                assertEquals(
                    "Правильный ответ должен совпадать с фактическим числом " +
                            "целевых эмодзи на поле (difficulty=$difficulty, seed=$seed)",
                    round.cells.count { it == round.targetEmoji },
                    round.correctCount
                )
            }
        }
    }

    @Test
    fun fieldHasExactlyGridSideSquaredCells() {
        for (difficulty in 1..3) {
            val round = CounterGame(difficulty, Random(2)).currentRound
            assertEquals(round.gridSide * round.gridSide, round.cells.size)
            assertEquals(round.cellCount, round.cells.size)
        }
    }

    @Test
    fun targetCountIsAtLeastMinimumAndLessThanHalfTheField() {
        for (difficulty in 1..3) {
            for (seed in 0L..49L) {
                val round = CounterGame(difficulty, Random(seed)).currentRound
                assertTrue(
                    "Целевых должно быть не меньше $MIN_TARGET_COUNT",
                    round.correctCount >= MIN_TARGET_COUNT
                )
                assertTrue(
                    "Целевых должно быть строго меньше половины поля " +
                            "(${round.correctCount} из ${round.cellCount})",
                    round.correctCount * 2 < round.cellCount
                )
            }
        }
    }

    @Test
    fun fieldContainsExpectedNumberOfKindsAndEveryDistractorAppears() {
        for (difficulty in 1..3) {
            for (seed in 0L..49L) {
                val game = CounterGame(difficulty, Random(seed))
                val distinctKinds = game.currentRound.cells.distinct()
                assertEquals(
                    "На поле должно быть ровно ${game.kindCount} видов эмодзи " +
                            "(каждый дистрактор хотя бы один раз)",
                    game.kindCount, distinctKinds.size
                )
                assertTrue(game.currentRound.targetEmoji in distinctKinds)
            }
        }
    }

    @Test
    fun allFieldEmojisComeFromThePool() {
        val poolEmojis = COUNTER_EMOJI_GROUPS.flatMap { it.emojis }.toSet()
        for (difficulty in 1..3) {
            val round = CounterGame(difficulty, Random(3)).currentRound
            for (cell in round.cells)
                assertTrue("Незнакомый эмодзи на поле: $cell", cell in poolEmojis)
        }
    }

    @Test
    fun difficultyOneKindsComeFromThreeDifferentGroups() {
        for (seed in 0L..29L) {
            val kinds = CounterGame(1, Random(seed)).currentRound.cells.distinct()
            assertEquals(
                "На сложности 1 все виды из разных групп (seed=$seed)",
                3, groupsOf(kinds).size
            )
        }
    }

    @Test
    fun difficultyTwoKindsComeFromExactlyTwoGroups() {
        for (seed in 0L..29L) {
            val kinds = CounterGame(2, Random(seed)).currentRound.cells.distinct()
            assertEquals(
                "На сложности 2 четыре вида из двух групп (seed=$seed)",
                2, groupsOf(kinds).size
            )
        }
    }

    @Test
    fun difficultyThreeKindsComeFromSingleGroup() {
        for (seed in 0L..29L) {
            val kinds = CounterGame(3, Random(seed)).currentRound.cells.distinct()
            assertEquals(
                "На сложности 3 все виды похожи — из одной группы (seed=$seed)",
                1, groupsOf(kinds).size
            )
        }
    }

    // ---------- Варианты ответа ----------

    @Test
    fun optionsAreFourUniqueNonNegativeAndContainCorrect() {
        for (difficulty in 1..3) {
            for (seed in 0L..49L) {
                val round = CounterGame(difficulty, Random(seed)).currentRound
                assertEquals(OPTIONS_COUNT, round.options.size)
                assertEquals(
                    "Варианты должны быть уникальны (seed=$seed): ${round.options}",
                    OPTIONS_COUNT, round.options.distinct().size
                )
                assertTrue(
                    "Правильный ответ должен быть среди вариантов",
                    round.correctCount in round.options
                )
                for (option in round.options)
                    assertTrue("Вариант не может быть отрицательным: $option", option >= 0)
            }
        }
    }

    // ---------- Детерминизм ----------

    @Test
    fun sameSeedProducesSameGame() {
        for (difficulty in 1..3) {
            val first = CounterGame(difficulty, Random(42))
            val second = CounterGame(difficulty, Random(42))
            repeat(first.totalRounds) {
                assertEquals(first.currentRound, second.currentRound)
                first.answer(first.currentRound.correctCount)
                second.answer(second.currentRound.correctCount)
            }
        }
    }

    // ---------- Раунды, ответы и победа ----------

    @Test
    fun correctAnswerAdvancesToNextRound() {
        val game = CounterGame(1, Random(7))
        assertEquals(1, game.roundNumber)
        val result = game.answer(game.currentRound.correctCount)
        assertEquals(AnswerResult.NEXT_ROUND, result)
        assertEquals(2, game.roundNumber)
        assertFalse(game.isFinished)
        assertEquals(0, game.mistakes)
    }

    @Test
    fun wrongAnswerCountsMistakeAndDoesNotAdvance() {
        val game = CounterGame(1, Random(7))
        val result = game.answer(wrongOption(game))
        assertEquals(AnswerResult.WRONG, result)
        assertEquals(1, game.mistakes)
        assertEquals("Номер раунда не должен меняться после ошибки", 1, game.roundNumber)
        assertFalse(game.isFinished)
    }

    @Test
    fun wrongAnswerRegeneratesFieldWithDifferentTarget() {
        for (seed in 0L..29L) {
            val game = CounterGame(2, Random(seed))
            val targetBefore = game.currentRound.targetEmoji
            game.answer(wrongOption(game))
            assertNotEquals(
                "После ошибки поле перегенерируется с другим целевым эмодзи, " +
                        "чтобы перебор вариантов не работал (seed=$seed)",
                targetBefore, game.currentRound.targetEmoji
            )
        }
    }

    @Test
    fun targetDoesNotRepeatInConsecutiveRounds() {
        for (seed in 0L..29L) {
            val game = CounterGame(3, Random(seed))
            var previousTarget = game.currentRound.targetEmoji
            while (!game.isFinished) {
                game.answer(game.currentRound.correctCount)
                if (!game.isFinished) {
                    assertNotEquals(
                        "Целевой эмодзи не должен повторяться два раунда подряд (seed=$seed)",
                        previousTarget, game.currentRound.targetEmoji
                    )
                    previousTarget = game.currentRound.targetEmoji
                }
            }
        }
    }

    @Test
    fun winAfterAllRoundsCompleted() {
        for (difficulty in 1..3) {
            val game = CounterGame(difficulty, Random(11))
            playToWin(game)
            assertTrue(game.isFinished)
            assertEquals(game.totalRounds, game.roundNumber)
        }
    }

    @Test
    fun answersAfterWinAreIgnored() {
        val game = CounterGame(1, Random(13))
        playToWin(game)

        val roundBefore = game.currentRound
        assertEquals(AnswerResult.WIN, game.answer(wrongOption(game)))
        assertEquals(0, game.mistakes)
        assertEquals(roundBefore, game.currentRound)
    }

    @Test
    fun mistakesAccumulateAcrossRounds() {
        val game = CounterGame(1, Random(17))
        game.answer(wrongOption(game))
        game.answer(game.currentRound.correctCount)
        game.answer(wrongOption(game))
        assertEquals(2, game.mistakes)
        assertEquals(2, game.roundNumber)
    }

    // ---------- Счёт ----------

    @Test
    fun scoreWithoutMistakesMatchesCalcFormula() {
        assertEquals(600, computeCounterScore(0, 0, 1))
        assertEquals((600 - 100) * 3, computeCounterScore(0, 100, 3))
    }

    @Test
    fun eachMistakeCostsTenPoints() {
        assertEquals(
            computeCounterScore(0, 100, 2) - 30,
            computeCounterScore(3, 100, 2)
        )
    }

    @Test
    fun scoreCanBeNegativeForVeryLongGame() {
        assertTrue(computeCounterScore(0, 700, 1) < 0)
    }
}
