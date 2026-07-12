package com.example.smartalarm.feature.games.fifteen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты партии [FifteenGame] и параметров сложности [FifteenDifficulty].
 */
class FifteenGameTest {

    @Test
    fun `difficulty 1 is 3x3 board with 15 shuffle moves`() {
        val difficulty = FifteenDifficulty.forLevel(1)
        assertEquals(3, difficulty.boardSize)
        assertEquals(15, difficulty.shuffleMoves)
    }

    @Test
    fun `difficulty 2 is 3x3 board with 40 shuffle moves`() {
        val difficulty = FifteenDifficulty.forLevel(2)
        assertEquals(3, difficulty.boardSize)
        assertEquals(40, difficulty.shuffleMoves)
    }

    @Test
    fun `difficulty 3 is 4x4 board with 60 shuffle moves`() {
        val difficulty = FifteenDifficulty.forLevel(3)
        assertEquals(4, difficulty.boardSize)
        assertEquals(60, difficulty.shuffleMoves)
    }

    @Test
    fun `out of range levels are clamped to the nearest bound`() {
        assertEquals(FifteenDifficulty.forLevel(1), FifteenDifficulty.forLevel(0))
        assertEquals(FifteenDifficulty.forLevel(1), FifteenDifficulty.forLevel(-5))
        assertEquals(FifteenDifficulty.forLevel(3), FifteenDifficulty.forLevel(4))
        assertEquals(FifteenDifficulty.forLevel(3), FifteenDifficulty.forLevel(100))
    }

    @Test
    fun `new game is shuffled and not won`() {
        for (level in 1..3) {
            for (seed in 0L until 10L) {
                val game = FifteenGame(FifteenDifficulty.forLevel(level), Random(seed))
                assertFalse("Партия выиграна сразу (level=$level, seed=$seed)", game.isWon)
                assertEquals(0, game.moveCount)
                assertEquals(
                    FifteenDifficulty.forLevel(level).shuffleMoves,
                    game.shuffleMovesPerformed.size
                )
            }
        }
    }

    @Test
    fun `game with the same seed produces the same board`() {
        val first = FifteenGame(FifteenDifficulty.forLevel(3), Random(123))
        val second = FifteenGame(FifteenDifficulty.forLevel(3), Random(123))
        assertEquals(first.board.snapshot(), second.board.snapshot())
        assertEquals(first.shuffleMovesPerformed, second.shuffleMovesPerformed)
    }

    @Test
    fun `invalid move does not increase move count`() {
        val game = FifteenGame(FifteenDifficulty.forLevel(1), Random(5))
        assertFalse(game.moveTile(game.board.emptyIndex))
        assertEquals(0, game.moveCount)
    }

    @Test
    fun `valid moves increase move count`() {
        val game = FifteenGame(FifteenDifficulty.forLevel(1), Random(5))
        val movable = game.board.movableIndices().first()
        assertTrue(game.moveTile(movable))
        assertEquals(1, game.moveCount)
    }

    @Test
    fun `game is won after replaying the shuffle in reverse`() {
        for (seed in 0L until 10L) {
            val game = FifteenGame(FifteenDifficulty.forLevel(2), Random(seed))
            for (move in game.shuffleMovesPerformed.reversed()) {
                assertTrue(game.moveTile(move.toIndex))
            }
            assertTrue("Партия не выиграна (seed=$seed)", game.isWon)
            assertEquals(game.shuffleMovesPerformed.size, game.moveCount)
        }
    }

    @Test
    fun `move bonus rewards solving close to the minimum`() {
        val game = FifteenGame(FifteenDifficulty.forLevel(1), Random(9))
        val k = game.difficulty.shuffleMoves
        // До первого хода бонус максимален: 2K - 0.
        assertEquals(2 * k, game.moveBonus())
        // Решение обратной последовательностью — ровно K ходов, бонус K.
        for (move in game.shuffleMovesPerformed.reversed()) {
            assertTrue(game.moveTile(move.toIndex))
        }
        assertEquals(k, game.moveBonus())
    }

    @Test
    fun `move bonus is never negative`() {
        val game = FifteenGame(FifteenDifficulty.forLevel(1), Random(3))
        // Делаем много лишних ходов туда-обратно (по одному это отмена — допустима для игрока).
        repeat(3 * game.difficulty.shuffleMoves) {
            val movable = game.board.movableIndices().first()
            assertTrue(game.moveTile(movable))
        }
        assertTrue(game.moveCount > 2 * game.difficulty.shuffleMoves)
        assertEquals(0, game.moveBonus())
    }
}
