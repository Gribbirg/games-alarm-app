package com.example.smartalarm.feature.games.sequence

import kotlin.random.Random

/**
 * Чистый генератор заданий игры «Продолжи ряд» — без зависимостей от Android.
 *
 * Параметры генерации (все значения — небольшие Int, переполнение невозможно):
 * - [SequenceType.ARITHMETIC] — 4–5 членов, шаг 2..9, растущий или убывающий
 *   (убывающий подбирается так, чтобы ряд и ответ оставались неотрицательными);
 * - [SequenceType.GEOMETRIC] — 4 члена, первый член 2..5, знаменатель 2..3
 *   (максимальный ответ 5 · 3⁴ = 405);
 * - [SequenceType.SQUARES] — 4–5 членов, квадраты k², … начиная с k = 1..7
 *   (максимальный ответ 12² = 144);
 * - [SequenceType.ALTERNATING] — 5 членов, чередование шагов +a и −b,
 *   a = 4..9, b = 1..a−1, старт 1..10 (ряд в целом растёт и положителен);
 * - [SequenceType.FIBONACCI_LIKE] — 5 членов, t₁ = 1..5, t₂ = t₁..9,
 *   далее сумма двух предыдущих (максимальный ответ 60).
 *
 * Вся случайность идёт через переданный [Random], поэтому генерация
 * детерминирована при фиксированном seed.
 */
object SequenceGenerator {

    /** Число вариантов ответа в каждом задании. */
    const val OPTIONS_COUNT = 4

    /**
     * Типы рядов, доступные на данной сложности.
     *
     * @param difficulty сложность 1..3 (значения вне диапазона приводятся к нему).
     * @return список доступных типов: 1 — арифметика; 2 — плюс геометрия
     * и квадраты; 3 — плюс чередующиеся шаги и фибоначчи-подобные ряды.
     */
    fun typesFor(difficulty: Int): List<SequenceType> =
        when (difficulty.coerceIn(1, 3)) {
            1 -> listOf(SequenceType.ARITHMETIC)
            2 -> listOf(
                SequenceType.ARITHMETIC,
                SequenceType.GEOMETRIC,
                SequenceType.SQUARES
            )
            else -> SequenceType.entries.toList()
        }

    /**
     * Число рядов, которые нужно решить для победы на данной сложности.
     *
     * @param difficulty сложность 1..3 (значения вне диапазона приводятся к нему).
     * @return 3, 4 или 5 рядов соответственно.
     */
    fun roundsFor(difficulty: Int): Int = difficulty.coerceIn(1, 3) + 2

    /**
     * Генерирует одно задание случайного типа, доступного на данной сложности.
     *
     * @param difficulty сложность 1..3.
     * @param random источник случайности (для детерминизма передайте Random(seed)).
     */
    fun generate(difficulty: Int, random: Random): SequenceTask {
        val types = typesFor(difficulty)
        return generate(types[random.nextInt(types.size)], random)
    }

    /**
     * Генерирует одно задание указанного типа.
     *
     * @param type тип ряда.
     * @param random источник случайности.
     */
    fun generate(type: SequenceType, random: Random): SequenceTask {
        val (terms, answer) = when (type) {
            SequenceType.ARITHMETIC -> arithmetic(random)
            SequenceType.GEOMETRIC -> geometric(random)
            SequenceType.SQUARES -> squares(random)
            SequenceType.ALTERNATING -> alternating(random)
            SequenceType.FIBONACCI_LIKE -> fibonacciLike(random)
        }
        return SequenceTask(type, terms, answer, buildOptions(type, terms, answer, random))
    }

    private fun arithmetic(random: Random): Pair<List<Int>, Int> {
        val length = random.nextInt(4, 6)
        val step = random.nextInt(2, 10)
        val decreasing = random.nextBoolean()
        val start =
            if (decreasing) step * length + random.nextInt(1, 21)
            else random.nextInt(1, 21)
        val signedStep = if (decreasing) -step else step
        val terms = List(length) { start + it * signedStep }
        return terms to (terms.last() + signedStep)
    }

