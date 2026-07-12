package com.example.smartalarm.feature.games.fifteen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты чистой логики поля [FifteenBoard]: допустимость ходов, обратимость,
 * свойства перемешивания и детерминизм по seed.
 */
class FifteenBoardTest {

    @Test
    fun `new board is solved and empty cell is in the last position`() {
        val board = FifteenBoard(3)
        assertTrue(board.isSolved())
        assertEquals(9, board.cellCount)
        assertEquals(8, board.emptyIndex)
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8, 0), board.snapshot())
    }

    @Test
    fun `only neighbors of the empty cell are movable`() {
        val board = FifteenBoard(3)
        // Пустая клетка в углу (индекс 8): соседи — 5 (сверху) и 7 (слева).
        assertEquals(listOf(5, 7), board.movableIndices())
        assertTrue(board.canMove(5))
        assertTrue(board.canMove(7))
        assertFalse(board.canMove(0))
        // Диагональный сосед (индекс 4) не считается соседом.
        assertFalse(board.canMove(4))
        // Сама пустая клетка и индексы вне поля недопустимы.
        assertFalse(board.canMove(8))
        assertFalse(board.canMove(-1))
        assertFalse(board.canMove(9))
    }

    @Test
    fun `empty cell in the center has four movable neighbors`() {
        val board = FifteenBoard(3)
        board.moveTile(5) // пустая -> 5
        board.moveTile(4) // пустая -> 4 (центр)
        assertEquals(4, board.emptyIndex)
        assertEquals(listOf(1, 3, 5, 7), board.movableIndices())
    }

    @Test
    fun `moving a non-neighbor tile fails and does not change the board`() {
        val board = FifteenBoard(3)
        val before = board.snapshot()
        assertFalse(board.moveTile(0))
        assertFalse(board.moveTile(4))
        assertFalse(board.moveTile(8))
        assertEquals(before, board.snapshot())
        assertEquals(8, board.emptyIndex)
    }

    @Test
    fun `moving a neighbor tile swaps it with the empty cell`() {
        val board = FifteenBoard(3)
        assertTrue(board.moveTile(5))
        assertEquals(5, board.emptyIndex)
        assertEquals(0, board.tileAt(5))
        assertEquals(6, board.tileAt(8))
        assertFalse(board.isSolved())
    }

    @Test
    fun `reverse move restores the previous state`() {
        val board = FifteenBoard(3)
        val before = board.snapshot()
        assertTrue(board.moveTile(5))
        // Плитка ушла с 5 на 8; обратный ход — сдвинуть плитку с 8.
        assertTrue(board.moveTile(8))
        assertEquals(before, board.snapshot())
        assertTrue(board.isSolved())
    }

    @Test
    fun `shuffled board is not solved`() {
        for (seed in 0L until 50L) {
            val board = FifteenBoard(3)
            board.shuffle(15, Random(seed))
            assertFalse("Поле собрано после перемешивания (seed=$seed)", board.isSolved())
        }
    }

    @Test
    fun `shuffled board is solvable by the reversed move sequence`() {
        for (seed in 0L until 20L) {
            val board = FifteenBoard(4)
            val moves = board.shuffle(60, Random(seed))
            assertEquals(60, moves.size)
            for (move in moves.reversed()) {
                assertTrue(
                    "Обратный ход недопустим (seed=$seed, move=$move)",
                    board.moveTile(move.toIndex)
                )
            }
            assertTrue("Поле не собралось обратными ходами (seed=$seed)", board.isSolved())
        }
    }

    @Test
    fun `shuffle never immediately undoes the previous move`() {
        for (seed in 0L until 50L) {
            val board = FifteenBoard(3)
            val moves = board.shuffle(40, Random(seed))
            moves.zipWithNext { previous, next ->
                assertNotEquals(
                    "Ход $next отменяет предыдущий $previous (seed=$seed)",
                    previous.toIndex, next.fromIndex
                )
            }
        }
    }

    @Test
    fun `every shuffle move goes into the current empty cell`() {
        val board = FifteenBoard(3)
        val moves = board.shuffle(40, Random(7))
        // Восстанавливаем партию заново теми же ходами и проверяем корректность.
        val replay = FifteenBoard(3)
        for (move in moves) {
            assertEquals(move.toIndex, replay.emptyIndex)
            assertTrue(replay.moveTile(move.fromIndex))
        }
        assertEquals(board.snapshot(), replay.snapshot())
    }

    @Test
    fun `shuffle is deterministic for the same seed`() {
        val first = FifteenBoard(4)
        val second = FifteenBoard(4)
        val firstMoves = first.shuffle(60, Random(42))
        val secondMoves = second.shuffle(60, Random(42))
        assertEquals(firstMoves, secondMoves)
        assertEquals(first.snapshot(), second.snapshot())
    }

    @Test
    fun `reset returns the board to the solved state`() {
        val board = FifteenBoard(3)
        board.shuffle(15, Random(1))
        board.reset()
        assertTrue(board.isSolved())
        assertEquals(8, board.emptyIndex)
    }

    @Test
    fun `board is solved again when tiles are back in order`() {
        val board = FifteenBoard(3)
        assertTrue(board.moveTile(7))
        assertTrue(board.moveTile(6))
        assertFalse(board.isSolved())
        assertTrue(board.moveTile(7))
        assertTrue(board.moveTile(8))
        assertTrue(board.isSolved())
    }
}
