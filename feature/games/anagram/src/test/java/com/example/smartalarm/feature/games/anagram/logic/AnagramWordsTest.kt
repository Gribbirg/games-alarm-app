package com.example.smartalarm.feature.games.anagram.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** Тесты словарей игры «Анаграммы». */
class AnagramWordsTest {

    @Test
    fun easyWordsHaveLength4to5() {
        for (word in AnagramWords.EASY)
            assertTrue("«$word» должно быть из 4–5 букв", word.length in 4..5)
    }

    @Test
    fun mediumWordsHaveLength6to7() {
        for (word in AnagramWords.MEDIUM)
            assertTrue("«$word» должно быть из 6–7 букв", word.length in 6..7)
    }

    @Test
    fun hardWordsHaveLength8OrMore() {
        for (word in AnagramWords.HARD)
            assertTrue("«$word» должно быть из 8+ букв", word.length >= 8)
    }

    @Test
    fun eachDictionaryHasAtLeast25Words() {
        assertTrue(AnagramWords.EASY.size >= 25)
        assertTrue(AnagramWords.MEDIUM.size >= 25)
        assertTrue(AnagramWords.HARD.size >= 25)
    }

    @Test
    fun dictionariesHaveNoDuplicates() {
        assertEquals(AnagramWords.EASY.size, AnagramWords.EASY.distinct().size)
        assertEquals(AnagramWords.MEDIUM.size, AnagramWords.MEDIUM.distinct().size)
        assertEquals(AnagramWords.HARD.size, AnagramWords.HARD.distinct().size)
    }

    @Test
    fun wordsContainOnlyLowercaseRussianLetters() {
        val regex = Regex("[а-яё]+")
        for (word in AnagramWords.EASY + AnagramWords.MEDIUM + AnagramWords.HARD)
            assertTrue("«$word» должно состоять из строчных русских букв", regex.matches(word))
    }

    @Test
    fun everyWordHasAtLeastTwoDistinctLetters() {
        for (word in AnagramWords.EASY + AnagramWords.MEDIUM + AnagramWords.HARD)
            assertTrue(
                "«$word» должно перемешиваться (нужны хотя бы две разные буквы)",
                word.toList().distinct().size >= 2
            )
    }

    @Test
    fun forDifficultyReturnsMatchingDictionaryAndClampsOutOfRange() {
        assertSame(AnagramWords.EASY, AnagramWords.forDifficulty(1))
        assertSame(AnagramWords.MEDIUM, AnagramWords.forDifficulty(2))
        assertSame(AnagramWords.HARD, AnagramWords.forDifficulty(3))
        assertSame(AnagramWords.EASY, AnagramWords.forDifficulty(0))
        assertSame(AnagramWords.HARD, AnagramWords.forDifficulty(4))
    }
}
