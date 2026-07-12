package com.example.smartalarm.feature.games.capitals

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты чистой логики игры «Столицы»:
 * база пар, генерация вопросов и вариантов, неповтор пар,
 * детерминизм, прогресс, победа, ошибки и счёт.
 */
class CapitalsGameTest {

    // ---------- База пар ----------

    @Test
    fun everyLevelHasDeclaredMinimumOfPairs() {
        assertTrue(
            "Уровень 1 должен содержать не меньше 25 пар",
            CAPITALS.count { it.level == 1 } >= 25
        )
        assertTrue(
            "Уровень 2 должен содержать не меньше 30 пар",
            CAPITALS.count { it.level == 2 } >= 30
        )
        assertTrue(
            "Уровень 3 должен содержать не меньше 25 пар",
            CAPITALS.count { it.level == 3 } >= 25
        )
    }

    @Test
    fun allCountriesAreUnique() {
        val duplicates = CAPITALS.groupBy { it.country }.filterValues { it.size > 1 }.keys
        assertTrue("Страны повторяются: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun allCapitalsAreUnique() {
        val duplicates = CAPITALS.groupBy { it.capital }.filterValues { it.size > 1 }.keys
        assertTrue("Столицы повторяются: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun everyPairIsWellFormed() {
        for (pair in CAPITALS) {
            assertTrue("Пустая страна: $pair", pair.country.isNotBlank())
            assertTrue("Пустая столица: $pair", pair.capital.isNotBlank())
            assertNotEquals(
                "Страна и столица не должны совпадать: $pair",
                pair.country, pair.capital
            )
            assertTrue("Уровень вне 1..3: $pair", pair.level in 1..3)
        }
    }

    @Test
    fun countryNamesAndCapitalNamesDoNotOverlap() {
        val countries = CAPITALS.map { it.country }.toSet()
        val capitals = CAPITALS.map { it.capital }.toSet()
        val overlap = countries.intersect(capitals)
        assertTrue("Названия стран и столиц пересекаются: $overlap", overlap.isEmpty())
    }

    @Test
    fun everyDifficultyPoolIsLargeEnough() {
        for (difficulty in 1..3) {
            val pool = CAPITALS.count { it.level <= difficulty }
            val game = CapitalsGame(difficulty, Random(1))
            assertTrue(
                "Пул сложности $difficulty ($pool пар) должен покрывать " +
                        "${game.targetCorrect} верных ответов и 4 варианта",
                pool >= game.targetCorrect && pool >= CapitalsGame.OPTIONS_COUNT
            )
        }
    }

    // ---------- Генерация вопроса ----------

    @Test
    fun correctAnswerMatchesBasePairAndDirection() {
        for (difficulty in 1..3) {
            for (seed in 0L..49L) {
                val question = CapitalsGame(difficulty, Random(seed)).currentQuestion
                assertTrue("Пара вопроса не из базы", question.entry in CAPITALS)
                when (question.direction) {
                    QuestionDirection.COUNTRY_TO_CAPITAL -> {
                        assertEquals(question.entry.capital, question.correctAnswer)
                        assertTrue(question.text.contains(question.entry.country))
                    }

                    QuestionDirection.CAPITAL_TO_COUNTRY -> {
                        assertEquals(question.entry.country, question.correctAnswer)
                        assertTrue(question.text.contains(question.entry.capital))
                    }
                }
            }
        }
    }

    @Test
    fun optionsAreFourUniqueContainCorrectAndShareType() {
        for (difficulty in 1..3) {
            val poolCapitals = CAPITALS.filter { it.level <= difficulty }.map { it.capital }.toSet()
            val poolCountries = CAPITALS.filter { it.level <= difficulty }.map { it.country }.toSet()
            for (seed in 0L..49L) {
                val question = CapitalsGame(difficulty, Random(seed)).currentQuestion

                assertEquals(CapitalsGame.OPTIONS_COUNT, question.options.size)
                assertEquals(
                    "Варианты должны быть уникальны: ${question.options}",
                    question.options.size, question.options.distinct().size
                )
                assertTrue(
                    "Верный ответ должен быть среди вариантов",
                    question.correctAnswer in question.options
                )

                val expectedType = when (question.direction) {
                    QuestionDirection.COUNTRY_TO_CAPITAL -> poolCapitals
                    QuestionDirection.CAPITAL_TO_COUNTRY -> poolCountries
                }
                for (option in question.options)
                    assertTrue(
                        "Вариант «$option» не из пула сложности $difficulty " +
                                "или не того типа (${question.direction})",
                        option in expectedType
                    )
            }
        }
    }

    @Test
    fun questionEntryRespectsDifficultyPool() {
        for (seed in 0L..49L) {
            val game = CapitalsGame(1, Random(seed))
            assertEquals(
                "На сложности 1 должны выпадать только страны уровня 1",
                1, game.currentQuestion.entry.level
            )
        }
    }

    @Test
    fun bothDirectionsActuallyOccur() {
        val directions = (0L..99L)
            .map { CapitalsGame(1, Random(it)).currentQuestion.direction }
            .toSet()
        assertEquals(
            "Оба направления вопроса должны встречаться",
            setOf(QuestionDirection.COUNTRY_TO_CAPITAL, QuestionDirection.CAPITAL_TO_COUNTRY),
            directions
        )
    }

    // ---------- Неповтор пар ----------

    @Test
    fun entriesDoNotRepeatWithinGameOnCorrectAnswers() {
        for (difficulty in 1..3) {
            for (seed in 0L..19L) {
                val game = CapitalsGame(difficulty, Random(seed))
                val entries = mutableListOf(game.currentQuestion.entry)
                while (!game.isFinished) {
                    game.onAnswer(game.currentQuestion.correctAnswer)
                    if (!game.isFinished)
                        entries.add(game.currentQuestion.entry)
                }
                assertEquals(
                    "Пары не должны повторяться за игру (seed=$seed)",
                    entries.size, entries.distinct().size
                )
            }
        }
    }

    @Test
    fun entriesDoNotRepeatAcrossMistakesToo() {
        for (seed in 0L..19L) {
            val game = CapitalsGame(1, Random(seed))
            val entries = mutableListOf(game.currentQuestion.entry)
            // 5 ошибок + верные ответы до победы: всего вопросов меньше пула (25).
            repeat(5) {
                val wrong = game.currentQuestion.options.first {
                    it != game.currentQuestion.correctAnswer
                }
                game.onAnswer(wrong)
                entries.add(game.currentQuestion.entry)
            }
            while (!game.isFinished) {
                game.onAnswer(game.currentQuestion.correctAnswer)
                if (!game.isFinished)
                    entries.add(game.currentQuestion.entry)
            }
            assertEquals(
                "Пары не должны повторяться и после ошибок (seed=$seed)",
                entries.size, entries.distinct().size
            )
        }
    }

    @Test
    fun exhaustedPoolIsResetInsteadOfCrashing() {
        val tinyBase = CAPITALS.filter { it.level == 1 }.take(4)
        val game = CapitalsGame(1, Random(3), tinyBase)
        // 4 верных ответа нужны для победы, но сначала — 10 ошибок:
        // вопросов больше, чем пар, и пул должен переиспользоваться без падения.
        repeat(10) {
            val wrong = game.currentQuestion.options.first {
                it != game.currentQuestion.correctAnswer
            }
            assertEquals(AnswerResult.WRONG, game.onAnswer(wrong))
            assertEquals(CapitalsGame.OPTIONS_COUNT, game.currentQuestion.options.size)
        }
        assertEquals(10, game.mistakes)
        assertFalse(game.isFinished)
    }

    @Test
    fun tooSmallBaseIsRejected() {
        val tinyBase = CAPITALS.take(3)
        try {
            CapitalsGame(1, Random(1), tinyBase)
            assertTrue("Ожидалось IllegalArgumentException для базы из 3 пар", false)
        } catch (expected: IllegalArgumentException) {
        }
    }

    // ---------- Детерминизм ----------

    @Test
    fun sameSeedProducesSameGame() {
        for (difficulty in 1..3) {
            val first = CapitalsGame(difficulty, Random(42))
            val second = CapitalsGame(difficulty, Random(42))
            while (!first.isFinished) {
                assertEquals(first.currentQuestion, second.currentQuestion)
                first.onAnswer(first.currentQuestion.correctAnswer)
                second.onAnswer(second.currentQuestion.correctAnswer)
            }
            assertTrue(second.isFinished)
        }
    }

    // ---------- Прогресс, победа, ошибки ----------

    @Test
    fun targetCorrectIsFourFiveSixByDifficulty() {
        assertEquals(4, CapitalsGame(1, Random(1)).targetCorrect)
        assertEquals(5, CapitalsGame(2, Random(1)).targetCorrect)
        assertEquals(6, CapitalsGame(3, Random(1)).targetCorrect)
    }

    @Test
    fun outOfRangeDifficultyIsCoerced() {
        assertEquals(1, CapitalsGame(0, Random(1)).difficulty)
        assertEquals(3, CapitalsGame(7, Random(1)).difficulty)
    }

    @Test
    fun correctAnswerAdvancesProgressAndAsksNewQuestion() {
        val game = CapitalsGame(2, Random(7))
        val firstQuestion = game.currentQuestion

        val result = game.onAnswer(firstQuestion.correctAnswer)

        assertEquals(AnswerResult.NEXT_QUESTION, result)
        assertEquals(1, game.correctCount)
        assertEquals(0, game.mistakes)
        assertFalse(game.isFinished)
        assertNotEquals(
            "После верного ответа должен появиться вопрос о другой паре",
            firstQuestion.entry, game.currentQuestion.entry
        )
    }

    @Test
    fun wrongAnswerCountsMistakeAndAsksNewQuestion() {
        val game = CapitalsGame(1, Random(7))
        val firstQuestion = game.currentQuestion
        val wrong = firstQuestion.options.first { it != firstQuestion.correctAnswer }

        val result = game.onAnswer(wrong)

        assertEquals(AnswerResult.WRONG, result)
        assertEquals(1, game.mistakes)
        assertEquals(0, game.correctCount)
        assertFalse(game.isFinished)
        assertNotEquals(
            "После ошибки должен появиться вопрос о другой паре",
            firstQuestion.entry, game.currentQuestion.entry
        )
    }

    @Test
    fun unknownOptionIsTreatedAsMistake() {
        val game = CapitalsGame(1, Random(9))
        assertEquals(AnswerResult.WRONG, game.onAnswer("Атлантида"))
        assertEquals(1, game.mistakes)
    }

    @Test
    fun winAfterTargetCorrectAnswers() {
        for (difficulty in 1..3) {
            val game = CapitalsGame(difficulty, Random(11))
            repeat(game.targetCorrect - 1) {
                assertEquals(
                    AnswerResult.NEXT_QUESTION,
                    game.onAnswer(game.currentQuestion.correctAnswer)
                )
            }
            assertEquals(AnswerResult.WIN, game.onAnswer(game.currentQuestion.correctAnswer))
            assertTrue(game.isFinished)
            assertEquals(game.targetCorrect, game.correctCount)
        }
    }

    @Test
    fun answersAfterWinAreIgnored() {
        val game = CapitalsGame(1, Random(13))
        repeat(game.targetCorrect) { game.onAnswer(game.currentQuestion.correctAnswer) }
        assertTrue(game.isFinished)

        val questionBefore = game.currentQuestion
        assertEquals(AnswerResult.WIN, game.onAnswer("Атлантида"))
        assertEquals(0, game.mistakes)
        assertEquals(game.targetCorrect, game.correctCount)
        assertEquals(questionBefore, game.currentQuestion)
    }

    @Test
    fun mistakesAccumulateAcrossQuestions() {
        val game = CapitalsGame(1, Random(17))
        repeat(3) {
            val wrong = game.currentQuestion.options.first {
                it != game.currentQuestion.correctAnswer
            }
            game.onAnswer(wrong)
        }
        game.onAnswer(game.currentQuestion.correctAnswer)
        assertEquals(3, game.mistakes)
        assertEquals(1, game.correctCount)
    }

    // ---------- Счёт ----------

    @Test
    fun scoreWithoutMistakesMatchesCalcFormula() {
        assertEquals(600, computeCapitalsScore(0, 0, 1))
        assertEquals((600 - 100) * 3, computeCapitalsScore(0, 100, 3))
    }

    @Test
    fun eachMistakeCostsTenPoints() {
        assertEquals(
            computeCapitalsScore(0, 100, 2) - 30,
            computeCapitalsScore(3, 100, 2)
        )
    }

    @Test
    fun scoreCanBeNegativeForVeryLongGame() {
        assertTrue(computeCapitalsScore(0, 700, 1) < 0)
    }
}
