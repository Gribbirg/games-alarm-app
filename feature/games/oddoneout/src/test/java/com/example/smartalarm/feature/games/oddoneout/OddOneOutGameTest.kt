package com.example.smartalarm.feature.games.oddoneout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты чистой логики игры «Найди лишнее»:
 * пул пар, параметры сложности, генерация раундов, нажатия, победа и счёт.
 */
class OddOneOutGameTest {

    // ---------- Пул пар ----------

    @Test
    fun poolHasAtLeastTenPairs() {
        assertTrue("В пуле должно быть не меньше 10 пар", EMOJI_PAIRS.size >= 10)
    }

    @Test
    fun everySimilarityTierHasAtLeastTwoPairs() {
        for (tier in 1..3) {
            val count = EMOJI_PAIRS.count { it.similarity == tier }
            assertTrue(
                "Уровень похожести $tier должен содержать минимум 2 пары " +
                        "(иначе нельзя не повторять пару два раунда подряд), а содержит $count",
                count >= 2
            )
        }
    }

    @Test
    fun everyPairHasTwoDistinctSymbols() {
        for (pair in EMOJI_PAIRS)
            assertNotEquals("Символы пары должны отличаться: $pair", pair.first, pair.second)
    }

    @Test
    fun allPairsAreUnique() {
        assertEquals(EMOJI_PAIRS.size, EMOJI_PAIRS.distinct().size)
    }

    // ---------- Параметры сложности ----------

    @Test
    fun difficultyOneUsesThreeByThreeGridAndThreeRounds() {
        val game = OddOneOutGame(1, Random(1))
        assertEquals(3, game.gridSide)
        assertEquals(3, game.totalRounds)
    }

    @Test
    fun difficultyTwoUsesFourByFourGridAndFourRounds() {
        val game = OddOneOutGame(2, Random(1))
        assertEquals(4, game.gridSide)
        assertEquals(4, game.totalRounds)
    }

    @Test
    fun difficultyThreeUsesFiveByFiveGridAndFiveRounds() {
        val game = OddOneOutGame(3, Random(1))
        assertEquals(5, game.gridSide)
        assertEquals(5, game.totalRounds)
    }

    @Test
    fun outOfRangeDifficultyIsCoerced() {
        assertEquals(1, OddOneOutGame(0, Random(1)).difficulty)
        assertEquals(3, OddOneOutGame(7, Random(1)).difficulty)
    }

    @Test
    fun roundsUseOnlyPairsOfMatchingSimilarity() {
        for (difficulty in 1..3) {
            val game = OddOneOutGame(difficulty, Random(5))
            repeat(game.totalRounds) {
                assertEquals(difficulty, game.currentRound.pair.similarity)
                game.onCellClicked(game.currentRound.oddIndex)
            }
        }
    }

    // ---------- Генерация раунда ----------

    @Test
    fun roundHasExactlyOneOddSymbolAtDeclaredIndex() {
        for (difficulty in 1..3) {
            for (seed in 0L..49L) {
                val game = OddOneOutGame(difficulty, Random(seed))
                val round = game.currentRound
                val symbols = round.symbols

                assertEquals(round.gridSide * round.gridSide, symbols.size)
                assertNotEquals(round.baseSymbol, round.oddSymbol)
                assertEquals(round.oddSymbol, symbols[round.oddIndex])
                assertEquals(
                    "Лишний символ должен встречаться ровно один раз",
                    1, symbols.count { it == round.oddSymbol }
                )
                symbols.forEachIndexed { index, symbol ->
                    if (index != round.oddIndex)
                        assertEquals(round.baseSymbol, symbol)
                }
            }
        }
    }

    @Test
    fun roundSymbolsComeFromItsPair() {
        val game = OddOneOutGame(2, Random(3))
        val round = game.currentRound
        val pairSymbols = setOf(round.pair.first, round.pair.second)
        assertEquals(pairSymbols, setOf(round.baseSymbol, round.oddSymbol))
    }

