package com.example.smartalarm.feature.games.weekday

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты арифметики дней недели ([Weekday.shifted]).
 */
class WeekdayShiftTest {

    @Test
    fun `среда плюс 9 дней это пятница`() {
        assertEquals(Weekday.FRIDAY, Weekday.WEDNESDAY.shifted(9))
    }

    @Test
    fun `понедельник минус 4 дня это четверг`() {
        assertEquals(Weekday.THURSDAY, Weekday.MONDAY.shifted(-4))
    }

    @Test
    fun `смещение на целое число недель не меняет день`() {
        for (day in Weekday.entries) {
            for (k in intArrayOf(-21, -14, -7, 0, 7, 14, 70)) {
                assertEquals(day, day.shifted(k))
            }
        }
    }

    @Test
    fun `большие положительные смещения`() {
        // 100 = 14 * 7 + 2
        assertEquals(Weekday.WEDNESDAY, Weekday.MONDAY.shifted(100))
        assertEquals(Weekday.SUNDAY, Weekday.FRIDAY.shifted(100))
    }

    @Test
    fun `большие отрицательные смещения`() {
        // -100 = -14 * 7 - 2
        assertEquals(Weekday.SATURDAY, Weekday.MONDAY.shifted(-100))
        assertEquals(Weekday.WEDNESDAY, Weekday.FRIDAY.shifted(-100))
    }

    @Test
    fun `переход через границы недели`() {
        assertEquals(Weekday.MONDAY, Weekday.SUNDAY.shifted(1))
        assertEquals(Weekday.SUNDAY, Weekday.MONDAY.shifted(-1))
    }

    @Test
    fun `смещения вперёд и назад согласованы`() {
        for (day in Weekday.entries) {
            for (offset in -100..100) {
                assertEquals(day, day.shifted(offset).shifted(-offset))
            }
        }
    }
}

/**
 * Тесты текстов вопроса: склонение числительных и формулировки.
 */
class WeekdayTextTest {

    @Test
    fun `склонение слова день`() {
        assertEquals("день", WeekdayQuestionFactory.daysWord(1))
        assertEquals("дня", WeekdayQuestionFactory.daysWord(2))
        assertEquals("дня", WeekdayQuestionFactory.daysWord(4))
        assertEquals("дней", WeekdayQuestionFactory.daysWord(5))
        assertEquals("дней", WeekdayQuestionFactory.daysWord(7))
        assertEquals("дней", WeekdayQuestionFactory.daysWord(11))
        assertEquals("дней", WeekdayQuestionFactory.daysWord(14))
        assertEquals("день", WeekdayQuestionFactory.daysWord(21))
        assertEquals("дня", WeekdayQuestionFactory.daysWord(22))
        assertEquals("дней", WeekdayQuestionFactory.daysWord(30))
        assertEquals("дней", WeekdayQuestionFactory.daysWord(100))
    }

    @Test
    fun `склонение слова неделя`() {
        assertEquals("неделю", WeekdayQuestionFactory.weeksWord(1))
        assertEquals("недели", WeekdayQuestionFactory.weeksWord(2))
        assertEquals("недели", WeekdayQuestionFactory.weeksWord(4))
        assertEquals("недель", WeekdayQuestionFactory.weeksWord(5))
        assertEquals("недель", WeekdayQuestionFactory.weeksWord(11))
        assertEquals("недель", WeekdayQuestionFactory.weeksWord(14))
    }

    @Test
    fun `текст величины смещения в днях`() {
        assertEquals("9 дней", WeekdayQuestionFactory.spanText(9, asWeeks = false))
        assertEquals("1 день", WeekdayQuestionFactory.spanText(1, asWeeks = false))
        assertEquals("100 дней", WeekdayQuestionFactory.spanText(100, asWeeks = false))
    }

    @Test
    fun `текст величины смещения в неделях и днях`() {
        assertEquals("3 недели и 4 дня", WeekdayQuestionFactory.spanText(25, asWeeks = true))
        assertEquals("3 недели", WeekdayQuestionFactory.spanText(21, asWeeks = true))
        assertEquals("14 недель и 2 дня", WeekdayQuestionFactory.spanText(100, asWeeks = true))
        // Меньше недели — недельная форма не используется
        assertEquals("5 дней", WeekdayQuestionFactory.spanText(5, asWeeks = true))
    }

    @Test
    fun `текст вопроса про будущее`() {
        assertEquals(
            "Сегодня — среда. Какой день недели будет через 9 дней?",
            WeekdayQuestionFactory.questionText(Weekday.WEDNESDAY, 9, asWeeks = false)
        )
    }

    @Test
    fun `текст вопроса про прошлое`() {
        assertEquals(
            "Сегодня — понедельник. Какой день недели был 4 дня назад?",
            WeekdayQuestionFactory.questionText(Weekday.MONDAY, -4, asWeeks = false)
        )
    }

