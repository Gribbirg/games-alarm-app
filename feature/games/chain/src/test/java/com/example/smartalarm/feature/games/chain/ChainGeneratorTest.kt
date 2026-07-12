package com.example.smartalarm.feature.games.chain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Тесты генератора цепочек: длины по сложностям, инвариант 0..999,
 * диапазоны операндов, наличие/отсутствие умножения и детерминизм seed.
 */
class ChainGeneratorTest {

    private val lengthByDifficulty = mapOf(1 to 3, 2 to 5, 3 to 7)
    private val addRangeByDifficulty = mapOf(1 to 2..9, 2 to 2..30, 3 to 10..99)
    private val multRangeByDifficulty = mapOf(2 to 2..5, 3 to 2..9)

    /** Независимое от [Chain] вычисление промежуточных значений цепочки. */
    private fun manualResults(chain: Chain): List<Int> {
        val results = mutableListOf<Int>()
        var value = chain.start
        for (step in chain.steps) {
            value = when (step.op) {
                ChainOp.PLUS -> value + step.operand
                ChainOp.MINUS -> value - step.operand
                ChainOp.MULTIPLY -> value * step.operand
            }
            results.add(value)
        }
        return results
    }

    @Test
    fun chainLengthMatchesDifficulty() {
        val generator = ChainGenerator(Random(42))
        for ((difficulty, length) in lengthByDifficulty)
            repeat(200) {
                assertEquals(length, generator.generate(difficulty).steps.size)
            }
    }

    @Test
    fun startAndIntermediateValuesStayInRange() {
        val generator = ChainGenerator(Random(123))
        for (difficulty in 1..3)
            repeat(300) {
                val chain = generator.generate(difficulty)
                assertTrue(
                    "Старт ${chain.start} вне 0..999 (сложность $difficulty)",
                    chain.start in 0..999
                )
                for (value in chain.intermediateResults)
                    assertTrue(
                        "Промежуточное $value вне 0..999 (сложность $difficulty, цепочка $chain)",
                        value in 0..999
                    )
            }
    }

    @Test
    fun resultEqualsSequentialApplicationOfSteps() {
        val generator = ChainGenerator(Random(7))
        for (difficulty in 1..3)
            repeat(300) {
                val chain = generator.generate(difficulty)
                val manual = manualResults(chain)
                assertEquals(manual, chain.intermediateResults)
                assertEquals(manual.last(), chain.result)
            }
    }

    @Test
    fun difficultyOneHasNoMultiplication() {
        val generator = ChainGenerator(Random(99))
        repeat(300) {
            assertFalse(generator.generate(1).steps.any { it.op == ChainOp.MULTIPLY })
        }
    }

    @Test
    fun operandsStayWithinDifficultyRanges() {
        val generator = ChainGenerator(Random(2024))
        for (difficulty in 1..3)
            repeat(300) {
                for (step in generator.generate(difficulty).steps) {
                    val range = when (step.op) {
                        ChainOp.MULTIPLY -> multRangeByDifficulty.getValue(difficulty)
                        else -> addRangeByDifficulty.getValue(difficulty)
                    }
                    assertTrue(
                        "Операнд ${step.operand} операции ${step.op} вне $range (сложность $difficulty)",
                        step.operand in range
                    )
                }
            }
    }

    @Test
    fun multiplicationAppearsOnHigherDifficulties() {
        val generator = ChainGenerator(Random(5))
        for (difficulty in 2..3) {
            val hasMultiplication = (1..300).any {
                generator.generate(difficulty).steps.any { step -> step.op == ChainOp.MULTIPLY }
            }
            assertTrue("Нет умножений на сложности $difficulty", hasMultiplication)
        }
    }

    @Test
    fun sameSeedGivesSameChains() {
        fun chains(seed: Long): List<Chain> {
            val generator = ChainGenerator(Random(seed))
            return (1..3).flatMap { difficulty -> List(50) { generator.generate(difficulty) } }
        }
        assertEquals(chains(42), chains(42))
    }

    @Test
    fun difficultyOutOfRangeIsCoerced() {
        assertEquals(3, ChainGenerator(Random(1)).generate(0).steps.size)
        assertEquals(3, ChainGenerator(Random(1)).generate(-5).steps.size)
        assertEquals(7, ChainGenerator(Random(1)).generate(99).steps.size)
    }
}
