package com.example.smartalarm.feature.games.chain

import kotlin.random.Random

/**
 * Генератор цепочек устного счёта для игры «Цепочка».
 *
 * Чистая логика без зависимостей от Android; вся случайность идёт через
 * переданный [random], поэтому с фиксированным seed генерация детерминирована.
 *
 * Инвариант: стартовое число и все промежуточные результаты лежат в диапазоне
 * [MIN_VALUE]..[MAX_VALUE] (0..999). Он обеспечивается по построению — операция
 * и операнд каждого шага выбираются только из допустимых для текущего значения:
 * - «+» — операнд обрезается сверху так, чтобы сумма не превысила 999;
 * - «−» — операнд обрезается сверху текущим значением, чтобы разность
 *   не ушла ниже 0;
 * - «×» — множитель обрезается сверху так, чтобы произведение не превысило 999,
 *   и умножение предлагается только при текущем значении от 2 (умножать 0 и 1
 *   тривиально) до 999 / минимальный_множитель.
 *
 * Хотя бы одна из операций «+»/«−» всегда допустима (их минимальный операнд
 * не больше 99, поэтому значения, для которых недопустимы обе, не существует),
 * так что генерация каждого шага завершается без перебора с отбрасыванием.
 *
 * Сложности:
 * - **1** — 3 операции, старт 1..20, только «+»/«−» с операндом 2..9;
 * - **2** — 5 операций, старт 10..50, «+»/«−» с операндом 2..30,
 *   «×» с множителем 2..5 (вероятность шага-умножения ~25%);
 * - **3** — 7 операций, старт 10..99, «+»/«−» с операндом 10..99,
 *   «×» с множителем 2..9 (вероятность шага-умножения ~30%).
 *
 * @param random источник случайности.
 */
class ChainGenerator(private val random: Random = Random.Default) {

    /** Параметры генерации для одной сложности. */
    private class Settings(
        val length: Int,
        val startRange: IntRange,
        val addRange: IntRange,
        val multRange: IntRange?,
        val multChance: Double
    )

    /**
     * Генерирует новую цепочку.
     *
     * @param difficulty сложность 1..3; значения вне диапазона приводятся к границам.
     * @return цепочка, все промежуточные значения которой лежат в 0..999.
     */
    fun generate(difficulty: Int): Chain {
        val settings = SETTINGS_BY_DIFFICULTY.getValue(difficulty.coerceIn(1, 3))
        val start = settings.startRange.random(random)
        var value = start
        val steps = ArrayList<ChainStep>(settings.length)
        repeat(settings.length) {
            val step = generateStep(value, settings)
            steps.add(step)
            value = step.apply(value)
        }
        return Chain(start, steps)
    }

    /** Генерирует один шаг, допустимый для текущего значения [value]. */
    private fun generateStep(value: Int, settings: Settings): ChainStep {
        val multRange = settings.multRange
        if (multRange != null &&
            value >= MIN_MULTIPLICAND &&
            value * multRange.first <= MAX_VALUE &&
            random.nextDouble() < settings.multChance
        ) {
            val maxFactor = minOf(multRange.last, MAX_VALUE / value)
            return ChainStep(ChainOp.MULTIPLY, (multRange.first..maxFactor).random(random))
        }

        val minOperand = settings.addRange.first
        val plusFeasible = value + minOperand <= MAX_VALUE
        val minusFeasible = value - minOperand >= MIN_VALUE
        val op = when {
            plusFeasible && minusFeasible ->
                if (random.nextBoolean()) ChainOp.PLUS else ChainOp.MINUS

            plusFeasible -> ChainOp.PLUS
            else -> ChainOp.MINUS
        }
        val maxOperand = when (op) {
            ChainOp.PLUS -> minOf(settings.addRange.last, MAX_VALUE - value)
            else -> minOf(settings.addRange.last, value - MIN_VALUE)
        }
        return ChainStep(op, (minOperand..maxOperand).random(random))
    }

    companion object {
        /** Нижняя граница стартового числа и всех промежуточных значений. */
        const val MIN_VALUE = 0

        /** Верхняя граница стартового числа и всех промежуточных значений. */
        const val MAX_VALUE = 999

        /** Минимальное значение, которое имеет смысл умножать (0 и 1 дают тривиальный шаг). */
        private const val MIN_MULTIPLICAND = 2

        private val SETTINGS_BY_DIFFICULTY = mapOf(
            1 to Settings(length = 3, startRange = 1..20, addRange = 2..9, multRange = null, multChance = 0.0),
            2 to Settings(length = 5, startRange = 10..50, addRange = 2..30, multRange = 2..5, multChance = 0.25),
            3 to Settings(length = 7, startRange = 10..99, addRange = 10..99, multRange = 2..9, multChance = 0.3)
        )
    }
}
