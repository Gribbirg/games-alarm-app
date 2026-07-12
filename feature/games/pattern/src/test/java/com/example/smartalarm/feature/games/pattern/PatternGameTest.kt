package com.example.smartalarm.feature.games.pattern

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты чистой логики игры «Закономерность»: пул эмодзи, шаблоны по
 * сложностям, генерация раундов, варианты ответа, ошибки, победа и счёт.
 */
class PatternGameTest {

    // ---------- Пул эмодзи ----------

    @Test
    fun poolHasAtLeastTwelveDistinctEmojis() {
        assertTrue("В пуле должно быть не меньше 12 эмодзи", EMOJI_POOL.size >= 12)
        assertEquals("Эмодзи пула должны быть уникальны",
            EMOJI_POOL.size, EMOJI_POOL.distinct().size)
    }

    // ---------- Шаблоны по сложностям ----------

    @Test
    fun difficultyOneTemplatesHavePeriodTwo() {
        assertTrue(DIFFICULTY_1_TEMPLATES.isNotEmpty())
        for (template in templatesFor(1))
            assertEquals("Сложность 1 — период 2: $template", 2, template.period)
    }

    @Test
    fun difficultyTwoTemplatesHavePeriodThree() {
        assertTrue(DIFFICULTY_2_TEMPLATES.size >= 2)
        for (template in templatesFor(2))
            assertEquals("Сложность 2 — период 3: $template", 3, template.period)
    }

    @Test
    fun difficultyThreeTemplatesHavePeriodFour() {
        assertTrue(DIFFICULTY_3_TEMPLATES.size >= 2)
        for (template in templatesFor(3))
            assertEquals("Сложность 3 — период 4: $template", 4, template.period)
    }

    @Test
    fun templateIndicesFormContiguousRangeFromZero() {
        for (difficulty in 1..3) {
            for (template in templatesFor(difficulty)) {
                val distinct = template.periodIndices.distinct().sorted()
                assertEquals(
                    "Индексы шаблона ${template.name} должны образовывать 0..N без пропусков",
                    (0 until template.symbolCount).toList(), distinct
                )
            }
        }
    }

    @Test
    fun templatesWithinDifficultyAreUnique() {
        for (difficulty in 1..3) {
            val templates = templatesFor(difficulty)
            assertEquals(templates.size, templates.distinct().size)
        }
    }

    @Test
    fun outOfRangeDifficultyIsCoercedForTemplates() {
        assertEquals(templatesFor(1), templatesFor(0))
        assertEquals(templatesFor(3), templatesFor(7))
    }

    // ---------- Параметры игры ----------

    @Test
    fun totalRoundsAreThreeFourFiveByDifficulty() {
        assertEquals(3, PatternGame(1, Random(1)).totalRounds)
        assertEquals(4, PatternGame(2, Random(1)).totalRounds)
        assertEquals(5, PatternGame(3, Random(1)).totalRounds)
    }

    @Test
    fun outOfRangeDifficultyIsCoercedForGame() {
        assertEquals(1, PatternGame(-2, Random(1)).difficulty)
        assertEquals(3, PatternGame(9, Random(1)).difficulty)
    }

    @Test
    fun roundsUseOnlyTemplatesOfGameDifficulty() {
        for (difficulty in 1..3) {
            val templates = templatesFor(difficulty)
            val game = PatternGame(difficulty, Random(5))
            while (!game.isFinished) {
                assertTrue(game.currentRound.template in templates)
                game.onOptionClicked(correctIndex(game))
            }
        }
    }

    // ---------- Генерация раунда ----------

    @Test
    fun visibleSequenceFollowsTemplateAndAnswerIsNextElement() {
        for (difficulty in 1..3) {
            for (seed in 0L..49L) {
                val game = PatternGame(difficulty, Random(seed))
                while (true) {
                    val round = game.currentRound
                    val period = round.template.period
                    val sequence = round.visibleSequence

                    assertEquals(round.visibleCount, sequence.size)
                    // Пересчитываем ряд напрямую по периоду шаблона.
                    sequence.forEachIndexed { position, symbol ->
                        assertEquals(
                            "Позиция $position ряда должна следовать шаблону " +
                                    "${round.template.name} (seed=$seed)",
                            round.symbols[round.template.periodIndices[position % period]],
                            symbol
                        )
                    }
                    assertEquals(
                        "Ответ — элемент, следующий сразу за показанными",
                        round.symbols[round.template.periodIndices[round.visibleCount % period]],
                        round.answer
                    )

                    if (game.onOptionClicked(correctIndex(game)) == AnswerResult.WIN) break
                }
            }
        }
    }

    @Test
    fun visibleCountIsAtLeastTwoPeriodsPlusAtMostTwo() {
        for (difficulty in 1..3) {
            for (seed in 0L..49L) {
                val round = PatternGame(difficulty, Random(seed)).currentRound
                val period = round.template.period
                assertTrue(
                    "Показ должен содержать 2 полных периода + 0..2 элемента, " +
                            "а содержит ${round.visibleCount} при периоде $period",
                    round.visibleCount in 2 * period..2 * period + 2
                )
            }
        }
    }

    @Test
    fun patternSymbolsAreDistinctAndTakenFromPool() {
        for (difficulty in 1..3) {
            for (seed in 0L..49L) {
                val round = PatternGame(difficulty, Random(seed)).currentRound
                assertEquals(round.template.symbolCount, round.symbols.size)
                assertEquals(
                    "Все символы паттерна должны быть различны (seed=$seed)",
                    round.symbols.size, round.symbols.distinct().size
                )
                for (symbol in round.symbols)
                    assertTrue(symbol in EMOJI_POOL)
            }
        }
    }