    @Test
    fun `текст вопроса с неделями`() {
        assertEquals(
            "Сегодня — пятница. Какой день недели будет через 3 недели и 4 дня?",
            WeekdayQuestionFactory.questionText(Weekday.FRIDAY, 25, asWeeks = true)
        )
        assertEquals(
            "Сегодня — пятница. Какой день недели был 3 недели назад?",
            WeekdayQuestionFactory.questionText(Weekday.FRIDAY, -21, asWeeks = true)
        )
    }
}

/**
 * Тесты генератора вопросов [WeekdayQuestionFactory.generate].
 */
class WeekdayQuestionFactoryTest {

    @Test
    fun `диапазоны смещений по сложностям`() {
        assertEquals(1..7, WeekdayQuestionFactory.offsetRangeFor(1))
        assertEquals(5..30, WeekdayQuestionFactory.offsetRangeFor(2))
        assertEquals(20..100, WeekdayQuestionFactory.offsetRangeFor(3))
        // Некорректная сложность трактуется как лёгкая
        assertEquals(1..7, WeekdayQuestionFactory.offsetRangeFor(0))
        assertEquals(1..7, WeekdayQuestionFactory.offsetRangeFor(99))
    }

    @Test
    fun `модуль смещения попадает в диапазон сложности`() {
        for (difficulty in 1..3) {
            val range = WeekdayQuestionFactory.offsetRangeFor(difficulty)
            for (seed in 0..199) {
                val question = WeekdayQuestionFactory.generate(difficulty, Random(seed))
                val magnitude = if (question.offset > 0) question.offset else -question.offset
                assertNotEquals(0, question.offset)
                assertTrue(
                    "смещение $magnitude вне $range (сложность $difficulty)",
                    magnitude in range
                )
            }
        }
    }

    @Test
    fun `генерируются оба направления смещения`() {
        val random = Random(1)
        val signs = (1..100)
            .map { WeekdayQuestionFactory.generate(2, random).offset > 0 }
            .toSet()
        assertEquals(setOf(true, false), signs)
    }

    @Test
    fun `верный ответ равен стартовому дню со смещением`() {
        for (difficulty in 1..3) {
            for (seed in 0..199) {
                val question = WeekdayQuestionFactory.generate(difficulty, Random(seed))
                assertEquals(question.startDay.shifted(question.offset), question.correctAnswer)
            }
        }
    }

    @Test
    fun `ответ согласован с текстом вопроса`() {
        for (difficulty in 1..3) {
            for (seed in 0..199) {
                val question = WeekdayQuestionFactory.generate(difficulty, Random(seed))
                assertEquals(
                    "текст: ${question.text}",
                    recomputeAnswerFromText(question.text),
                    question.correctAnswer
                )
            }
        }
    }

    @Test
    fun `четыре уникальных варианта с верным ответом`() {
        for (difficulty in 1..3) {
            for (seed in 0..199) {
                val question = WeekdayQuestionFactory.generate(difficulty, Random(seed))
                assertEquals(WeekdayQuestionFactory.OPTIONS_COUNT, question.options.size)
                assertEquals(
                    WeekdayQuestionFactory.OPTIONS_COUNT,
                    question.options.toSet().size
                )
                assertTrue(question.options.contains(question.correctAnswer))
            }
        }
    }

    @Test
    fun `верный ответ встречается на разных позициях`() {
        val random = Random(3)
        val positions = (1..200)
            .map {
                val question = WeekdayQuestionFactory.generate(1, random)
                question.options.indexOf(question.correctAnswer)
            }
            .toSet()
        assertEquals(setOf(0, 1, 2, 3), positions)
    }

    @Test
    fun `детерминизм при одинаковом seed`() {
        for (difficulty in 1..3) {
            val first = Random(42).let { r -> List(50) { WeekdayQuestionFactory.generate(difficulty, r) } }
            val second = Random(42).let { r -> List(50) { WeekdayQuestionFactory.generate(difficulty, r) } }
            assertEquals(first, second)
        }
    }

    @Test
    fun `недельная формулировка только на третьей сложности`() {
        for (difficulty in 1..2) {
            val random = Random(5)
            repeat(200) {
                val question = WeekdayQuestionFactory.generate(difficulty, random)
                assertFalse(question.text.contains("недел"))
            }
        }
        val random = Random(5)
        val hasWeeks = (1..200).any {
            WeekdayQuestionFactory.generate(3, random).text.contains("недел")
        }
        assertTrue(hasWeeks)
    }

    /**
     * Независимо пересчитывает верный ответ, разбирая текст вопроса:
     * стартовый день, числа и направление («через …» / «… назад»).
     */
    private fun recomputeAnswerFromText(text: String): Weekday {
        val startDay = Weekday.entries.first {
            text.startsWith("Сегодня — ${it.displayName}.")
        }
        val numbers = Regex("\\d+").findAll(text).map { it.value.toInt() }.toList()
        val magnitude = if (text.contains("недел")) {
            if (numbers.size == 2) numbers[0] * 7 + numbers[1] else numbers[0] * 7
        } else {
            assertEquals(1, numbers.size)
            numbers[0]
        }
        val backwards = text.contains("назад")
        assertTrue(text.contains(if (backwards) "был" else "будет через"))
        return startDay.shifted(if (backwards) -magnitude else magnitude)
    }
}
