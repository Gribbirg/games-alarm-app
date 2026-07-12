package com.example.smartalarm.feature.games.spelling

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Тесты базы слов игры «Как пишется?».
 */
class SpellingDataTest {

    private val allLevels = mapOf(
        1 to SpellingData.LEVEL_1,
        2 to SpellingData.LEVEL_2,
        3 to SpellingData.LEVEL_3
    )

    @Test
    fun `each level has at least 20 entries`() {
        allLevels.forEach { (level, entries) ->
            assertTrue(
                "На уровне $level меньше 20 записей: ${entries.size}",
                entries.size >= 20
            )
        }
    }

    @Test
    fun `each entry has 1 to 3 wrong variants`() {
        allLevels.forEach { (level, entries) ->
            entries.forEach { entry ->
                assertTrue(
                    "«${entry.correct}» (уровень $level): неверное число wrong-вариантов",
                    entry.wrong.size in 1..3
                )
            }
        }
    }

    @Test
    fun `wrong variants differ from correct spelling`() {
        allLevels.forEach { (level, entries) ->
            entries.forEach { entry ->
                assertFalse(
                    "«${entry.correct}» (уровень $level): wrong содержит эталон",
                    entry.wrong.contains(entry.correct)
                )
            }
        }
    }

    @Test
    fun `wrong variants are unique within an entry`() {
        allLevels.forEach { (level, entries) ->
            entries.forEach { entry ->
                assertEquals(
                    "«${entry.correct}» (уровень $level): wrong-варианты дублируются",
                    entry.wrong.size,
                    entry.wrong.toSet().size
                )
            }
        }
    }

    @Test
    fun `entries are not blank`() {
        allLevels.forEach { (_, entries) ->
            entries.forEach { entry ->
                assertTrue(entry.correct.isNotBlank())
                entry.wrong.forEach { assertTrue(it.isNotBlank()) }
            }
        }
    }

    @Test
    fun `correct spellings are unique within a level`() {
        allLevels.forEach { (level, entries) ->
            assertEquals(
                "Уровень $level: повторяются эталонные написания",
                entries.size,
                entries.map { it.correct }.toSet().size
            )
        }
    }

    @Test
    fun `entriesForDifficulty maps levels and clamps out-of-range values`() {
        assertEquals(SpellingData.LEVEL_1, SpellingData.entriesForDifficulty(1))
        assertEquals(SpellingData.LEVEL_2, SpellingData.entriesForDifficulty(2))
        assertEquals(SpellingData.LEVEL_3, SpellingData.entriesForDifficulty(3))
        assertEquals(SpellingData.LEVEL_1, SpellingData.entriesForDifficulty(0))
        assertEquals(SpellingData.LEVEL_3, SpellingData.entriesForDifficulty(4))
    }
}