    @Test
    fun sameSeedProducesSameGame() {
        for (difficulty in 1..3) {
            val first = OddOneOutGame(difficulty, Random(42))
            val second = OddOneOutGame(difficulty, Random(42))
            repeat(first.totalRounds) {
                assertEquals(first.currentRound, second.currentRound)
                first.onCellClicked(first.currentRound.oddIndex)
                second.onCellClicked(second.currentRound.oddIndex)
            }
        }
    }

    @Test
    fun consecutiveRoundsUseDifferentPairs() {
        for (seed in 0L..49L) {
            val game = OddOneOutGame(3, Random(seed))
            var previousPair = game.currentRound.pair
            while (!game.isFinished) {
                game.onCellClicked(game.currentRound.oddIndex)
                if (!game.isFinished) {
                    assertNotEquals(
                        "Одна и та же пара не должна выпадать два раунда подряд (seed=$seed)",
                        previousPair, game.currentRound.pair
                    )
                    previousPair = game.currentRound.pair
                }
            }
        }
    }

    // ---------- Нажатия и победа ----------

    @Test
    fun correctClickAdvancesToNextRound() {
        val game = OddOneOutGame(1, Random(7))
        assertEquals(1, game.roundNumber)
        val result = game.onCellClicked(game.currentRound.oddIndex)
        assertEquals(ClickResult.NEXT_ROUND, result)
        assertEquals(2, game.roundNumber)
        assertFalse(game.isFinished)
        assertEquals(0, game.mistakes)
    }

    @Test
    fun wrongClickCountsMistakeAndDoesNotAdvance() {
        val game = OddOneOutGame(1, Random(7))
        val round = game.currentRound
        val wrongIndex = (round.oddIndex + 1) % round.cellCount

        val result = game.onCellClicked(wrongIndex)

        assertEquals(ClickResult.WRONG, result)
        assertEquals(1, game.mistakes)
        assertEquals(1, game.roundNumber)
        assertEquals("Раунд не должен смениться после ошибки", round, game.currentRound)
        assertFalse(game.isFinished)
    }

    @Test
    fun winAfterAllRoundsCompleted() {
        for (difficulty in 1..3) {
            val game = OddOneOutGame(difficulty, Random(11))
            repeat(game.totalRounds - 1) {
                assertEquals(ClickResult.NEXT_ROUND, game.onCellClicked(game.currentRound.oddIndex))
            }
            assertEquals(ClickResult.WIN, game.onCellClicked(game.currentRound.oddIndex))
            assertTrue(game.isFinished)
            assertEquals(game.totalRounds, game.roundNumber)
        }
    }

    @Test
    fun clicksAfterWinAreIgnored() {
        val game = OddOneOutGame(1, Random(13))
        repeat(game.totalRounds) { game.onCellClicked(game.currentRound.oddIndex) }
        assertTrue(game.isFinished)

        val roundBefore = game.currentRound
        assertEquals(ClickResult.WIN, game.onCellClicked(0))
        assertEquals(0, game.mistakes)
        assertEquals(roundBefore, game.currentRound)
    }

    @Test
    fun mistakesAccumulateAcrossRounds() {
        val game = OddOneOutGame(1, Random(17))
        val wrong1 = (game.currentRound.oddIndex + 1) % game.currentRound.cellCount
        game.onCellClicked(wrong1)
        game.onCellClicked(game.currentRound.oddIndex)
        val wrong2 = (game.currentRound.oddIndex + 1) % game.currentRound.cellCount
        game.onCellClicked(wrong2)
        assertEquals(2, game.mistakes)
    }

    // ---------- Счёт ----------

    @Test
    fun scoreWithoutMistakesMatchesCalcFormula() {
        assertEquals(600, computeOddOneOutScore(0, 0, 1))
        assertEquals((600 - 100) * 3, computeOddOneOutScore(0, 100, 3))
    }

    @Test
    fun eachMistakeCostsTenPoints() {
        assertEquals(
            computeOddOneOutScore(0, 100, 2) - 30,
            computeOddOneOutScore(3, 100, 2)
        )
    }

    @Test
    fun scoreCanBeNegativeForVeryLongGame() {
        assertTrue(computeOddOneOutScore(0, 700, 1) < 0)
    }
}
