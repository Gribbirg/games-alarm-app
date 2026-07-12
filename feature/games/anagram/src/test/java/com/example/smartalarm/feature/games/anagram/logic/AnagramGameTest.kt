package com.example.smartalarm.feature.games.anagram.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Тесты чистой логики игры «Анаграммы»: [shuffleWord] и [AnagramGame]. */
class AnagramGameTest {

    // ---------- shuffleWord ----------

    @Test
    fun shuffleAlwaysDiffersFromOriginalForAllDictionaryWords() {
        val allWords = AnagramWords.EASY + AnagramWords.MEDIUM + AnagramWords.HARD
        for ((i, word) in allWords.withIndex()) {
            val shuffled = shuffleWord(word, Random(i)).joinToString("")
            assertNotEquals("перемешивание «$word» не должно совпасть с исходным", word, shuffled)
        }
    }

    @Test
    fun shuffleKeepsTheSameMultisetOfLetters() {
        val word = "будильник"
        val shuffled = shuffleWord(word, Random(1))
        assertEquals(word.toList().sorted(), shuffled.sorted())
    }

    @Test
    fun shuffleOfSingleLetterWordReturnsItUnchanged() {
        // Слово из одной повторяющейся буквы перемешать «по-другому» нельзя.
        assertEquals(listOf('а', 'а', 'а'), shuffleWord("ааа", Random(1)))
    }

    @Test
    fun shuffleIsDeterministicWithSeed() {
        assertEquals(shuffleWord("квартира", Random(42)), shuffleWord("квартира", Random(42)))
    }

    // ---------- выбор слов ----------

    @Test
    fun wordCountForDifficultyIs2_3_3() {
        assertEquals(2, AnagramGame.wordCountForDifficulty(1))
        assertEquals(3, AnagramGame.wordCountForDifficulty(2))
        assertEquals(3, AnagramGame.wordCountForDifficulty(3))
    }

    @Test
    fun gameIsDeterministicWithSeed() {
        val game1 = AnagramGame(3, Random(42))
        val game2 = AnagramGame(3, Random(42))
        assertEquals(game1.currentWord, game2.currentWord)
        assertEquals(game1.shuffledLetters, game2.shuffledLetters)
    }

