package com.example.smartalarm.feature.games.reverse.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Тесты словарей игры «Наоборот»: [ReverseWords]. */
class ReverseWordsTest {

    @Test
    fun easyWordsAre4To5LettersLong() {
        for (word in ReverseWords.EASY)
            assertTrue("«$word» должно быть из 4–5 букв", word.length in 4..5)
    }

    @Test
    fun mediumWordsAre6To7LettersLong() {
        for (word in ReverseWords.MEDIUM)
            assertTrue("«$word» должно быть из 6–7 букв", word.length in 6..7)
    }

    @Test
    fun hardWordsAre8OrMoreLettersLong() {
        for (word in ReverseWords.HARD)
            assertTrue("«$word» должно быть из 8+ букв", word.length >= 8)
    }

    @Test
    fun eachLevelHasAtLeast25Words() {
        assertTrue(ReverseWords.EASY.size >= 25)
        assertTrue(ReverseWords.MEDIUM.size >= 25)
        assertTrue(ReverseWords.HARD.size >= 25)
    }

    @Test
    fun noDuplicatesWithinAnyLevel() {
        for (level in listOf(ReverseWords.EASY, ReverseWords.MEDIUM, ReverseWords.HARD))
            assertEquals(level.distinct().size, level.size)
    }

    @Test
    fun noPalindromes_displayedWordNeverEqualsAnswer() {
        val allWords = ReverseWords.EASY + ReverseWords.MEDIUM + ReverseWords.HARD
        for (word in allWords)
            assertNotEquals(
                "«$word» — палиндром, перевёртыш совпал бы с ответом",
                word,
                word.reversed()
            )
    }

    @Test
    fun allWordsAreLowercaseRussian() {
        val allWords = ReverseWords.EASY + ReverseWords.MEDIUM + ReverseWords.HARD
        val russianLowercase = Regex("[а-яё]+")
        for (word in allWords)
            assertTrue("«$word» должно состоять из строчных русских букв", russianLowercase.matches(word))
    }

    @Test
    fun forDifficultyMapsLevelsAndClampsOutOfRange() {
        assertEquals(ReverseWords.EASY, ReverseWords.forDifficulty(1))
        assertEquals(ReverseWords.MEDIUM, ReverseWords.forDifficulty(2))
        assertEquals(ReverseWords.HARD, ReverseWords.forDifficulty(3))
        assertEquals(ReverseWords.EASY, ReverseWords.forDifficulty(0))
        assertEquals(ReverseWords.HARD, ReverseWords.forDifficulty(4))
    }
}
