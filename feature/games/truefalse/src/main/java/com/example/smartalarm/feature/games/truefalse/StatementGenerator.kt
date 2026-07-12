package com.example.smartalarm.feature.games.truefalse

import kotlin.random.Random

/**
 * Генератор арифметических утверждений для игры «Верно или нет».
 *
 * Утверждения двух видов (выбираются равновероятно):
 * - равенство: `a op b = r`, где `op` — «+», «-» или «×»;
 * - сравнение: `a op b cmp n` или `n cmp a op b`, где `cmp` — «>» или «<».
 *
 * Истинные и ложные утверждения генерируются поровну (примерно 50/50).
 * Ложное равенство получается из истинного искажением результата:
 * либо сдвигом на малую правдоподобную дельту ±1..3, либо (с вероятностью ~30 %,
 * если возможно) перестановкой двух цифр результата (47 → 74). Оба способа
 * гарантируют, что показанное число не равно настоящему результату.
 * В ложном сравнении число `n` берётся по «неправильную» сторону от значения
 * выражения на расстоянии 1..3, а в четверти случаев `n` равно значению
 * выражения — строгое сравнение при равенстве тоже ложно.
 *
 * Диапазоны операндов по сложности:
 *
 * | Сложность | `a + b`    | `a - b` (a > b)           | `a × b`            |
 * |-----------|------------|---------------------------|--------------------|
 * | 1         | 2..20      | a: 10..30, b: 1..a-1      | 2..9 × 2..9        |
 * | 2         | 11..99     | a: 30..99, b: 10..a-1     | 3..12 × 4..19      |
 * | 3         | 101..999   | a: 200..999, b: 100..a-1  | 7..19 × 11..29     |
 *
 * Все числа в утверждениях неотрицательны; значение выражения всегда ≥ 1.
 * Класс не зависит от Android и детерминирован при фиксированном [random].
 *
 * @param difficulty уровень сложности 1..3 (значения вне диапазона приводятся к границе)
 * @param random источник случайности; передайте [Random] с фиксированным seed для воспроизводимости
 */
class StatementGenerator(
    difficulty: Int,
    private val random: Random = Random.Default
) {

    private val level = difficulty.coerceIn(1, 3)

    /** Арифметическая операция внутри выражения. */
    private enum class Op(val symbol: String) { ADD("+"), SUB("-"), MUL("×") }

    /** Выражение вида «a op b» с уже вычисленным значением. */
    private data class Expression(val text: String, val value: Int)

    /**
     * Генерирует утверждение со случайной (≈50/50) истинностью.
     *
     * @return утверждение, у которого [Statement.isTrue] совпадает с фактической истинностью текста
     */
    fun generate(): Statement = generate(random.nextBoolean())

    /**
     * Генерирует утверждение с заданной истинностью.
     *
     * @param shouldBeTrue `true` — сгенерировать истинное утверждение, `false` — ложное
     * @return утверждение, у которого [Statement.isTrue] равно [shouldBeTrue]
     */
    fun generate(shouldBeTrue: Boolean): Statement =
        if (random.nextBoolean()) equality(shouldBeTrue) else comparison(shouldBeTrue)

    private fun equality(shouldBeTrue: Boolean): Statement {
        val expression = randomExpression()
        val shown = if (shouldBeTrue) expression.value else corrupt(expression.value)
        return Statement("${expression.text} = $shown", shown == expression.value)
    }

    private fun comparison(shouldBeTrue: Boolean): Statement {
        val expression = randomExpression()
        val greater = random.nextBoolean()
        val expressionOnLeft = random.nextBoolean()

        // Истинность утверждения требует n > value ровно в этих случаях:
        // «value < n» (выражение слева, «<») и «n > value» (выражение справа, «>»).
        val trueNeedsAbove = expressionOnLeft != greater
        val nAbove = if (shouldBeTrue) trueNeedsAbove else !trueNeedsAbove
        val delta = random.nextInt(1, MAX_DELTA + 1)
        val n = when {
            // Коварный ложный случай: n равно значению, строгое сравнение ложно.
            !shouldBeTrue && random.nextInt(4) == 0 -> expression.value
            nAbove -> expression.value + delta
            // value >= 1, поэтому после coerceAtLeast(0) всегда n < value.
            else -> (expression.value - delta).coerceAtLeast(0)
        }

        val symbol = if (greater) ">" else "<"
        val text: String
        val isTrue: Boolean
        if (expressionOnLeft) {
            text = "${expression.text} $symbol $n"
            isTrue = if (greater) expression.value > n else expression.value < n
        } else {
            text = "$n $symbol ${expression.text}"
            isTrue = if (greater) n > expression.value else n < expression.value
        }
        return Statement(text, isTrue)
    }

    private fun randomExpression(): Expression {
        val op = Op.entries[random.nextInt(Op.entries.size)]
        val (a, b) = when (op) {
            Op.ADD -> when (level) {
                1 -> (2..20).random(random) to (2..20).random(random)
                2 -> (11..99).random(random) to (11..99).random(random)
                else -> (101..999).random(random) to (101..999).random(random)
            }

            Op.SUB -> when (level) {
                1 -> (10..30).random(random).let { it to (1 until it).random(random) }
                2 -> (30..99).random(random).let { it to (10 until it).random(random) }
                else -> (200..999).random(random).let { it to (100 until it).random(random) }
            }

            Op.MUL -> when (level) {
                1 -> (2..9).random(random) to (2..9).random(random)
                2 -> (3..12).random(random) to (4..19).random(random)
                else -> (7..19).random(random) to (11..29).random(random)
            }
        }
        val value = when (op) {
            Op.ADD -> a + b
            Op.SUB -> a - b
            Op.MUL -> a * b
        }
        return Expression("$a ${op.symbol} $b", value)
    }

    /**
     * Искажает результат так, чтобы он гарантированно отличался от [value]:
     * перестановка двух разных цифр (без ведущего нуля) или сдвиг на ±1..3
     * (в минус — только если результат остаётся неотрицательным).
     */
    private fun corrupt(value: Int): Int {
        if (random.nextInt(10) < 3) {
            digitSwap(value)?.let { return it }
        }
        val delta = random.nextInt(1, MAX_DELTA + 1)
        return if (random.nextBoolean() && value - delta >= 0) value - delta else value + delta
    }

    /**
     * Меняет местами две разные цифры числа. Возвращает `null`, если число
     * одноцифровое или любая перестановка дала бы то же число либо ведущий ноль.
     */
    private fun digitSwap(value: Int): Int? {
        val digits = value.toString().toCharArray()
        val pairs = ArrayList<Pair<Int, Int>>()
        for (i in digits.indices) {
            for (j in i + 1 until digits.size) {
                if (digits[i] != digits[j] && !(i == 0 && digits[j] == '0')) {
                    pairs.add(i to j)
                }
            }
        }
        if (pairs.isEmpty()) return null
        val (i, j) = pairs[random.nextInt(pairs.size)]
        val swapped = digits.copyOf().also {
            it[i] = digits[j]
            it[j] = digits[i]
        }
        return String(swapped).toInt()
    }

    private companion object {
        /** Максимальная дельта искажения в ложных утверждениях. */
        const val MAX_DELTA = 3
    }
}