    // ---------- Варианты ответа ----------

    @Test
    fun optionsAreFourUniqueContainCorrectAnswerAndAnOutsider() {
        for (difficulty in 1..3) {
            for (seed in 0L..49L) {
                val round = PatternGame(difficulty, Random(seed)).currentRound
                val options = round.options

                assertEquals(PatternGame.OPTIONS_COUNT, options.size)
                assertEquals("Варианты должны быть уникальны (seed=$seed)",
                    options.size, options.distinct().size)
                assertTrue("Среди вариантов должен быть верный ответ",
                    round.answer in options)
                assertTrue("Нужен хотя бы один посторонний эмодзи (не из паттерна)",
                    options.any { it !in round.symbols })
                for (option in options)
                    assertTrue("Вариант должен быть из пула: $option", option in EMOJI_POOL)
            }
        }
    }

    // ---------- Ошибки, раунды и победа ----------

    @Test
    fun correctAnswerAdvancesToNextRound() {
        val game = PatternGame(1, Random(7))
        assertEquals(1, game.roundNumber)
        val result = game.onOptionClicked(correctIndex(game))
        assertEquals(AnswerResult.NEXT_ROUND, result)
        assertEquals(2, game.roundNumber)
        assertFalse(game.isFinished)
        assertEquals(0, game.mistakes)
    }

    @Test
    fun wrongAnswerCountsMistakeAndRegeneratesRoundWithoutAdvancing() {
        val game = PatternGame(2, Random(7))
        val wrongIndex = (correctIndex(game) + 1) % PatternGame.OPTIONS_COUNT

        val result = game.onOptionClicked(wrongIndex)

        assertEquals(AnswerResult.WRONG, result)
        assertEquals(1, game.mistakes)
        assertEquals("Номер раунда не должен вырасти после ошибки", 1, game.roundNumber)
        assertFalse(game.isFinished)
    }

    @Test
    fun bruteForceDoesNotWin() {
        // Кликаем только неверные варианты много раз подряд:
        // игра не должна ни продвинуться, ни завершиться.
        val game = PatternGame(3, Random(21))
        repeat(50) {
            val wrongIndex = (correctIndex(game) + 1) % PatternGame.OPTIONS_COUNT
            assertEquals(AnswerResult.WRONG, game.onOptionClicked(wrongIndex))
        }
        assertEquals(1, game.roundNumber)
        assertEquals(50, game.mistakes)
        assertFalse(game.isFinished)
    }

    @Test
    fun winAfterAllRoundsCompleted() {
        for (difficulty in 1..3) {
            val game = PatternGame(difficulty, Random(11))
            repeat(game.totalRounds - 1) {
                assertEquals(AnswerResult.NEXT_ROUND, game.onOptionClicked(correctIndex(game)))
            }
            assertEquals(AnswerResult.WIN, game.onOptionClicked(correctIndex(game)))
            assertTrue(game.isFinished)
            assertEquals(game.totalRounds, game.roundNumber)
        }
    }

    @Test
    fun clicksAfterWinAreIgnored() {
        val game = PatternGame(1, Random(13))
        repeat(game.totalRounds) { game.onOptionClicked(correctIndex(game)) }
        assertTrue(game.isFinished)

        val roundBefore = game.currentRound
        assertEquals(AnswerResult.WIN, game.onOptionClicked(0))
        assertEquals(0, game.mistakes)
        assertEquals(roundBefore, game.currentRound)
    }

    @Test
    fun mistakesAccumulateAcrossRounds() {
        val game = PatternGame(1, Random(17))
        game.onOptionClicked((correctIndex(game) + 1) % PatternGame.OPTIONS_COUNT)
        game.onOptionClicked(correctIndex(game))
        game.onOptionClicked((correctIndex(game) + 1) % PatternGame.OPTIONS_COUNT)
        assertEquals(2, game.mistakes)
    }

    @Test
    fun consecutiveRoundsUseDifferentTemplatesWhenPossible() {
        for (difficulty in 2..3) {
            for (seed in 0L..49L) {
                val game = PatternGame(difficulty, Random(seed))
                var previous = game.currentRound.template
                while (!game.isFinished) {
                    game.onOptionClicked(correctIndex(game))
                    if (!game.isFinished) {
                        assertNotEquals(
                            "Шаблон не должен повторяться два раунда подряд (seed=$seed)",
                            previous, game.currentRound.template
                        )
                        previous = game.currentRound.template
                    }
                }
            }
        }
    }

    // ---------- Детерминизм ----------

    @Test
    fun sameSeedProducesSameGame() {
        for (difficulty in 1..3) {
            val first = PatternGame(difficulty, Random(42))
            val second = PatternGame(difficulty, Random(42))
            repeat(first.totalRounds) {
                assertEquals(first.currentRound, second.currentRound)
                first.onOptionClicked(correctIndex(first))
                second.onOptionClicked(correctIndex(second))
            }
        }
    }

    // ---------- Счёт ----------

    @Test
    fun scoreWithoutMistakesMatchesCalcFormula() {
        assertEquals(600, computePatternScore(0, 0, 1))
        assertEquals((600 - 100) * 3, computePatternScore(0, 100, 3))
    }

    @Test
    fun eachMistakeCostsTenPoints() {
        assertEquals(
            computePatternScore(0, 100, 2) - 30,
            computePatternScore(3, 100, 2)
        )
    }

    @Test
    fun scoreCanBeNegativeForVeryLongGame() {
        assertTrue(computePatternScore(0, 700, 1) < 0)
    }

    /** Индекс правильного ответа среди вариантов текущего раунда. */
    private fun correctIndex(game: PatternGame): Int =
        game.currentRound.options.indexOf(game.currentRound.answer)
}
