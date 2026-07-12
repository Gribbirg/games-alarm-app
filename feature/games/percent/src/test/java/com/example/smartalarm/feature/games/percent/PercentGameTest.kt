package com.example.smartalarm.feature.games.percent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Тесты чистой логики [PercentGame]. */
class PercentGameTest {

    /** Индекс любого неверного варианта текущей задачи. */
    private fun wrongIndex(game: PercentGame): Int =
        game.currentTask.options.indices.first { it != game.currentTask.correctIndex }

    /**
     * Пересчитывает верный ответ задачи из её параметров (тип, процент,
     * данное число), попутно проверяя целочисленность деления на 100.
     */
    private fun recomputeAnswer(task: PercentTask): Int = when (task.type) {
        PercentTaskType.PERCENT_OF -> {
            assertEquals(
                "P% от X не целые: $task", 0, task.given * task.percent % 100
            )
            task.given * task.percent / 100
        }

        PercentTaskType.INCREASE, PercentTaskType.PRICE_INCREASE -> {
            assertEquals(
                "прибавка не целая: $task", 0, task.given * task.percent % 100
            )
            task.given + task.given * task.percent / 100
        }

        PercentTaskType.DECREASE, PercentTaskType.PRICE_DECREASE -> {
            assertEquals(
                "скидка не целая: $task", 0, task.given * task.percent % 100
            )
            task.given - task.given * task.percent / 100
        }

        PercentTaskType.FIND_NUMBER -> {
            // X — ответ; проверяем согласованность: P% от X равно данному Y.
            assertEquals(
                "P% от искомого числа не целые: $task",
                0, task.answer * task.percent % 100
            )
            assertEquals(
                "Y не равно P% от искомого числа: $task",
                task.given, task.answer * task.percent / 100
            )
            // и обратный пересчёт: X = Y·100/P — целое.
            assertEquals("X = Y·100/P не целое: $task", 0, task.given * 100 % task.percent)
            task.given * 100 / task.percent
        }
    }

    @Test
    fun `difficulty sets target correct answers`() {
        assertEquals(3, PercentGame(1, Random(1)).targetCorrect)
        assertEquals(4, PercentGame(2, Random(1)).targetCorrect)
        assertEquals(5, PercentGame(3, Random(1)).targetCorrect)
    }

    @Test
    fun `difficulty 1 uses only simple percents of round numbers`() {
        val game = PercentGame(1, Random(2))
        repeat(200) {
            val task = game.currentTask
            assertEquals(PercentTaskType.PERCENT_OF, task.type)
            assertTrue(
                "не простой процент: ${task.percent}",
                task.percent in setOf(10, 25, 50, 100)
            )
            // «Круглость» данного числа по построению генератора.
            val roundness = when (task.percent) {
                10, 100 -> 10
                25 -> 20
                else -> 2
            }
            assertEquals("не круглое число: $task", 0, task.given % roundness)
            game.answer(wrongIndex(game)) // форсируем новую задачу
        }
    }

    @Test
    fun `difficulty 2 uses percent-of and change tasks with percents multiple of 5`() {
        val game = PercentGame(2, Random(3))
        val allowed = setOf(
            PercentTaskType.PERCENT_OF,
            PercentTaskType.INCREASE,
            PercentTaskType.DECREASE
        )
        val seen = mutableSetOf<PercentTaskType>()
        repeat(300) {
            val task = game.currentTask
            assertTrue("недопустимый тип: ${task.type}", task.type in allowed)
            assertTrue("процент вне 5..95: ${task.percent}", task.percent in 5..95)
            assertEquals("процент не кратен 5: ${task.percent}", 0, task.percent % 5)
            seen += task.type
            game.answer(wrongIndex(game))
        }
        assertEquals("не все типы встретились", allowed, seen)
    }

    @Test
    fun `difficulty 3 uses price and inverse tasks`() {
        val game = PercentGame(3, Random(4))
        val allowed = setOf(
            PercentTaskType.PRICE_INCREASE,
            PercentTaskType.PRICE_DECREASE,
            PercentTaskType.FIND_NUMBER
        )
        val seen = mutableSetOf<PercentTaskType>()
        repeat(300) {
            val task = game.currentTask
            assertTrue("недопустимый тип: ${task.type}", task.type in allowed)
            assertTrue("процент вне 5..95: ${task.percent}", task.percent in 5..95)
            assertEquals("процент не кратен 5: ${task.percent}", 0, task.percent % 5)
            seen += task.type
            game.answer(wrongIndex(game))
        }
        assertEquals("не все типы встретились", allowed, seen)
    }

