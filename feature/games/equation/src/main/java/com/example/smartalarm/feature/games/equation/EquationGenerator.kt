package com.example.smartalarm.feature.games.equation

import kotlin.random.Random

/**
 * Генератор уравнений с пропущенным элементом для игры «Уравнение».
 *
 * Чистая логика без зависимостей от Android; вся случайность идёт через
 * переданный [random], поэтому с фиксированным seed генерация детерминирована.
 *
 * Сложности:
 * - **1** — одно действие (`+ − × ÷`), маленькие числа, пропущено число
 *   (любой операнд или результат): `12 + ? = 20`. Деление всегда нацело,
 *   вычитание без отрицательных результатов.
 * - **2** — пропущен оператор, числа крупнее: `12 ? 3 = 4` → `÷`.
 *   Варианты ответа — все четыре оператора; гарантируется, что равенство
 *   выполняет ровно один из них.
 * - **3** — двухшаговое выражение `a ∘ b ∘ c = d` (операторы `+ − ×`,
 *   стандартный приоритет: умножение раньше сложения/вычитания),
 *   пропущено одно из чисел.
 *
 * Для числовых ответов генерируются три правдоподобных дистрактора
 * (ответ ± 1..10), ни один из которых не удовлетворяет уравнению.
 *
 * @param random источник случайности.
 */
class EquationGenerator(private val random: Random = Random.Default) {

    /**
     * Генерирует новое уравнение.
     *
     * @param difficulty сложность 1..3; значения вне диапазона приводятся к границам.
     * @return задание с текстом уравнения, верным ответом и четырьмя вариантами.
     */
    fun generate(difficulty: Int): EquationTask = when (difficulty.coerceIn(1, 3)) {
        1 -> generateMissingNumber()
        2 -> generateMissingOperator()
        else -> generateTwoStep()
    }

    /** Сложность 1: одно действие, маленькие числа, пропущено число. */
    private fun generateMissingNumber(): EquationTask {
        val op = Operator.entries.random(random)
        val (a, b) = when (op) {
            Operator.PLUS -> (1..9).random(random) to (1..9).random(random)
            Operator.MINUS -> {
                val x = (2..18).random(random)
                x to (1 until x).random(random)
            }

            Operator.MULTIPLY -> (2..9).random(random) to (2..9).random(random)
            Operator.DIVIDE -> {
                val divisor = (2..9).random(random)
                val quotient = (2..9).random(random)
                divisor * quotient to divisor
            }
        }
        val c = op.apply(a, b)!!
        return missingNumberTask(slot = (0..2).random(random)) { slot ->
            when (slot) {
                0 -> NumberSlot(a, "? ${op.symbol} $b = $c", 1) { op.apply(it, b) != c }
                1 -> NumberSlot(b, "$a ${op.symbol} ? = $c", 1) { op.apply(a, it) != c }
                else -> NumberSlot(c, "$a ${op.symbol} $b = ?", 0) { it != c }
            }
        }
    }

    /** Сложность 2: пропущен оператор, ровно один из четырёх подходит. */
    private fun generateMissingOperator(): EquationTask {
        while (true) {
            val op = Operator.entries.random(random)
            val (a, b) = when (op) {
                Operator.PLUS -> (10..99).random(random) to (10..99).random(random)
                Operator.MINUS -> {
                    val x = (20..99).random(random)
                    x to (10 until x).random(random)
                }

                Operator.MULTIPLY -> (3..12).random(random) to (3..12).random(random)
                Operator.DIVIDE -> {
                    val divisor = (2..12).random(random)
                    val quotient = (2..12).random(random)
                    divisor * quotient to divisor
                }
            }
            val c = op.apply(a, b)!!
            if (Operator.entries.count { it.apply(a, b) == c } != 1) continue
            return EquationTask(
                text = "$a ? $b = $c",
                answer = op.symbol,
                options = Operator.entries.map { it.symbol }.shuffled(random)
            )
        }
    }

    /** Сложность 3: двухшаговое выражение `a ∘ b ∘ c = d`, пропущено число. */
    private fun generateTwoStep(): EquationTask {
        val ops = listOf(Operator.PLUS, Operator.MINUS, Operator.MULTIPLY)
        while (true) {
            val op1 = ops.random(random)
            val op2 = ops.random(random)
            // Два умножения подряд дают слишком большие числа — пропускаем.
            if (op1 == Operator.MULTIPLY && op2 == Operator.MULTIPLY) continue

            val a = if (op1 == Operator.MULTIPLY) (2..9).random(random) else (10..40).random(random)
            val b = if (op1 == Operator.MULTIPLY || op2 == Operator.MULTIPLY)
                (2..9).random(random)
            else
                (10..40).random(random)
            val c = if (op2 == Operator.MULTIPLY) (2..9).random(random) else (2..30).random(random)

            val d = evaluateTwoStep(a, op1, b, op2, c)
            if (d < 0 || d > 200) continue

            return missingNumberTask(slot = (0..3).random(random)) { slot ->
                val text = { q0: String, q1: String, q2: String, q3: String ->
                    "$q0 ${op1.symbol} $q1 ${op2.symbol} $q2 = $q3"
                }
                when (slot) {
                    0 -> NumberSlot(a, text("?", "$b", "$c", "$d"), 1) {
                        evaluateTwoStep(it, op1, b, op2, c) != d
                    }

                    1 -> NumberSlot(b, text("$a", "?", "$c", "$d"), 1) {
                        evaluateTwoStep(a, op1, it, op2, c) != d
                    }

                    2 -> NumberSlot(c, text("$a", "$b", "?", "$d"), 1) {
                        evaluateTwoStep(a, op1, b, op2, it) != d
                    }

                    else -> NumberSlot(d, text("$a", "$b", "$c", "?"), 0) { it != d }
                }
            }
        }
    }

    /**
     * Вычисляет `a op1 b op2 c` со стандартным приоритетом операций
     * (умножение раньше сложения и вычитания). Деление не используется.
     */
    private fun evaluateTwoStep(a: Int, op1: Operator, b: Int, op2: Operator, c: Int): Int =
        if (op2 == Operator.MULTIPLY && op1 != Operator.MULTIPLY)
            op1.apply(a, b * c)!!
        else
            op2.apply(op1.apply(a, b)!!, c)!!

    /** Описание пропущенной позиции: верный ответ, текст, минимум для дистракторов и проверка «кандидат НЕ решает уравнение». */
    private class NumberSlot(
        val answer: Int,
        val text: String,
        val minValue: Int,
        val isWrongValue: (Int) -> Boolean
    )

    private fun missingNumberTask(slot: Int, slotFactory: (Int) -> NumberSlot): EquationTask {
        val numberSlot = slotFactory(slot)
        return EquationTask(
            text = numberSlot.text,
            answer = numberSlot.answer.toString(),
            options = numberOptions(numberSlot.answer, numberSlot.minValue, numberSlot.isWrongValue)
        )
    }

    /**
     * Собирает четыре уникальных варианта: верный ответ и три дистрактора
     * в пределах ±10 от ответа, не меньше [minValue] и не решающих уравнение.
     */
    private fun numberOptions(
        answer: Int,
        minValue: Int,
        isWrongValue: (Int) -> Boolean
    ): List<String> {
        val options = linkedSetOf(answer)
        while (options.size < 4) {
            val delta = (1..10).random(random)
            val candidate = answer + if (random.nextBoolean()) delta else -delta
            if (candidate >= minValue && candidate !in options && isWrongValue(candidate))
                options.add(candidate)
        }
        return options.map { it.toString() }.shuffled(random)
    }
}