    private fun geometric(random: Random): Pair<List<Int>, Int> {
        val first = random.nextInt(2, 6)
        val ratio = random.nextInt(2, 4)
        val terms = MutableList(4) { first }
        for (i in 1 until terms.size) terms[i] = terms[i - 1] * ratio
        return terms.toList() to (terms.last() * ratio)
    }

    private fun squares(random: Random): Pair<List<Int>, Int> {
        val length = random.nextInt(4, 6)
        val startK = random.nextInt(1, 8)
        val terms = List(length) { (startK + it) * (startK + it) }
        return terms to ((startK + length) * (startK + length))
    }

    private fun alternating(random: Random): Pair<List<Int>, Int> {
        val plus = random.nextInt(4, 10)
        val minus = random.nextInt(1, plus)
        val start = random.nextInt(1, 11)
        val length = 5
        val terms = MutableList(length) { start }
        for (i in 1 until length)
            terms[i] = terms[i - 1] + if (i % 2 == 1) plus else -minus
        val answer = terms.last() + if (length % 2 == 1) plus else -minus
        return terms.toList() to answer
    }

    private fun fibonacciLike(random: Random): Pair<List<Int>, Int> {
        val length = 5
        val first = random.nextInt(1, 6)
        val second = random.nextInt(first, 10)
        val terms = MutableList(length) { 0 }
        terms[0] = first
        terms[1] = second
        for (i in 2 until length) terms[i] = terms[i - 1] + terms[i - 2]
        return terms.toList() to (terms[length - 1] + terms[length - 2])
    }

    /**
     * Собирает 4 уникальных варианта ответа: правильный и три правдоподобных
     * дистрактора (типичные ошибки: неверный шаг, «продолжил как арифметику»,
     * промах на 1–2). Дистракторы не совпадают ни с ответом, ни друг с другом.
     */
    private fun buildOptions(
        type: SequenceType,
        terms: List<Int>,
        answer: Int,
        random: Random
    ): List<Int> {
        val last = terms.last()
        val lastDiff = last - terms[terms.size - 2]
        val plausible = mutableListOf(
            // «Продолжил с последним шагом» — ошибочно для всех типов, кроме
            // арифметики (там candidates отфильтруются как равные ответу).
            last + lastDiff,
            answer + 1, answer - 1,
            answer + 2, answer - 2
        )
        when (type) {
            SequenceType.ARITHMETIC -> {
                plausible += answer + lastDiff
                plausible += last
            }
            SequenceType.GEOMETRIC -> {
                val ratio = if (terms[0] != 0) terms[1] / terms[0] else 2
                plausible += answer + ratio
                plausible += answer - ratio
                plausible += last * (ratio + 1)
            }
            SequenceType.SQUARES -> {
                // Ответ — точный квадрат: root² == answer.
                val root = generateSequence(1) { it + 1 }.first { it * it >= answer }
                plausible += (root + 1) * (root + 1) // пропустил один квадрат
                plausible += answer + 2 * lastDiff
            }
            SequenceType.ALTERNATING -> {
                val stepUp = terms[1] - terms[0]
                val stepDown = terms[1] - terms[2]
                // Применил шаг не той «очереди».
                plausible += last + stepUp
                plausible += last - stepDown
                plausible += last + stepUp - stepDown
            }
            SequenceType.FIBONACCI_LIKE -> {
                plausible += last * 2
                plausible += answer + terms[terms.size - 3]
            }
        }

        val distractors = plausible
            .filter { it != answer }
            .distinct()
            .shuffled(random)
            .take(OPTIONS_COUNT - 1)
            .toMutableList()

        // Страховка: добиваем до трёх дистракторов заведомо уникальными значениями.
        var delta = 3
        while (distractors.size < OPTIONS_COUNT - 1) {
            for (candidate in listOf(answer + delta, answer - delta)) {
                if (distractors.size < OPTIONS_COUNT - 1 &&
                    candidate != answer && candidate !in distractors
                ) distractors.add(candidate)
            }
            delta++
        }

        return (distractors + answer).shuffled(random)
    }
}
