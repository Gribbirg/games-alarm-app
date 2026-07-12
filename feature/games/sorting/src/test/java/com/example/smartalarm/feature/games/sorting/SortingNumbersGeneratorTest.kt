package com.example.smartalarm.feature.games.sorting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты генератора чисел [SortingNumbersGenerator]: количество, уникальность,
 * диапазоны по сложностям и детерминизм при фиксированном seed.
 */
class SortingNumbersGeneratorTest {

    private val seeds = (0L until 100L).toList()

    @Test
    fun `difficulty 1 - six unique numbers in 1 to 50`() {
        for (seed in seeds) {
            val numbers = SortingNumbersGenerator.generate(1, Random(seed))
            assertEquals(6, numbers.size)
            assertEquals(numbers.size, numbers.distinct().size)
            assertTrue(numbers.all { it in 1..50 })
        }
    }

    @Test
    fun `difficulty 2 - nine unique numbers in 1 to 199`() {
        for (seed in seeds) {
            val numbers = SortingNumbersGenerator.generate(2, Random(seed))
            assertEquals(9, numbers.size)
            assertEquals(numbers.size, numbers.distinct().size)
            assertTrue(numbers.all { it in 1..199 })
        }
    }

    @Test
    fun `difficulty 3 - twelve unique numbers in -99 to 199 without zero`() {
        for (seed in seeds) {
            val numbers = SortingNumbersGenerator.generate(3, Random(seed))
            assertEquals(12, numbers.size)
            assertEquals(numbers.size, numbers.distinct().size)
            assertTrue(numbers.all { it in -99..199 })
            assertTrue(numbers.none { it == 0 })
        }
    }

    @Test
    fun `difficulty 3 - exactly three negative numbers`() {
        for (seed in seeds) {
            val numbers = SortingNumbersGenerator.generate(3, Random(seed))
            assertEquals(3, numbers.count { it < 0 })
        }
    }

    @Test
    fun `unknown difficulty falls back to difficulty 1 parameters`() {
        for (difficulty in listOf(0, -1, 4, 100)) {
            val numbers = SortingNumbersGenerator.generate(difficulty, Random(42))
            assertEquals(6, numbers.size)
            assertTrue(numbers.all { it in 1..50 })
        }
    }

    @Test
    fun `same seed gives same numbers`() {
        for (difficulty in 1..3) {
            val first = SortingNumbersGenerator.generate(difficulty, Random(123))
            val second = SortingNumbersGenerator.generate(difficulty, Random(123))
            assertEquals(first, second)
        }
    }

    @Test
    fun `different seeds give different numbers`() {
        // Не строгая гарантия для любых seed, но для выбранных пар должна выполняться.
        for (difficulty in 1..3) {
            val first = SortingNumbersGenerator.generate(difficulty, Random(1))
            val second = SortingNumbersGenerator.generate(difficulty, Random(2))
            assertNotEquals(first, second)
        }
    }

    @Test
    fun `countFor matches generated size`() {
        assertEquals(6, SortingNumbersGenerator.countFor(1))
        assertEquals(9, SortingNumbersGenerator.countFor(2))
        assertEquals(12, SortingNumbersGenerator.countFor(3))
        for (difficulty in 1..3) {
            assertEquals(
                SortingNumbersGenerator.countFor(difficulty),
                SortingNumbersGenerator.generate(difficulty, Random(7)).size
            )
        }
    }
}
