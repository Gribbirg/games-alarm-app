package com.example.smartalarm.feature.games.maze

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты [MazeGameLogic]: размеры по сложности, движение сквозь проходы,
 * блокировка стенами, победа на выходе.
 */
class MazeGameLogicTest {

    @Test
    fun sizeForDifficultyMapping() {
        assertEquals(7, MazeGameLogic.sizeForDifficulty(1))
        assertEquals(9, MazeGameLogic.sizeForDifficulty(2))
        assertEquals(11, MazeGameLogic.sizeForDifficulty(3))
        // Некорректные значения не должны ломать игру — размер по умолчанию.
        assertEquals(7, MazeGameLogic.sizeForDifficulty(0))
        assertEquals(7, MazeGameLogic.sizeForDifficulty(99))
    }

    @Test
    fun playerStartsAtTopLeft_exitAtBottomRight() {
        val logic = MazeGameLogic(Maze.generate(7, 7, Random(1)))
        assertEquals(0, logic.playerRow)
        assertEquals(0, logic.playerCol)
        assertEquals(6, logic.exitRow)
        assertEquals(6, logic.exitCol)
        assertFalse(logic.isFinished)
    }

    @Test
    fun moveIntoWallIsBlockedAndKeepsPosition() {
        // Вручную собранный лабиринт 2x2: открыт только коридор (0,0)->(0,1)->(1,1).
        val maze = Maze(2, 2)
        maze.removeWall(0, 0, Direction.RIGHT)
        maze.removeWall(0, 1, Direction.DOWN)
        val logic = MazeGameLogic(maze)

        // Вверх и влево — внешние границы, вниз — стена.
        assertEquals(MoveResult.BLOCKED, logic.tryMove(Direction.UP))
        assertEquals(MoveResult.BLOCKED, logic.tryMove(Direction.LEFT))
        assertEquals(MoveResult.BLOCKED, logic.tryMove(Direction.DOWN))
        assertEquals(0, logic.playerRow)
        assertEquals(0, logic.playerCol)
    }

    @Test
    fun moveThroughPassageMovesPlayer() {
        val maze = Maze(2, 2)
        maze.removeWall(0, 0, Direction.RIGHT)
        maze.removeWall(0, 1, Direction.DOWN)
        val logic = MazeGameLogic(maze)

        assertEquals(MoveResult.MOVED, logic.tryMove(Direction.RIGHT))
        assertEquals(0, logic.playerRow)
        assertEquals(1, logic.playerCol)
        assertFalse(logic.isFinished)
    }

    @Test
    fun reachingExitReturnsFinished() {
        val maze = Maze(2, 2)
        maze.removeWall(0, 0, Direction.RIGHT)
        maze.removeWall(0, 1, Direction.DOWN)
        val logic = MazeGameLogic(maze)

        assertEquals(MoveResult.MOVED, logic.tryMove(Direction.RIGHT))
        assertEquals(MoveResult.FINISHED, logic.tryMove(Direction.DOWN))
        assertTrue(logic.isFinished)
        assertEquals(logic.exitRow, logic.playerRow)
        assertEquals(logic.exitCol, logic.playerCol)
    }

    @Test
    fun movesAfterFinishDoNotChangePosition() {
        val maze = Maze(2, 2)
        maze.removeWall(0, 0, Direction.RIGHT)
        maze.removeWall(0, 1, Direction.DOWN)
        maze.removeWall(1, 0, Direction.RIGHT)
        val logic = MazeGameLogic(maze)

        logic.tryMove(Direction.RIGHT)
        logic.tryMove(Direction.DOWN)
        assertTrue(logic.isFinished)

        // Дальше игра завершена: любой ход — FINISHED, позиция не меняется.
        assertEquals(MoveResult.FINISHED, logic.tryMove(Direction.LEFT))
        assertEquals(logic.exitRow, logic.playerRow)
        assertEquals(logic.exitCol, logic.playerCol)
    }

    @Test
    fun blockedMoveAtStartOfGeneratedMaze() {
        // Верхняя и левая границы — всегда стены, из старта туда не пройти.
        for (seed in listOf(0L, 7L, 100L)) {
            val logic = MazeGameLogic(Maze.generate(9, 9, Random(seed)))
            assertEquals(MoveResult.BLOCKED, logic.tryMove(Direction.UP))
            assertEquals(MoveResult.BLOCKED, logic.tryMove(Direction.LEFT))
            assertEquals(0, logic.playerRow)
            assertEquals(0, logic.playerCol)
        }
    }

    @Test
    fun bfsPathFromStartLeadsToVictoryInGeneratedMazes() {
        for (size in listOf(7, 9, 11)) for (seed in listOf(3L, 14L, 15L)) {
            val maze = Maze.generate(size, size, Random(seed))
            val logic = MazeGameLogic(maze)
            val path = bfsPath(maze)
            assertTrue("путь до выхода существует", path.isNotEmpty())

            path.forEachIndexed { index, direction ->
                val expected =
                    if (index == path.size - 1) MoveResult.FINISHED else MoveResult.MOVED
                assertEquals(
                    "шаг $index (size=$size, seed=$seed)",
                    expected,
                    logic.tryMove(direction)
                )
            }
            assertTrue(logic.isFinished)
        }
    }

    /** Кратчайший путь BFS из (0,0) в правый нижний угол как список направлений. */
    private fun bfsPath(maze: Maze): List<Direction> {
        val start = 0 to 0
        val target = (maze.rows - 1) to (maze.cols - 1)
        val cameFrom = HashMap<Pair<Int, Int>, Pair<Pair<Int, Int>, Direction>>()
        val visited = HashSet<Pair<Int, Int>>()
        val queue = ArrayDeque<Pair<Int, Int>>()
        visited.add(start)
        queue.add(start)

        while (queue.isNotEmpty()) {
            val cell = queue.removeFirst()
            if (cell == target) break
            for (direction in Direction.entries) {
                if (!maze.canMove(cell.first, cell.second, direction)) continue
                val next = (cell.first + direction.dRow) to (cell.second + direction.dCol)
                if (next in visited) continue
                visited.add(next)
                cameFrom[next] = cell to direction
                queue.add(next)
            }
        }

        if (target !in cameFrom && target != start) return emptyList()
        val path = ArrayDeque<Direction>()
        var cell = target
        while (cell != start) {
            val (prev, direction) = cameFrom.getValue(cell)
            path.addFirst(direction)
            cell = prev
        }
        return path.toList()
    }
}
