package com.example.smartalarm.feature.games.reverse.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

/** Тесты чистой логики игры «Наоборот»: вспомогательные функции и [ReverseGame]. */
class ReverseGameTest {

    // ---------- вспомогательные функции ----------

    @Test
    fun commonLetterCountRespectsRepeatedLetters() {
        // Общие буквы «сорока» и «корова»: к, о, о, р, а — вторая «о» тоже считается.
        assertEquals(5, commonLetterCount("сорока", "корова"))
        assertEquals(0, commonLetterCount("дым", "гора"))
        assertEquals(4, commonLetterCount("коса", "коса"))
    }

    @Test
    fun commonPrefixLengthCountsMatchingLeadingLetters() {
        assertEquals(4, commonPrefixLength("корова", "корона"))
        assertEquals(1, commonPrefixLength("полка", "палка"))
        assertEquals(0, commonPrefixLength("гора", "нора"))
    }

    // ---------- параметры игры ----------

    @Test
    fun roundCountForDifficultyIs3_4_5() {
        assertEquals(3, ReverseGame.roundCountForDifficulty(1))
        assertEquals(4, ReverseGame.roundCountForDifficulty(2))
        assertEquals(5, ReverseGame.roundCountForDifficulty(3))
        // Значения вне диапазона приводятся к ближайшему уровню.
        assertEquals(3, ReverseGame.roundCountForDifficulty(0))
        assertEquals(5, ReverseGame.roundCountForDifficulty(7))
    }

    @Test(expected = IllegalArgumentException::class)
    fun tooSmallDictionaryIsRejected() {
        ReverseGame(1, Random(1), listOf("вода", "гора", "нора"))
    }

    // ---------- раунд ----------

    @Test
    fun displayedWordIsAnswerReversed() {
        for (seed in 0..20)
            for (difficulty in 1..3) {
                val round = ReverseGame(difficulty, Random(seed)).currentRound
                assertEquals(round.answer.reversed(), round.displayed)
            }
    }

    @Test
    fun optionsAreFourDistinctWordsIncludingAnswer() {
        for (seed in 0..20)
            for (difficulty in 1..3) {
                val round = ReverseGame(difficulty, Random(seed)).currentRound
                assertEquals(ReverseGame.OPTIONS_COUNT, round.options.size)
                assertEquals(round.options.size, round.options.distinct().size)
                assertTrue(round.answer in round.options)
            }
    }

    @Test
    fun noOptionEqualsDisplayedWord() {
        // Ни один вариант не совпадает со словом на экране — иначе ответ
        // был бы неоднозначным (например, «кот» ↔ «ток»).
        for (seed in 0..20)
            for (difficulty in 1..3) {
                val game = ReverseGame(difficulty, Random(seed))
                while (!game.isGameWon) {
                    for (option in game.currentRound.options)
                        assertNotEquals(game.currentRound.displayed, option)
                    game.submitAnswer(game.correctIndex())
                }
            }
    }

    @Test
    fun distractorsLengthIsWithinOneOfAnswerLength() {
        for (seed in 0..20)
            for (difficulty in 1..3) {
                val round = ReverseGame(difficulty, Random(seed)).currentRound
                for (option in round.options)
                    assertTrue(
                        "«$option» слишком отличается по длине от «${round.answer}»",
                        abs(option.length - round.answer.length) <= 1
                    )
            }
    }

    @Test
    fun gameIsDeterministicWithSeed() {
        val game1 = ReverseGame(2, Random(42))
        val game2 = ReverseGame(2, Random(42))
        while (!game1.isGameWon) {
            assertEquals(game1.currentRound.answer, game2.currentRound.answer)
            assertEquals(game1.currentRound.options, game2.currentRound.options)
            assertEquals(game1.submitAnswer(game1.correctIndex()), game2.submitAnswer(game2.correctIndex()))
        }
        assertTrue(game2.isGameWon)
    }

    // ---------- прогресс и победа ----------

    @Test
    fun correctAnswerAdvancesProgressAndChangesWord() {
        val game = ReverseGame(2, Random(5))
        val firstWord = game.currentRound.answer
        assertEquals(AnswerResult.CORRECT, game.submitAnswer(game.correctIndex()))
        assertEquals(1, game.correctCount)
        assertNotEquals(firstWord, game.currentRound.answer)
    }

