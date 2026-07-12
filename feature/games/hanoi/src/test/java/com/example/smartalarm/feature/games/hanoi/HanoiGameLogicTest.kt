package com.example.smartalarm.feature.games.hanoi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Юнит-тесты чистой логики «Ханойской башни» [HanoiGameLogic].
 */
class HanoiGameLogicTest {

    @Test
    fun `initial state has all disks on first rod in descending order`() {
        for (n in listOf(3, 4, 5)) {
            val game = HanoiGameLogic(n)
            val rods = game.rodsSnapshot()
            assertEquals((n downTo 1).toList(), rods[0])
            assertTrue(rods[1].isEmpty())
            assertTrue(rods[2].isEmpty())
            assertEquals(0, game.moveCount)
            assertFalse(game.isWon)
        }
    }

    @Test
    fun `minMoves is 2 pow n minus 1`() {
        assertEquals(7, HanoiGameLogic(3).minMoves)
        assertEquals(15, HanoiGameLogic(4).minMoves)
        assertEquals(31, HanoiGameLogic(5).minMoves)
    }

    @Test
    fun `topDisk returns smallest disk on start rod and null for empty rods`() {
        val game = HanoiGameLogic(3)
        assertEquals(1, game.topDisk(0))
        assertNull(game.topDisk(1))
        assertNull(game.topDisk(2))
    }

    @Test
    fun `legal move transfers top disk and increments move count`() {
        val game = HanoiGameLogic(3)
        assertEquals(MoveResult.OK, game.move(0, 1))
        val rods = game.rodsSnapshot()
        assertEquals(listOf(3, 2), rods[0])
        assertEquals(listOf(1), rods[1])
        assertEquals(1, game.moveCount)
    }

    @Test
    fun `smaller disk can be placed on bigger disk`() {
        val game = HanoiGameLogic(3)
        assertEquals(MoveResult.OK, game.move(0, 2)) // диск 1 -> стержень 2
        assertEquals(MoveResult.OK, game.move(0, 1)) // диск 2 -> стержень 1
        assertEquals(MoveResult.OK, game.move(2, 1)) // диск 1 на диск 2 — можно
        assertEquals(listOf(2, 1), game.rodsSnapshot()[1])
        assertEquals(3, game.moveCount)
    }

    @Test
    fun `bigger disk cannot be placed on smaller disk`() {
        val game = HanoiGameLogic(3)
        assertEquals(MoveResult.OK, game.move(0, 1)) // диск 1 -> стержень 1
        val before = game.rodsSnapshot()
        // Верх стержня 0 — диск 2, класть на диск 1 нельзя.
        assertEquals(MoveResult.ILLEGAL, game.move(0, 1))
        assertEquals(before, game.rodsSnapshot())
        assertEquals(1, game.moveCount) // нелегальный ход не считается ходом
    }

    @Test
    fun `move from empty rod is illegal and not counted`() {
        val game = HanoiGameLogic(3)
        assertEquals(MoveResult.ILLEGAL, game.move(1, 2))
        assertEquals(MoveResult.ILLEGAL, game.move(2, 0))
        assertEquals(0, game.moveCount)
        assertEquals((3 downTo 1).toList(), game.rodsSnapshot()[0])
    }

    @Test
    fun `move to same rod is illegal and not counted`() {
        val game = HanoiGameLogic(3)
        assertEquals(MoveResult.ILLEGAL, game.move(0, 0))
        assertEquals(0, game.moveCount)
    }

    @Test
    fun `move with rod index out of range is illegal`() {
        val game = HanoiGameLogic(3)
        assertEquals(MoveResult.ILLEGAL, game.move(0, 3))
        assertEquals(MoveResult.ILLEGAL, game.move(-1, 2))
        assertEquals(MoveResult.ILLEGAL, game.move(3, 0))
        assertEquals(0, game.moveCount)
    }

    @Test
    fun `win only when tower is on third rod`() {
        val game = HanoiGameLogic(1)
        assertEquals(MoveResult.OK, game.move(0, 1)) // башня на среднем — не победа
        assertFalse(game.isWon)
        assertEquals(MoveResult.WIN, game.move(1, 2))
        assertTrue(game.isWon)
    }

    @Test
    fun `full tower on middle rod is not a win`() {
        val game = HanoiGameLogic(2)
        assertEquals(MoveResult.OK, game.move(0, 2))
        assertEquals(MoveResult.OK, game.move(0, 1))
        assertEquals(MoveResult.OK, game.move(2, 1)) // вся башня на стержне 1
        assertEquals(listOf(2, 1), game.rodsSnapshot()[1])
        assertFalse(game.isWon)
    }

    @Test
    fun `optimal recursive solution wins in exactly 2 pow n minus 1 moves`() {
        for (n in listOf(3, 4, 5)) {
            val game = HanoiGameLogic(n)
            val moves = mutableListOf<Pair<Int, Int>>()
            solve(n, from = 0, to = 2, via = 1, moves = moves)

            assertEquals(game.minMoves, moves.size)
            moves.forEachIndexed { index, (from, to) ->
                val expected = if (index == moves.size - 1) MoveResult.WIN else MoveResult.OK
                assertEquals("move #$index ($from -> $to), n=$n", expected, game.move(from, to))
            }
            assertTrue(game.isWon)
            assertEquals((1 shl n) - 1, game.moveCount)
            assertEquals((n downTo 1).toList(), game.rodsSnapshot()[2])
        }
    }

    @Test
    fun `disks for difficulty are 3 4 5`() {
        assertEquals(3, HanoiGameLogic.disksForDifficulty(1))
        assertEquals(4, HanoiGameLogic.disksForDifficulty(2))
        assertEquals(5, HanoiGameLogic.disksForDifficulty(3))
        assertEquals(5, HanoiGameLogic.disksForDifficulty(0)) // некорректный уровень — максимум
    }

    /** Классическое рекурсивное решение: собирает список ходов (from, to). */
    private fun solve(n: Int, from: Int, to: Int, via: Int, moves: MutableList<Pair<Int, Int>>) {
        if (n == 0) return
        solve(n - 1, from, via, to, moves)
        moves.add(from to to)
        solve(n - 1, via, to, from, moves)
    }
}
