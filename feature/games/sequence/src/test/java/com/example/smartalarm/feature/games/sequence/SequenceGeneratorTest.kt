package com.example.smartalarm.feature.games.sequence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты чистого генератора рядов [SequenceGenerator].
 */
class SequenceGeneratorTest {

    private val manySeeds = 0..199

    @Test
    fun typesForDifficultyOne_onlyArithmetic() {
        assertEquals(listOf(SequenceType.ARITHMETIC), SequenceGenerator.typesFor(1))
    }

    @Test
    fun typesForDifficultyTwo_addsGeometricAndSquares() {
        val types = SequenceGenerator.typesFor(2)
        assertEquals(
            setOf(SequenceType.ARITHMETIC, SequenceType.GEOMETRIC, SequenceType.SQUARES),
            types.toSet()
        )
    }

    @Test
    fun typesForDifficultyThree_allTypes() {
        assertEquals(SequenceType.entries.toSet(), SequenceGenerator.typesFor(3).toSet())
    }

    @Test
    fun typesFor_coercesOutOfRangeDifficulty() {
        assertEquals(SequenceGenerator.typesFor(1), SequenceGenerator.typesFor(0))
        assertEquals(SequenceGenerator.typesFor(1), SequenceGenerator.typesFor(-5))
        assertEquals(SequenceGenerator.typesFor(3), SequenceGenerator.typesFor(99))
    }

    @Test
    fun roundsFor_threeFourFive() {
        assertEquals(3, SequenceGenerator.roundsFor(1))
        assertEquals(4, SequenceGenerator.roundsFor(2))
        assertEquals(5, SequenceGenerator.roundsFor(3))
        assertEquals(3, SequenceGenerator.roundsFor(0))
        assertEquals(5, SequenceGenerator.roundsFor(10))
    }

    @Test
    fun arithmetic_followsConstantStep() {
        for (seed in manySeeds) {
            val task = SequenceGenerator.generate(SequenceType.ARITHMETIC, Random(seed))
            val terms = task.terms
            assertTrue("длина 4-5", terms.size in 4..5)
            val step = terms[1] - terms[0]
            assertTrue("шаг по модулю 2..9", kotlin.math.abs(step) in 2..9)
            for (i in 1 until terms.size)
                assertEquals("постоянный шаг", step, terms[i] - terms[i - 1])
            assertEquals("ответ продолжает прогрессию", terms.last() + step, task.answer)
            assertTrue("члены неотрицательны", terms.all { it >= 0 } && task.answer >= 0)
        }
    }

    @Test
    fun geometric_followsConstantRatio() {
        for (seed in manySeeds) {
            val task = SequenceGenerator.generate(SequenceType.GEOMETRIC, Random(seed))
            val terms = task.terms
            assertEquals(4, terms.size)
            val ratio = terms[1] / terms[0]
            assertTrue("знаменатель 2..3", ratio in 2..3)
            for (i in 1 until terms.size)
                assertEquals("постоянное отношение", terms[i - 1] * ratio, terms[i])
            assertEquals("ответ продолжает прогрессию", terms.last() * ratio, task.answer)
            assertTrue("ответ ограничен", task.answer <= 405)
        }
    }

    @Test
    fun squares_areConsecutiveSquares() {
        for (seed in manySeeds) {
            val task = SequenceGenerator.generate(SequenceType.SQUARES, Random(seed))
            val terms = task.terms
            assertTrue("длина 4-5", terms.size in 4..5)
            val firstRoot = kotlin.math.sqrt(terms[0].toDouble()).toInt()
            assertEquals("первый член — точный квадрат", firstRoot * firstRoot, terms[0])
            terms.forEachIndexed { i, term ->
                val root = firstRoot + i
                assertEquals("последовательные квадраты", root * root, term)
            }
            val nextRoot = firstRoot + terms.size
            assertEquals("ответ — следующий квадрат", nextRoot * nextRoot, task.answer)
        }
    }

    @Test
    fun alternating_alternatesPlusAndMinusSteps() {
        for (seed in manySeeds) {
            val task = SequenceGenerator.generate(SequenceType.ALTERNATING, Random(seed))
            val terms = task.terms
            assertEquals(5, terms.size)
            val plus = terms[1] - terms[0]
            val minus = terms[1] - terms[2]
            assertTrue("плюс-шаг 4..9", plus in 4..9)
            assertTrue("минус-шаг меньше плюс-шага", minus in 1 until plus)
            for (i in 1 until terms.size) {
                val expected = if (i % 2 == 1) plus else -minus
                assertEquals("чередование шагов", expected, terms[i] - terms[i - 1])
            }
            assertEquals("ответ — следующий шаг чередования", terms.last() + plus, task.answer)
            assertTrue("все члены положительны", terms.all { it >= 1 })
        }
    }

    @Test
    fun fibonacciLike_eachTermIsSumOfPreviousTwo() {
        for (seed in manySeeds) {
            val task = SequenceGenerator.generate(SequenceType.FIBONACCI_LIKE, Random(seed))
            val terms = task.terms
            assertEquals(5, terms.size)
            assertTrue("старт положительный", terms[0] >= 1 && terms[1] >= terms[0])
            for (i in 2 until terms.size)
                assertEquals("сумма двух предыдущих", terms[i - 1] + terms[i - 2], terms[i])
            assertEquals(
                "ответ — сумма двух последних",
                terms[terms.size - 1] + terms[terms.size - 2],
                task.answer
            )
        }
    }

    @Test
    fun options_areUniqueAndContainAnswerOnce() {
        for (seed in manySeeds) {
            for (type in SequenceType.entries) {
                val task = SequenceGenerator.generate(type, Random(seed))
                assertEquals("ровно 4 варианта", 4, task.options.size)
                assertEquals("варианты уникальны", 4, task.options.toSet().size)
                assertEquals(
                    "верный ответ встречается ровно один раз",
                    1,
                    task.options.count { it == task.answer }
                )
                assertTrue("проверка ответа согласована", task.isCorrect(task.answer))
                task.options.filter { it != task.answer }.forEach {
                    assertFalse("дистрактор не засчитывается", task.isCorrect(it))
                }
            }
        }
    }

    @Test
    fun generateByDifficulty_usesOnlyAllowedTypes() {
        for (difficulty in 1..3) {
            val allowed = SequenceGenerator.typesFor(difficulty).toSet()
            for (seed in manySeeds) {
                val task = SequenceGenerator.generate(difficulty, Random(seed))
                assertTrue("тип разрешён на сложности $difficulty", task.type in allowed)
            }
        }
    }

    @Test
    fun generate_isDeterministicWithSameSeed() {
        for (seed in 0..49) {
            val first = SequenceGenerator.generate(3, Random(seed))
            val second = SequenceGenerator.generate(3, Random(seed))
            assertEquals(first, second)
        }
    }

    @Test
    fun questionText_endsWithQuestionMark() {
        val task = SequenceGenerator.generate(SequenceType.ARITHMETIC, Random(1))
        assertEquals(task.terms.joinToString(", ") + ", ?", task.questionText)
    }
}
