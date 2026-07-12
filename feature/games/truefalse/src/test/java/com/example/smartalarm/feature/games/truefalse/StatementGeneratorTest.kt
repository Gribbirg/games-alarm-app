package com.example.smartalarm.feature.games.truefalse

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты генератора утверждений: заявленная истинность всегда совпадает
 * с фактической (текст разбирается и пересчитывается независимо),
 * обе метки встречаются, числа неотрицательны, оба вида утверждений есть.
 */
class StatementGeneratorTest {

    /**
     * Независимо разбирает текст утверждения и вычисляет его истинность.
     * Поддерживаемые формы (5 токенов через пробел):
     * «a op b = r», «a op b cmp n», «n cmp a op b»,
     * где op — «+», «-», «×», cmp — «>», «<».
     */
    private fun evaluate(text: String): Boolean {
        val t = text.split(" ")
        assertEquals("Ожидаются 5 токенов: «$text»", 5, t.size)
        fun apply(a: Int, op: String, b: Int): Int = when (op) {
            "+" -> a + b
            "-" -> a - b
            "×" -> a * b
            else -> throw IllegalArgumentException("Неизвестная операция «$op» в «$text»")
        }
        return when {
            t[3] == "=" -> apply(t[0].toInt(), t[1], t[2].toInt()) == t[4].toInt()
            t[3] == ">" -> apply(t[0].toInt(), t[1], t[2].toInt()) > t[4].toInt()
            t[3] == "<" -> apply(t[0].toInt(), t[1], t[2].toInt()) < t[4].toInt()
            t[1] == ">" -> t[0].toInt() > apply(t[2].toInt(), t[3], t[4].toInt())
            t[1] == "<" -> t[0].toInt() < apply(t[2].toInt(), t[3], t[4].toInt())
            else -> throw IllegalArgumentException("Не удалось разобрать «$text»")
        }
    }

    @Test
    fun declaredTruthMatchesActualTruth() {
        for (difficulty in 1..3) {
            val generator = StatementGenerator(difficulty, Random(100 + difficulty))
            var trueCount = 0
            repeat(SAMPLE_SIZE) {
                val statement = generator.generate()
                assertEquals(
                    "Метка не совпадает с фактом: «${statement.text}» (сложность $difficulty)",
                    evaluate(statement.text),
                    statement.isTrue
                )
                if (statement.isTrue) trueCount++
            }
            // Доля истинных ~50%: при 3000 испытаниях допускаем широкий коридор.
            assertTrue(
                "Истинных слишком мало/много: $trueCount из $SAMPLE_SIZE (сложность $difficulty)",
                trueCount in (SAMPLE_SIZE / 3)..(SAMPLE_SIZE * 2 / 3)
            )
        }
    }

    @Test
    fun forcedTrueStatementsAreAlwaysTrue() {
        for (difficulty in 1..3) {
            val generator = StatementGenerator(difficulty, Random(200 + difficulty))
            repeat(SAMPLE_SIZE) {
                val statement = generator.generate(shouldBeTrue = true)
                assertTrue("Ожидалось истинное: «${statement.text}»", statement.isTrue)
                assertTrue("Фактически ложное: «${statement.text}»", evaluate(statement.text))
            }
        }
    }

    @Test
    fun forcedFalseStatementsAreAlwaysFalse() {
        for (difficulty in 1..3) {
            val generator = StatementGenerator(difficulty, Random(300 + difficulty))
            repeat(SAMPLE_SIZE) {
                val statement = generator.generate(shouldBeTrue = false)
                assertTrue("Ожидалось ложное: «${statement.text}»", !statement.isTrue)
                assertTrue("Фактически истинное: «${statement.text}»", !evaluate(statement.text))
            }
        }
    }

    @Test
    fun allNumbersAreNonNegative() {
        for (difficulty in 1..3) {
            val generator = StatementGenerator(difficulty, Random(400 + difficulty))
            repeat(SAMPLE_SIZE) {
                val statement = generator.generate()
                statement.text.split(" ")
                    .mapNotNull { it.toIntOrNull() }
                    .forEach { number ->
                        assertTrue(
                            "Отрицательное число в «${statement.text}»",
                            number >= 0
                        )
                    }
            }
        }
    }

    @Test
    fun bothStatementKindsAppear() {
        for (difficulty in 1..3) {
            val generator = StatementGenerator(difficulty, Random(500 + difficulty))
            var equalities = 0
            var comparisons = 0
            repeat(SAMPLE_SIZE) {
                val text = generator.generate().text
                if ("=" in text) equalities++
                if (">" in text || "<" in text) comparisons++
            }
            assertTrue("Нет равенств (сложность $difficulty)", equalities > 0)
            assertTrue("Нет сравнений (сложность $difficulty)", comparisons > 0)
            assertEquals(SAMPLE_SIZE, equalities + comparisons)
        }
    }

    @Test
    fun invalidDifficultyIsCoercedToBounds() {
        // Не должно падать и должно давать корректные утверждения.
        for (difficulty in intArrayOf(-1, 0, 4, 100)) {
            val generator = StatementGenerator(difficulty, Random(600))
            repeat(100) {
                val statement = generator.generate()
                assertEquals(evaluate(statement.text), statement.isTrue)
            }
        }
    }

    private companion object {
        const val SAMPLE_SIZE = 3000
    }
}