    @Test
    fun wrongAnswerKeepsProgressAndChangesWord() {
        val game = ReverseGame(2, Random(5))
        val firstWord = game.currentRound.answer
        assertEquals(AnswerResult.WRONG, game.submitAnswer(game.wrongIndex()))
        assertEquals(0, game.correctCount)
        assertNotEquals(firstWord, game.currentRound.answer)
        assertFalse(game.isGameWon)
    }

    @Test
    fun winAfterTotalRoundsCorrectAnswers() {
        for (difficulty in 1..3) {
            val game = ReverseGame(difficulty, Random(difficulty))
            val results = mutableListOf<AnswerResult>()
            repeat(game.totalRounds) {
                results += game.submitAnswer(game.correctIndex())
            }
            assertEquals(
                List(game.totalRounds - 1) { AnswerResult.CORRECT } + AnswerResult.GAME_WON,
                results
            )
            assertTrue(game.isGameWon)
            assertEquals(game.totalRounds, game.correctCount)
        }
    }

    @Test
    fun mistakesDoNotPreventEventualWin() {
        val game = ReverseGame(1, Random(11))
        assertEquals(AnswerResult.WRONG, game.submitAnswer(game.wrongIndex()))
        assertEquals(AnswerResult.WRONG, game.submitAnswer(game.wrongIndex()))
        repeat(game.totalRounds - 1) {
            assertEquals(AnswerResult.CORRECT, game.submitAnswer(game.correctIndex()))
        }
        assertEquals(AnswerResult.GAME_WON, game.submitAnswer(game.correctIndex()))
        assertTrue(game.isGameWon)
    }

    @Test
    fun afterVictorySubmitKeepsReportingWinWithoutChanges() {
        val game = ReverseGame(1, Random(3))
        repeat(game.totalRounds) { game.submitAnswer(game.correctIndex()) }
        val lastRound = game.currentRound
        assertEquals(AnswerResult.GAME_WON, game.submitAnswer(0))
        assertEquals(game.totalRounds, game.correctCount)
        assertSame(lastRound, game.currentRound)
    }

    @Test(expected = IllegalArgumentException::class)
    fun submitAnswerRejectsIndexOutOfRange() {
        ReverseGame(1, Random(1)).submitAnswer(ReverseGame.OPTIONS_COUNT)
    }

    // ---------- неповторяемость слов ----------

    @Test
    fun answersDoNotRepeatWithinOneGameIncludingMistakes() {
        for (seed in 0..10) {
            val game = ReverseGame(3, Random(seed))
            val seen = mutableListOf(game.currentRound.answer)
            // Чередуем ошибки и верные ответы — новые слова не должны повторяться.
            var mistakes = 0
            while (!game.isGameWon) {
                val result = if (mistakes < 4) {
                    mistakes++
                    game.submitAnswer(game.wrongIndex())
                } else {
                    game.submitAnswer(game.correctIndex())
                }
                if (result != AnswerResult.GAME_WON) seen += game.currentRound.answer
            }
            assertEquals(seen.size, seen.distinct().size)
        }
    }

    @Test
    fun exhaustedDictionaryResetsButNeverRepeatsWordTwiceInARow() {
        // Словарь минимального размера: ошибки быстро исчерпывают его.
        val dictionary = listOf("вода", "гора", "река", "мука")
        val game = ReverseGame(1, Random(13), dictionary)
        var previous = game.currentRound.answer
        repeat(30) {
            game.submitAnswer(game.wrongIndex())
            assertNotEquals(previous, game.currentRound.answer)
            previous = game.currentRound.answer
        }
    }

    /** Индекс правильного ответа в вариантах текущего раунда. */
    private fun ReverseGame.correctIndex(): Int =
        currentRound.options.indexOf(currentRound.answer)

    /** Индекс любого неверного варианта текущего раунда. */
    private fun ReverseGame.wrongIndex(): Int =
        currentRound.options.indices.first { currentRound.options[it] != currentRound.answer }
}
