package com.example.smartalarm.feature.games.chain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты игровой логики: последовательность показа, число цепочек для победы,
 * проверка ответа (верный/неверный/пустой), ошибки и детерминизм seed.
 */
class ChainGameTest {

    /** Прокликивает «Дальше» до фазы ввода итога. */
    private fun ChainGame.revealAll() {
        while (!isInputPhase) check(showNextStep()) { "Показ шагов остановился до фазы ввода" }
    }

    @Test
    fun totalChainsDependOnDifficulty() {
        assertEquals(1, ChainGame(1, Random(1)).totalChains)
        assertEquals(1, ChainGame(2, Random(1)).totalChains)
        assertEquals(2, ChainGame(3, Random(1)).totalChains)
    }

    @Test
    fun difficultyOutOfRangeIsCoerced() {
        assertEquals(1, ChainGame(0, Random(1)).difficulty)
        assertEquals(1, ChainGame(-5, Random(1)).difficulty)
        assertEquals(3, ChainGame(99, Random(1)).difficulty)
    }

    @Test
    fun displaySequenceShowsStartThenStepsThenPrompt() {
        val game = ChainGame(2, Random(3))
        val chain = game.currentChain

        assertEquals(chain.start.toString(), game.currentDisplay)
        assertEquals(0, game.shownSteps)
        assertFalse(game.isInputPhase)

        for ((index, step) in chain.steps.withIndex()) {
            assertTrue(game.showNextStep())
            assertEquals(step.text, game.currentDisplay)
            assertEquals(index + 1, game.shownSteps)
        }
        assertFalse("Последний шаг ещё показывается", game.isInputPhase)

        assertTrue(game.showNextStep())
        assertTrue(game.isInputPhase)
        assertEquals("= ?", game.currentDisplay)
        assertFalse("В фазе ввода шаги не листаются", game.showNextStep())
    }

    @Test
    fun correctAnswerWinsWithOneChainOnLowDifficulties() {
        for (difficulty in 1..2) {
            val game = ChainGame(difficulty, Random(difficulty))
            game.revealAll()
            assertEquals(
                ChainAnswerResult.WIN,
                game.submitAnswer(game.currentChain.result.toString())
            )
            assertTrue(game.isWon)
            assertEquals(1, game.solvedChains)
        }
    }

    @Test
    fun difficultyThreeNeedsTwoChains() {
        val game = ChainGame(3, Random(11))
        game.revealAll()
        assertEquals(
            ChainAnswerResult.CHAIN_SOLVED,
            game.submitAnswer(game.currentChain.result.toString())
        )
        assertFalse(game.isWon)
        assertEquals(1, game.solvedChains)
        assertEquals("Новая цепочка начинается с показа старта", 0, game.shownSteps)
        assertFalse(game.isInputPhase)

        game.revealAll()
        assertEquals(
            ChainAnswerResult.WIN,
            game.submitAnswer(game.currentChain.result.toString())
        )
        assertTrue(game.isWon)
        assertEquals(2, game.solvedChains)
    }

    @Test
    fun wrongAnswerStartsNewChainAndKeepsSolvedCount() {
        val game = ChainGame(1, Random(8))
        game.revealAll()
        val wrong = game.currentChain.result + 1
        assertEquals(ChainAnswerResult.WRONG, game.submitAnswer(wrong.toString()))
        assertEquals(0, game.solvedChains)
        assertEquals(0, game.shownSteps)
        assertFalse(game.isInputPhase)
        assertFalse(game.isWon)

        game.revealAll()
        assertEquals(
            "Новую цепочку по-прежнему можно решить",
            ChainAnswerResult.WIN,
            game.submitAnswer(game.currentChain.result.toString())
        )
    }

    @Test
    fun emptyOrInvalidInputDoesNotChangeState() {
        val game = ChainGame(1, Random(4))
        game.revealAll()
        val chain = game.currentChain
        for (input in listOf("", "   ", "abc", "12a"))
            assertEquals(
                "Ввод «$input» должен считаться отсутствием ответа",
                ChainAnswerResult.EMPTY,
                game.submitAnswer(input)
            )
        assertEquals("Цепочка не поменялась", chain, game.currentChain)
        assertTrue(game.isInputPhase)
        assertEquals(0, game.solvedChains)
    }

    @Test
    fun answerBeforeInputPhaseIsIgnored() {
        val game = ChainGame(1, Random(6))
        assertEquals(
            ChainAnswerResult.EMPTY,
            game.submitAnswer(game.currentChain.result.toString())
        )
        assertEquals(0, game.solvedChains)
        assertEquals(0, game.shownSteps)
    }

    @Test
    fun answerWithSurroundingSpacesIsAccepted() {
        val game = ChainGame(1, Random(9))
        game.revealAll()
        assertEquals(
            ChainAnswerResult.WIN,
            game.submitAnswer(" ${game.currentChain.result} ")
        )
    }

    @Test
    fun submitAfterWinAlwaysReturnsWinAndKeepsState() {
        val game = ChainGame(1, Random(2))
        game.revealAll()
        game.submitAnswer(game.currentChain.result.toString())
        assertTrue(game.isWon)

        assertEquals(ChainAnswerResult.WIN, game.submitAnswer("123"))
        assertEquals(1, game.solvedChains)
        assertFalse("После победы шаги не листаются", game.showNextStep())
    }

    @Test
    fun sameSeedGivesSameGame() {
        fun playedChains(seed: Long): List<Chain> {
            val game = ChainGame(3, Random(seed))
            val chains = mutableListOf(game.currentChain)
            while (!game.isWon) {
                game.revealAll()
                game.submitAnswer(game.currentChain.result.toString())
                if (!game.isWon) chains.add(game.currentChain)
            }
            return chains
        }
        assertEquals(playedChains(42), playedChains(42))
    }
}
