package com.example.smartalarm.feature.games.equation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты генератора уравнений: корректность равенств, варианты ответов,
 * деление нацело, отсутствие отрицательных чисел и детерминизм по seed.
 */
class EquationGeneratorTest {

    /**
     * Независимый от генератора вычислитель: подставляет [substitution] вместо `?`
     * в текст задания и проверяет, что равенство выполняется. Умножение и деление
     * имеют приоритет над сложением и вычитанием; деление с остатком или на ноль
     * делает равенство невыполненным.
     */
    private fun equationHolds(task: EquationTask, substitution: String): Boolean {
        val sides = task.text.split(" = ")
        if (sides.size != 2) return false
        val (left, right) = sides
        val expected = (if (right == "?") substitution else right).toIntOrNull() ?: return false
        val tokens = left.split(" ").map { if (it == "?") substitution else it }
        return evaluate(tokens) == expected
    }

    /** Вычисляет выражение из чередующихся чисел и операторов; null — если оно некорректно. */
    private fun evaluate(tokens: List<String>): Int? {
        val numbers = mutableListOf<Int>()
        val ops = mutableListOf<Operator>()
        tokens.forEachIndexed { index, token ->
            if (index % 2 == 0)
                numbers.add(token.toIntOrNull() ?: return null)
            else
                ops.add(Operator.fromSymbol(token) ?: return null)
        }
        if (numbers.size != ops.size + 1) return null

        var i = 0
        while (i < ops.size) {
            if (ops[i] == Operator.MULTIPLY || ops[i] == Operator.DIVIDE) {
                val partial = ops[i].apply(numbers[i], numbers[i + 1]) ?: return null
                numbers[i] = partial
                numbers.removeAt(i + 1)
                ops.removeAt(i)
            } else {
                i++
            }
        }

        var result = numbers[0]
        for (j in ops.indices)
            result = ops[j].apply(result, numbers[j + 1]) ?: return null
        return result
    }

    private fun manyTasks(difficulty: Int): List<EquationTask> {
        val tasks = mutableListOf<EquationTask>()
        for (seed in 0..49) {
            val generator = EquationGenerator(Random(seed))
            repeat(5) { tasks.add(generator.generate(difficulty)) }
        }
        return tasks
    }

    @Test
    fun correctAnswerSatisfiesEquation_allDifficulties() {
        for (difficulty in 1..3)
            for (task in manyTasks(difficulty))
                assertTrue(
                    "Верный ответ не сходится: '${task.text}' с '${task.answer}'",
                    equationHolds(task, task.answer)
                )
    }

    @Test
    fun optionsAreFourUniqueAndContainAnswer() {
        for (difficulty in 1..3)
            for (task in manyTasks(difficulty)) {
                assertEquals("Должно быть 4 варианта: $task", 4, task.options.size)
                assertEquals("Варианты дублируются: $task", 4, task.options.toSet().size)
                assertTrue("Нет верного ответа в вариантах: $task", task.answer in task.options)
            }
    }

    @Test
    fun distractorsDoNotSatisfyEquation() {
        for (difficulty in 1..3)
            for (task in manyTasks(difficulty))
                for (option in task.options.filter { it != task.answer })
                    assertFalse(
                        "Дистрактор тоже решает уравнение: '${task.text}' с '$option'",
                        equationHolds(task, option)
                    )
    }

    @Test
    fun missingOperatorTaskOffersAllFourOperators() {
        for (task in manyTasks(2)) {
            assertEquals(
                "Варианты сложности 2 — все четыре оператора: $task",
                Operator.entries.map { it.symbol }.toSet(),
                task.options.toSet()
            )
            assertTrue(
                "Пропущен должен быть оператор: $task",
                Operator.fromSymbol(task.answer) != null
            )
        }
    }

    @Test
    fun divisionTasksAreExact() {
        for (difficulty in 1..2)
            for (task in manyTasks(difficulty).filter { it.text.contains(Operator.DIVIDE.symbol) })
                assertTrue(
                    "Деление должно быть нацело: '${task.text}' с '${task.answer}'",
                    equationHolds(task, task.answer)
                )
    }

    @Test
    fun noNegativeNumbersInTextOrOptions() {
        val negative = Regex("-\\d")
        for (difficulty in 1..3)
            for (task in manyTasks(difficulty)) {
                assertFalse(
                    "Отрицательное число в тексте: '${task.text}'",
                    negative.containsMatchIn(task.text)
                )
                for (option in task.options)
                    assertFalse(
                        "Отрицательный вариант ответа: $task",
                        negative.containsMatchIn(option)
                    )
            }
    }

    @Test
    fun sameSeedGivesSameTasks() {
        fun tasksFor(seed: Long): List<EquationTask> {
            val generator = EquationGenerator(Random(seed))
            return (1..3).flatMap { difficulty -> List(10) { generator.generate(difficulty) } }
        }
        assertEquals(tasksFor(42), tasksFor(42))
        assertEquals(tasksFor(7), tasksFor(7))
    }
}
