package com.example.smartalarm.feature.games.pairs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты генерации колоды: размер и состав по сложностям, детерминизм.
 */
class PairsDeckTest {

    @Test
    fun `deck size is 6 for difficulty 1`() {
        assertEquals(6, generatePairsDeck(1, Random(42)).size)
    }

    @Test
    fun `deck size is 12 for difficulty 2`() {
        assertEquals(12, generatePairsDeck(2, Random(42)).size)
    }

    @Test
    fun `deck size is 16 for difficulty 3`() {
        assertEquals(16, generatePairsDeck(3, Random(42)).size)
    }

    @Test
    fun `unknown difficulty falls back to easiest deck`() {
        assertEquals(6, generatePairsDeck(0, Random(42)).size)
        assertEquals(6, generatePairsDeck(99, Random(42)).size)
    }

    @Test
    fun `every symbol appears exactly twice`() {
        for (difficulty in 1..3) {
            val deck = generatePairsDeck(difficulty, Random(difficulty.toLong()))
            val counts = deck.groupingBy { it }.eachCount()
            assertEquals(pairsCountFor(difficulty), counts.size)
            counts.forEach { (symbol, count) ->
                assertEquals("symbol $symbol", 2, count)
            }
        }
    }

    @Test
    fun `deck symbols come from the shared pool`() {
        val deck = generatePairsDeck(3, Random(7))
        assertTrue(PAIRS_SYMBOLS.containsAll(deck))
    }

    @Test
    fun `same seed produces same deck`() {
        for (difficulty in 1..3) {
            assertEquals(
                generatePairsDeck(difficulty, Random(123)),
                generatePairsDeck(difficulty, Random(123))
            )
        }
    }

    @Test
    fun `pairs count matches deck size`() {
        assertEquals(3, pairsCountFor(1))
        assertEquals(6, pairsCountFor(2))
        assertEquals(8, pairsCountFor(3))
    }

    @Test
    fun `grid columns follow difficulty`() {
        assertEquals(2, pairsColumnsFor(1))
        assertEquals(3, pairsColumnsFor(2))
        assertEquals(4, pairsColumnsFor(3))
        assertEquals(2, pairsColumnsFor(0))
    }

    @Test
    fun `deck size is divisible by column count`() {
        for (difficulty in 1..3) {
            val deck = generatePairsDeck(difficulty, Random(5))
            assertEquals(0, deck.size % pairsColumnsFor(difficulty))
        }
    }
}

/**
 * Тесты конечного автомата ходов [PairsGame]: совпадения, несовпадения,
 * игнорирование кликов, победа и счёт ошибок.
 */
class PairsGameTest {

    /** Колода из двух пар с известным расположением. */
    private fun newGame() = PairsGame(listOf("A", "B", "A", "B"))

    @Test
    fun `first click reveals card`() {
        val game = newGame()
        val result = game.openCard(0)
        assertEquals(PairsMoveResult.FirstRevealed(0), result)
        assertTrue(game.isRevealed(0))
        assertFalse(game.isMatched(0))
    }

    @Test
    fun `matching second card keeps both open`() {
        val game = newGame()
        game.openCard(0)
        val result = game.openCard(2)
        assertEquals(PairsMoveResult.Match(0, 2, isWin = false), result)
        assertTrue(game.isMatched(0))
        assertTrue(game.isMatched(2))
        assertEquals(1, game.matchedPairsCount)
        assertEquals(0, game.mistakes)
        assertFalse(game.isWon)
    }

    @Test
    fun `mismatch counts as mistake and stays visible until hidden`() {
        val game = newGame()
        game.openCard(0)
        val result = game.openCard(1)
        assertEquals(PairsMoveResult.Mismatch(0, 1), result)
        assertEquals(1, game.mistakes)
        assertTrue(game.hasPendingMismatch)
        assertTrue(game.isRevealed(0))
        assertTrue(game.isRevealed(1))

        // Пока несовпадение показывается — любые клики игнорируются.
        assertEquals(PairsMoveResult.Ignored, game.openCard(2))

        assertEquals(0 to 1, game.hideMismatched())
        assertFalse(game.hasPendingMismatch)
        assertFalse(game.isRevealed(0))
        assertFalse(game.isRevealed(1))
    }

    @Test
    fun `hideMismatched returns null when nothing is pending`() {
        val game = newGame()
        assertNull(game.hideMismatched())
        game.openCard(0)
        assertNull(game.hideMismatched())
        assertTrue(game.isRevealed(0))
    }

    @Test
    fun `clicking the same card twice is ignored`() {
        val game = newGame()
        game.openCard(0)
        assertEquals(PairsMoveResult.Ignored, game.openCard(0))
        // Карточка всё ещё открыта как первая в ходе.
        assertTrue(game.isRevealed(0))
        assertEquals(0, game.mistakes)
    }

    @Test
    fun `clicking a matched card is ignored`() {
        val game = newGame()
        game.openCard(0)
        game.openCard(2)
        assertEquals(PairsMoveResult.Ignored, game.openCard(0))
        // Найденная карточка не может стать первой картой нового хода.
        val result = game.openCard(1)
        assertEquals(PairsMoveResult.FirstRevealed(1), result)
    }

    @Test
    fun `out of range click is ignored`() {
        val game = newGame()
        assertEquals(PairsMoveResult.Ignored, game.openCard(-1))
        assertEquals(PairsMoveResult.Ignored, game.openCard(4))
    }

    @Test
    fun `last match wins the game`() {
        val game = newGame()
        game.openCard(0)
        game.openCard(2)
        game.openCard(1)
        val result = game.openCard(3)
        assertEquals(PairsMoveResult.Match(1, 3, isWin = true), result)
        assertTrue(game.isWon)
        assertEquals(2, game.matchedPairsCount)
    }

    @Test
    fun `mistakes accumulate across turns`() {
        val game = newGame()
        game.openCard(0)
        game.openCard(1)
        game.hideMismatched()
        game.openCard(2)
        game.openCard(3)
        game.hideMismatched()
        assertEquals(2, game.mistakes)
        assertEquals(0, game.matchedPairsCount)
    }

    @Test
    fun `full random deck can always be solved`() {
        val deck = generatePairsDeck(3, Random(2024))
        val game = PairsGame(deck)
        // Находим пары по известной колоде: для каждого символа — оба индекса.
        val indicesBySymbol = deck.withIndex().groupBy({ it.value }, { it.index })
        var lastResult: PairsMoveResult? = null
        for ((_, indices) in indicesBySymbol) {
            game.openCard(indices[0])
            lastResult = game.openCard(indices[1])
        }
        assertTrue(game.isWon)
        assertEquals(0, game.mistakes)
        assertEquals(8, game.matchedPairsCount)
        assertTrue((lastResult as PairsMoveResult.Match).isWin)
    }

    @Test
    fun `empty deck is not won`() {
        assertFalse(PairsGame(emptyList()).isWon)
    }
}
