package com.example.smartalarm.feature.games.mole

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Тесты чистой логики игры «Поймай крота»:
 * параметры сложности, выбор следующей норы, попадания/промахи, победа, очки.
 */
class MoleLogicTest {

    // ---- Параметры сложности ----

    @Test
    fun difficultyLevel1() {
        val d = MoleDifficulty.forLevel(1)
        assertEquals(3, d.gridSize)
        assertEquals(1200L, d.showTimeMs)
        assertEquals(5, d.targetHits)
        assertEquals(9, d.holeCount)
    }

    @Test
    fun difficultyLevel2() {
        val d = MoleDifficulty.forLevel(2)
        assertEquals(3, d.gridSize)
        assertEquals(900L, d.showTimeMs)
        assertEquals(7, d.targetHits)
        assertEquals(9, d.holeCount)
    }

    @Test
    fun difficultyLevel3() {
        val d = MoleDifficulty.forLevel(3)
        assertEquals(4, d.gridSize)
        assertEquals(650L, d.showTimeMs)
        assertEquals(10, d.targetHits)
        assertEquals(16, d.holeCount)
    }

    @Test
    fun difficultyOutOfRangeIsCoerced() {
        assertEquals(MoleDifficulty.forLevel(1), MoleDifficulty.forLevel(0))
        assertEquals(MoleDifficulty.forLevel(1), MoleDifficulty.forLevel(-5))
        assertEquals(MoleDifficulty.forLevel(3), MoleDifficulty.forLevel(4))
        assertEquals(MoleDifficulty.forLevel(3), MoleDifficulty.forLevel(100))
    }

    // ---- Выбор следующей норы ----

    @Test
    fun moleStartsHidden() {
        val game = MoleGame(MoleDifficulty.forLevel(1), Random(1))
        assertEquals(MoleGame.NO_MOLE, game.currentHole)
    }

    @Test
    fun showMoleReturnsHoleWithinBoundsAndNotEqualToPrevious() {
        for (level in 1..3) {
            val difficulty = MoleDifficulty.forLevel(level)
            val game = MoleGame(difficulty, Random(42L + level))
            var previous = MoleGame.NO_MOLE
            repeat(1000) {
                val hole = game.showMole()
                assertTrue("нора $hole вне поля", hole in 0 until difficulty.holeCount)
                assertEquals(hole, game.currentHole)
                if (previous != MoleGame.NO_MOLE)
                    assertNotEquals("крот появился в той же норе", previous, hole)
                previous = hole
            }
        }
    }

    @Test
    fun nextHoleDiffersFromPreviousEvenAfterMoleHidItself() {
        val game = MoleGame(MoleDifficulty.forLevel(1), Random(7))
        repeat(500) {
            val shown = game.showMole()
            game.hideMole()
            val next = game.showMole()
            assertNotEquals(shown, next)
            game.hideMole()
        }
    }

    @Test
    fun sameSeedGivesSameHoleSequence() {
        val a = MoleGame(MoleDifficulty.forLevel(3), Random(123))
        val b = MoleGame(MoleDifficulty.forLevel(3), Random(123))
        repeat(200) {
            assertEquals(a.showMole(), b.showMole())
        }
    }

    @Test
    fun allHolesExceptPreviousAreReachable() {
        val difficulty = MoleDifficulty.forLevel(1)
        val game = MoleGame(difficulty, Random(99))
        val seen = mutableSetOf<Int>()
        repeat(2000) { seen.add(game.showMole()) }
        assertEquals((0 until difficulty.holeCount).toSet(), seen)
    }

    // ---- Попадания и промахи ----

    @Test
    fun tapOnMoleIsHitAndHidesMole() {
        val game = MoleGame(MoleDifficulty.forLevel(1), Random(1))
        val hole = game.showMole()
        assertEquals(MoleTapResult.HIT, game.tap(hole))
        assertEquals(1, game.hits)
        assertEquals(0, game.misses)
        assertEquals(MoleGame.NO_MOLE, game.currentHole)
    }

    @Test
    fun tapOnEmptyHoleIsMiss() {
        val game = MoleGame(MoleDifficulty.forLevel(1), Random(1))
        val hole = game.showMole()
        val empty = (hole + 1) % game.difficulty.holeCount
        assertEquals(MoleTapResult.MISS, game.tap(empty))
        assertEquals(0, game.hits)
        assertEquals(1, game.misses)
        assertEquals(hole, game.currentHole)
    }

    @Test
    fun tapWhenNoMoleIsMiss() {
        val game = MoleGame(MoleDifficulty.forLevel(2), Random(5))
        assertEquals(MoleTapResult.MISS, game.tap(0))
        assertEquals(1, game.misses)

        val hole = game.showMole()
        game.tap(hole)
        assertEquals(MoleTapResult.MISS, game.tap(hole))
        assertEquals(2, game.misses)
        assertEquals(1, game.hits)
    }

    @Test
    fun hideMoleDoesNotPenalize() {
        val game = MoleGame(MoleDifficulty.forLevel(1), Random(3))
        game.showMole()
        game.hideMole()
        assertEquals(0, game.hits)
        assertEquals(0, game.misses)
        assertEquals(MoleGame.NO_MOLE, game.currentHole)
    }

    // ---- Прогресс и победа ----

    @Test
    fun winAfterTargetHits() {
        for (level in 1..3) {
            val difficulty = MoleDifficulty.forLevel(level)
            val game = MoleGame(difficulty, Random(11L + level))
            repeat(difficulty.targetHits - 1) {
                game.tap(game.showMole())
                assertFalse("победа раньше времени", game.isWon)
            }
            game.tap(game.showMole())
            assertTrue(game.isWon)
            assertEquals(difficulty.targetHits, game.hits)
        }
    }

    @Test
    fun missesDoNotAffectProgress() {
        val game = MoleGame(MoleDifficulty.forLevel(1), Random(17))
        val hole = game.showMole()
        repeat(20) { game.tap((hole + 1) % game.difficulty.holeCount) }
        assertEquals(20, game.misses)
        assertEquals(0, game.hits)
        assertFalse(game.isWon)
    }

    // ---- Очки ----

    @Test
    fun missPenaltyIsTen() {
        assertEquals(10, MoleGame.MISS_PENALTY)
    }

    @Test
    fun finishBonusFormula() {
        assertEquals(600, finishBonus(0, 1))
        assertEquals(570, finishBonus(30, 1))
        assertEquals(1140, finishBonus(30, 2))
        assertEquals(1650, finishBonus(50, 3))
        assertEquals(0, finishBonus(600, 2))
        assertEquals(-30, finishBonus(630, 1))
    }
}