    @Test
    fun `answers are integer, positive and arithmetically correct at every difficulty`() {
        for (difficulty in 1..3) {
            val game = PercentGame(difficulty, Random(difficulty * 10))
            repeat(200) {
                val task = game.currentTask
                assertTrue("данное число не положительно: $task", task.given > 0)
                assertTrue("ответ не положителен: $task", task.answer > 0)
                assertEquals(
                    "ответ арифметически неверен: $task",
                    recomputeAnswer(task), task.answer
                )
                game.answer(wrongIndex(game))
            }
        }
    }

    @Test
    fun `task text matches its parameters`() {
        for (difficulty in 1..3) {
            val game = PercentGame(difficulty, Random(difficulty * 100))
            repeat(200) {
                val task = game.currentTask
                val expected = when (task.type) {
                    PercentTaskType.PERCENT_OF ->
                        "Сколько будет ${task.percent}% от ${task.given}?"

                    PercentTaskType.INCREASE ->
                        "Число ${task.given} увеличили на ${task.percent}%. " +
                                "Какое число получилось?"

                    PercentTaskType.DECREASE ->
                        "Число ${task.given} уменьшили на ${task.percent}%. " +
                                "Какое число получилось?"

                    PercentTaskType.PRICE_INCREASE ->
                        "Товар стоил ${task.given} ₽, подорожал на ${task.percent}%. " +
                                "Сколько он стоит теперь?"

                    PercentTaskType.PRICE_DECREASE ->
                        "Товар стоил ${task.given} ₽, подешевел на ${task.percent}%. " +
                                "Сколько он стоит теперь?"

                    PercentTaskType.FIND_NUMBER ->
                        "${task.percent}% числа равно ${task.given}. Найдите это число."
                }
                assertEquals(expected, task.text)
                game.answer(wrongIndex(game))
            }
        }
    }

    @Test
    fun `options are four, unique, positive and contain the answer`() {
        for (difficulty in 1..3) {
            val game = PercentGame(difficulty, Random(difficulty * 1000))
            repeat(200) {
                val task = game.currentTask
                assertEquals(4, task.options.size)
                assertEquals("варианты не уникальны: $task", 4, task.options.toSet().size)
                task.options.forEach { option ->
                    assertTrue("вариант не положителен: $task", option > 0)
                }
                assertTrue(task.correctIndex in 0..3)
                assertEquals(task.answer, task.options[task.correctIndex])
                game.answer(wrongIndex(game))
            }
        }
    }

    @Test
    fun `consecutive tasks are different`() {
        val game = PercentGame(1, Random(5))
        repeat(100) {
            val previous = game.currentTask.text
            game.answer(wrongIndex(game))
            assertNotEquals(previous, game.currentTask.text)
        }
    }

    @Test
    fun `same seed produces the same game`() {
        val first = PercentGame(2, Random(42))
        val second = PercentGame(2, Random(42))
        repeat(50) {
            assertEquals(first.currentTask, second.currentTask)
            // отвечаем неверно, чтобы задачи продолжали генерироваться
            val result = first.answer(wrongIndex(first))
            assertEquals(result, second.answer(wrongIndex(second)))
        }
    }

    @Test
    fun `correct answers advance progress and reach win`() {
        for (difficulty in 1..3) {
            val game = PercentGame(difficulty, Random(6))
            repeat(game.targetCorrect - 1) { step ->
                assertEquals(
                    AnswerResult.CORRECT,
                    game.answer(game.currentTask.correctIndex)
                )
                assertEquals(step + 1, game.correctCount)
            }
            assertEquals(AnswerResult.WIN, game.answer(game.currentTask.correctIndex))
            assertEquals(game.targetCorrect, game.correctCount)
        }
    }

    @Test
    fun `wrong answer keeps progress, counts mistake and changes task`() {
        val game = PercentGame(2, Random(9))
        assertEquals(AnswerResult.CORRECT, game.answer(game.currentTask.correctIndex))
        val progressBefore = game.correctCount
        val taskBefore = game.currentTask

        assertEquals(AnswerResult.WRONG, game.answer(wrongIndex(game)))
        assertEquals(progressBefore, game.correctCount)
        assertEquals(1, game.mistakeCount)
        assertNotEquals(taskBefore, game.currentTask)

        // после ошибки победа по-прежнему достижима
        var result: AnswerResult
        do {
            result = game.answer(game.currentTask.correctIndex)
        } while (result == AnswerResult.CORRECT)
        assertEquals(AnswerResult.WIN, result)
        assertEquals(game.targetCorrect, game.correctCount)
    }

    @Test
    fun `answer rejects invalid option index`() {
        val game = PercentGame(1, Random(11))
        assertThrows(IllegalArgumentException::class.java) { game.answer(-1) }
        assertThrows(IllegalArgumentException::class.java) { game.answer(4) }
    }
}
