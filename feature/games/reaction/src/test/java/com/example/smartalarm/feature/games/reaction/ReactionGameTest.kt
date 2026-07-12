package com.example.smartalarm.feature.games.reaction

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Тесты чистой логики [ReactionGame]: попадания/промахи, перегенерация
 * зоны, прогресс, победа, очки и детерминизм.
 */
class ReactionGameTest {

    /** Позиция гарантированно внутри текущей зоны (её середина). */
    private fun insideZone(game: ReactionGame): Double =
        (game.zone.start + game.zone.end) / 2

    /** Позиция гарантированно вне текущей зоны (дальний край шкалы). */
    private fun outsideZone(game: ReactionGame): Double =
        if (game.zone.contains(0.0)) 1.0 else 0.0

    @Test
    fun `hit increases progress and score and regenerates zone`() {
        val game = ReactionGame(1, Random(42))
        val zonesSeen = HashSet<ReactionZone>()

        zonesSeen.add(game.zone)
        assertTrue(game.press(insideZone(game)))
        zonesSeen.add(game.zone)

        assertEquals(1, game.hitCount)
        assertEquals(0, game.missCount)
        assertEquals(ReactionGame.SCORE_PER_HIT, game.score)
        // Вероятность совпадения двух случайных зон ничтожна
        assertEquals(2, zonesSeen.size)
    }

    @Test
    fun `miss increases miss count, subtracts score and keeps the zone`() {
        val game = ReactionGame(2, Random(7))
        val zoneBefore = game.zone

        assertFalse(game.press(outsideZone(game)))

        assertEquals(0, game.hitCount)
        assertEquals(1, game.missCount)
        assertEquals(ReactionGame.SCORE_PER_MISS, game.score)
        assertEquals(zoneBefore, game.zone)
    }

    @Test
    fun `hits on zone boundaries count`() {
        val game = ReactionGame(3, Random(11))
        assertTrue(game.press(game.zone.start))
        assertTrue(game.press(game.zone.end))
        assertEquals(2, game.hitCount)
    }

    @Test
    fun `game is won after exactly hitsToWin hits`() {
        for (difficulty in 1..3) {
            val game = ReactionGame(difficulty, Random(difficulty))
            repeat(game.settings.hitsToWin - 1) {
                assertTrue(game.press(insideZone(game)))
                assertFalse(game.isWon)
            }
            assertTrue(game.press(insideZone(game)))
            assertTrue(game.isWon)
            assertEquals(game.settings.hitsToWin, game.hitCount)
        }
    }

    @Test
    fun `misses do not bring victory closer`() {
        val game = ReactionGame(1, Random(5))
        repeat(50) {
            game.press(outsideZone(game))
        }
        assertEquals(0, game.hitCount)
        assertEquals(50, game.missCount)
        assertFalse(game.isWon)
        assertEquals(50 * ReactionGame.SCORE_PER_MISS, game.score)
    }

    @Test
    fun `score combines hits and misses`() {
        val game = ReactionGame(2, Random(3))

        game.press(outsideZone(game))
        assertEquals(ReactionGame.SCORE_PER_MISS, game.score)

        game.press(insideZone(game))
        assertEquals(ReactionGame.SCORE_PER_MISS + ReactionGame.SCORE_PER_HIT, game.score)
    }

    @Test
    fun `presses after victory are ignored`() {
        val game = ReactionGame(1, Random(9))
        repeat(game.settings.hitsToWin) {
            game.press(insideZone(game))
        }
        assertTrue(game.isWon)

        val hits = game.hitCount
        val misses = game.missCount
        val score = game.score
        val zone = game.zone

        assertFalse(game.press(insideZone(game)))
        assertFalse(game.press(outsideZone(game)))

        assertEquals(hits, game.hitCount)
        assertEquals(misses, game.missCount)
        assertEquals(score, game.score)
        assertEquals(zone, game.zone)
    }

    @Test
    fun `zone always matches difficulty width and stays within scale`() {
        for (difficulty in 1..3) {
            val game = ReactionGame(difficulty, Random(2024 + difficulty))
            repeat(200) {
                assertTrue(game.zone.start >= 0.0)
                assertTrue(game.zone.end <= 1.0)
                assertEquals(game.settings.zoneWidth, game.zone.width, EPS)
                // Промах не меняет зону, попадание перегенерирует — чередуем
                game.press(outsideZone(game))
                if (game.hitCount < game.settings.hitsToWin - 1)
                    game.press(insideZone(game))
            }
        }
    }

    @Test
    fun `same seed produces identical games`() {
        for (difficulty in 1..3) {
            val game1 = ReactionGame(difficulty, Random(777))
            val game2 = ReactionGame(difficulty, Random(777))
            repeat(game1.settings.hitsToWin) {
                assertEquals(game1.zone, game2.zone)
                val position = insideZone(game1)
                assertEquals(game1.press(position), game2.press(position))
                assertEquals(game1.score, game2.score)
            }
        }
    }

    companion object {
        private const val EPS = 1e-9
    }
}