    @Test
    fun wordsDoNotRepeatWithinOneGame() {
        val game = AnagramGame(3, Random(7))
        val seen = mutableListOf(game.currentWord)
        while (!game.isGameWon) {
            game.typeCurrentWordCorrectly()
            game.submit()
            if (!game.isGameWon) seen += game.currentWord
        }
        assertEquals(game.totalWords, seen.size)
        assertEquals(seen.size, seen.distinct().size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun tooSmallDictionaryIsRejected() {
        AnagramGame(3, Random(1), listOf("слово"))
    }

    // ---------- набор букв ----------

    @Test
    fun pressLetterCollectsLettersByIndexIncludingDuplicates() {
        // «потоп»: буквы «п» и «о» встречаются дважды — набор идёт по индексам.
        val game = AnagramGame(1, Random(3), listOf("потоп", "топот"))
        for (i in game.shuffledLetters.indices)
            assertTrue(game.pressLetter(i))
        assertEquals(game.shuffledLetters.joinToString(""), game.typedWord)
        assertTrue(game.isWordComplete)
    }

    @Test
    fun sameIndexCannotBePressedTwiceEvenIfLetterRepeats() {
        val game = AnagramGame(1, Random(3), listOf("потоп", "топот"))
        assertTrue(game.pressLetter(0))
        assertFalse(game.pressLetter(0))
        assertEquals(1, game.typedIndices.size)
    }

    @Test
    fun pressLetterRejectsIndexOutOfRange() {
        val game = AnagramGame(1, Random(3), listOf("вода", "гора"))
        assertFalse(game.pressLetter(-1))
        assertFalse(game.pressLetter(game.shuffledLetters.size))
        assertTrue(game.typedIndices.isEmpty())
    }

    @Test
    fun pressLetterRejectedWhenWordIsComplete() {
        val game = AnagramGame(1, Random(3), listOf("вода", "гора"))
        for (i in game.shuffledLetters.indices) game.pressLetter(i)
        assertFalse(game.pressLetter(0))
    }

    @Test
    fun resetClearsTypedLettersAndAllowsPressingAgain() {
        val game = AnagramGame(1, Random(3), listOf("вода", "гора"))
        game.pressLetter(0)
        game.pressLetter(1)
        game.resetInput()
        assertTrue(game.typedIndices.isEmpty())
        assertEquals("", game.typedWord)
        assertTrue(game.pressLetter(0))
    }

    // ---------- проверка слова ----------

    @Test
    fun submitOfIncompleteWordReturnsNotCompleteAndKeepsInput() {
        val game = AnagramGame(1, Random(3), listOf("вода", "гора"))
        game.pressLetter(0)
        assertEquals(SubmitResult.NOT_COMPLETE, game.submit())
        assertEquals(1, game.typedIndices.size)
        assertEquals(0, game.wordsCompleted)
    }

    @Test
    fun submitOfWrongWordReturnsWrongAndClearsInput() {
        val game = AnagramGame(1, Random(3), listOf("книга", "школа"))
        // Порядок 0..n-1 даёт перемешанную строку, которая точно != исходному слову.
        for (i in game.shuffledLetters.indices) game.pressLetter(i)
        assertNotEquals(game.currentWord, game.typedWord)
        assertEquals(SubmitResult.WRONG, game.submit())
        assertTrue(game.typedIndices.isEmpty())
        assertEquals(0, game.wordsCompleted)
        assertFalse(game.isGameWon)
    }

    @Test
    fun submitOfCorrectWordAdvancesToNextWordWithNewShuffle() {
        val game = AnagramGame(1, Random(3), listOf("книга", "школа"))
        val firstWord = game.currentWord
        game.typeCurrentWordCorrectly()
        assertEquals(SubmitResult.WORD_DONE, game.submit())
        assertEquals(1, game.wordsCompleted)
        assertTrue(game.typedIndices.isEmpty())
        assertNotEquals(firstWord, game.currentWord)
        // Новые буквы соответствуют новому слову.
        assertEquals(game.currentWord.toList().sorted(), game.shuffledLetters.sorted())
    }

    @Test
    fun solvingAllWordsWinsTheGame() {
        val game = AnagramGame(2, Random(9))
        val results = mutableListOf<SubmitResult>()
        repeat(game.totalWords) {
            game.typeCurrentWordCorrectly()
            results += game.submit()
        }
        assertEquals(
            listOf(SubmitResult.WORD_DONE, SubmitResult.WORD_DONE, SubmitResult.GAME_WON),
            results
        )
        assertTrue(game.isGameWon)
        assertEquals(game.totalWords, game.wordsCompleted)
    }

    @Test
    fun afterVictoryInputIsRejectedAndSubmitKeepsReportingWin() {
        val game = AnagramGame(1, Random(5), listOf("вода", "гора"))
        repeat(game.totalWords) {
            game.typeCurrentWordCorrectly()
            game.submit()
        }
        assertTrue(game.isGameWon)
        assertFalse(game.pressLetter(0))
        assertEquals(SubmitResult.GAME_WON, game.submit())
    }

    /** Набирает текущее слово правильно, сопоставляя буквы слова индексам кнопок. */
    private fun AnagramGame.typeCurrentWordCorrectly() {
        val used = mutableSetOf<Int>()
        for (letter in currentWord) {
            val index = shuffledLetters.withIndex()
                .first { it.index !in used && it.value == letter }.index
            used += index
            assertTrue(pressLetter(index))
        }
        assertEquals(currentWord, typedWord)
    }
}
