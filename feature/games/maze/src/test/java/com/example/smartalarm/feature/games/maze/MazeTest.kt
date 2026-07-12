package com.example.smartalarm.feature.games.maze

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты модели [Maze] и генератора: идеальность лабиринта,
 * согласованность стен, внешние границы, детерминизм по seed.
 */
class MazeTest {

    private val sizes = listOf(7, 9, 11)
    private val seeds = listOf(0L, 1L, 42L, 2026L)

    @Test
    fun newMazeHasAllWalls() {
        val maze = Maze(3, 3)
        for (row in 0 until 3)
            for (col in 0 until 3)
                for (direction in Direction.entries)
                    assertTrue(maze.hasWall(row, col, direction))
    }

    @Test
    fun borderWallsAlwaysPresentInGeneratedMaze() {
        for (size in sizes) for (seed in seeds) {
            val maze = Maze.generate(size, size, Random(seed))
            for (col in 0 until size) {
                assertTrue(maze.hasWall(0, col, Direction.UP))
                assertTrue(maze.hasWall(size - 1, col, Direction.DOWN))
            }
            for (row in 0 until size) {
                assertTrue(maze.hasWall(row, 0, Direction.LEFT))
                assertTrue(maze.hasWall(row, size - 1, Direction.RIGHT))
            }
        }
    }

    @Test
    fun removingOuterWallThrows() {
        val maze = Maze(2, 2)
        assertThrows(IllegalArgumentException::class.java) {
            maze.removeWall(0, 0, Direction.UP)
        }
        assertThrows(IllegalArgumentException::class.java) {
            maze.removeWall(1, 1, Direction.RIGHT)
        }
    }

    @Test
    fun generatedMazeIsPerfect_allCellsReachable_andPassagesEqualCellsMinusOne() {
        for (size in sizes) for (seed in seeds) {
            val maze = Maze.generate(size, size, Random(seed))

            val reachable = bfsReachableCount(maze)
            assertEquals("все клетки достижимы (size=$size, seed=$seed)", size * size, reachable)

            assertEquals(
                "число проходов = клетки − 1 (size=$size, seed=$seed)",
                size * size - 1,
                countPassages(maze)
            )
        }
    }

    @Test
    fun wallsAreConsistentBetweenNeighbours() {
        for (size in sizes) for (seed in seeds) {
            val maze = Maze.generate(size, size, Random(seed))
            for (row in 0 until size)
                for (col in 0 until size)
                    for (direction in Direction.entries) {
                        val nRow = row + direction.dRow
                        val nCol = col + direction.dCol
                        if (maze.isInside(nRow, nCol)) {
                            assertEquals(
                                "стена ($row,$col)->$direction согласована с соседом",
                                maze.hasWall(row, col, direction),
                                maze.hasWall(nRow, nCol, direction.opposite)
                            )
                            assertEquals(
                                maze.canMove(row, col, direction),
                                maze.canMove(nRow, nCol, direction.opposite)
                            )
                        }
                    }
        }
    }

    @Test
    fun sameSeedProducesSameMaze() {
        for (size in sizes) {
            val first = Maze.generate(size, size, Random(123))
            val second = Maze.generate(size, size, Random(123))
            assertEquals(wallsSignature(first), wallsSignature(second))
        }
    }

    @Test
    fun differentSeedsProduceDifferentMazes() {
        val first = Maze.generate(11, 11, Random(1))
        val second = Maze.generate(11, 11, Random(2))
        assertNotEquals(wallsSignature(first), wallsSignature(second))
    }

    @Test
    fun removeWallOpensPassageBothWays() {
        val maze = Maze(2, 2)
        maze.removeWall(0, 0, Direction.RIGHT)
        assertFalse(maze.hasWall(0, 0, Direction.RIGHT))
        assertFalse(maze.hasWall(0, 1, Direction.LEFT))
        assertTrue(maze.canMove(0, 0, Direction.RIGHT))
        assertTrue(maze.canMove(0, 1, Direction.LEFT))
    }

    /** Число достижимых из (0,0) клеток (BFS по проходам). */
    private fun bfsReachableCount(maze: Maze): Int {
        val visited = Array(maze.rows) { BooleanArray(maze.cols) }
        val queue = ArrayDeque<Pair<Int, Int>>()
        visited[0][0] = true
        queue.add(0 to 0)
        var count = 0
        while (queue.isNotEmpty()) {
            val (row, col) = queue.removeFirst()
            count++
            for (direction in Direction.entries) {
                val nRow = row + direction.dRow
                val nCol = col + direction.dCol
                if (maze.canMove(row, col, direction) && !visited[nRow][nCol]) {
                    visited[nRow][nCol] = true
                    queue.add(nRow to nCol)
                }
            }
        }
        return count
    }

    /** Число проходов (убранных внутренних стен): пары вправо и вниз. */
    private fun countPassages(maze: Maze): Int {
        var passages = 0
        for (row in 0 until maze.rows)
            for (col in 0 until maze.cols) {
                if (maze.canMove(row, col, Direction.RIGHT)) passages++
                if (maze.canMove(row, col, Direction.DOWN)) passages++
            }
        return passages
    }

    /** Строковая сигнатура всех стен лабиринта для сравнения на равенство. */
    private fun wallsSignature(maze: Maze): String {
        val sb = StringBuilder()
        for (row in 0 until maze.rows)
            for (col in 0 until maze.cols)
                for (direction in Direction.entries)
                    sb.append(if (maze.hasWall(row, col, direction)) '1' else '0')
        return sb.toString()
    }
}
