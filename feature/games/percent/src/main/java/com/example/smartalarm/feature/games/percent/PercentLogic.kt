package com.example.smartalarm.feature.games.percent

import kotlin.random.Random

/** Тип задачи на проценты. */
enum class PercentTaskType {
    /** «Сколько будет P% от X?» — найти процент от числа. */
    PERCENT_OF,

    /** «Число X увеличили на P%. Какое число получилось?» */
    INCREASE,

    /** «Число X уменьшили на P%. Какое число получилось?» */
    DECREASE,

    /** «Товар стоил X ₽, подорожал на P%. Сколько он стоит теперь?» */
    PRICE_INCREASE,

    /** «Товар стоил X ₽, подешевел на P%. Сколько он стоит теперь?» */
    PRICE_DECREASE,

    /** «P% числа равно Y. Найдите это число.» — обратная задача. */
    FIND_NUMBER
}

/** Исход ответа игрока на задачу. */
enum class AnswerResult {
    /** Ответ верный, игра продолжается со следующей задачей. */
    CORRECT,

    /** Ответ неверный (штраф −10 очков), задана новая задача. */
    WRONG,

    /** Ответ верный и набрано нужное число верных ответов — победа. */
    WIN
}

/**
 * Одна задача игры «Проценты». Ответ всегда целый — генератор строит
 * условие от целых частей.
 *
 * @property type тип задачи
 * @property percent процент P из условия
 * @property given данное в условии число: X (от которого берётся процент /
 * которое изменяется / исходная цена) для всех типов, кроме
 * [PercentTaskType.FIND_NUMBER], где это Y — известное значение P% искомого
 * числа
 * @property answer верный ответ (всегда целый и положительный)
 * @property text текст задачи на русском
 * @property options четыре уникальных варианта ответа в порядке показа,
 * ровно один из них верный
 * @property correctIndex индекс верного варианта в [options]
 */
data class PercentTask(
    val type: PercentTaskType,
    val percent: Int,
    val given: Int,
    val answer: Int,
    val text: String,
    val options: List<Int>,
    val correctIndex: Int
)

/**
 * Чистая логика мини-игры «Проценты» (без зависимостей от Android).
 *
 * Правила: игроку показывается текстовая задача на проценты с целым
 * ответом, и он выбирает ответ из четырёх вариантов. Для победы нужно
 * [targetCorrect] верных ответов (ошибки прогресс не сбрасывают, но
 * штрафуются вызывающей стороной на −10 очков); после любого ответа,
 * кроме победного, задаётся новая задача.
 *
 * Сложность задаёт типы задач и число верных ответов для победы:
 * - **1** (3 ответа) — только [PercentTaskType.PERCENT_OF] с «простыми»
 *   процентами 10/25/50/100 от круглых чисел;
 * - **2** (4 ответа) — [PercentTaskType.PERCENT_OF] с любым процентом,
 *   кратным 5, плюс увеличение/уменьшение числа на P%
 *   ([PercentTaskType.INCREASE], [PercentTaskType.DECREASE]);
 * - **3** (5 ответов) — наценки/скидки на цену товара
 *   ([PercentTaskType.PRICE_INCREASE], [PercentTaskType.PRICE_DECREASE])
 *   и обратные задачи ([PercentTaskType.FIND_NUMBER]).
 *
 * Целочисленность ответа гарантируется построением: данное число X (или
 * искомое в обратной задаче) выбирается кратным 100/НОД(P, 100), поэтому
 * X·P делится на 100 нацело.
 *
 * Дистракторы правдоподобные — типичные ошибки (взяли дополнение до 100%,
 * изменили число не в ту сторону, выдали саму прибавку или исходное число,
 * в обратной задаче применили процент ещё раз) плюс значения, близкие к
 * верному ответу.
 *
 * @param difficulty уровень сложности 1..3 (иные значения трактуются как 3)
 * @param random источник случайности; подставьте [Random] с seed для
 * воспроизводимых партий (используется в тестах)
 */
class PercentGame(private val difficulty: Int, private val random: Random = Random.Default) {

    /** Сколько верных ответов нужно для победы: 3/4/5 по сложности. */
    val targetCorrect: Int = when (difficulty) {
        1 -> 3
        2 -> 4
        else -> 5
    }

    /** Число верных ответов, данных к текущему моменту (0..[targetCorrect]). */
    var correctCount: Int = 0
        private set

    /** Число ошибок, сделанных к текущему моменту. */
    var mistakeCount: Int = 0
        private set

    private var previousText = ""

    /** Текущая задача, которую нужно показать игроку. */
    var currentTask: PercentTask = generateTask()
        private set

    /**
     * Обрабатывает выбор игроком варианта ответа.
     *
     * При верном ответе увеличивается [correctCount]; если достигнут
     * [targetCorrect] — возвращается [AnswerResult.WIN] и задача не меняется,
     * иначе задаётся новая задача. При ошибке увеличивается [mistakeCount]
     * и тоже задаётся новая задача.
     *
     * @param optionIndex индекс выбранного варианта в
     * [PercentTask.options] текущей задачи
     * @return исход ответа
     * @throws IllegalArgumentException если индекс вне 0..3
     */
    fun answer(optionIndex: Int): AnswerResult {
        require(optionIndex in currentTask.options.indices) {
            "Индекс варианта вне диапазона: $optionIndex"
        }
        return if (optionIndex == currentTask.correctIndex) {
            correctCount++
            if (correctCount >= targetCorrect) {
                AnswerResult.WIN
            } else {
                currentTask = generateTask()
                AnswerResult.CORRECT
            }
        } else {
            mistakeCount++
            currentTask = generateTask()
            AnswerResult.WRONG
        }
    }

    private fun generateTask(): PercentTask {
        var task = generateSingleTask()
        while (task.text == previousText) {
            task = generateSingleTask()
        }
        previousText = task.text
        return task
    }

    private fun generateSingleTask(): PercentTask {
        val type = when (difficulty) {
            1 -> PercentTaskType.PERCENT_OF

            2 -> listOf(
                PercentTaskType.PERCENT_OF,
                PercentTaskType.INCREASE,
                PercentTaskType.DECREASE
            ).random(random)

            else -> listOf(
                PercentTaskType.PRICE_INCREASE,
                PercentTaskType.PRICE_DECREASE,
                PercentTaskType.FIND_NUMBER
            ).random(random)
        }

        val percent: Int
        val given: Int
        val answer: Int
        when (type) {
            PercentTaskType.PERCENT_OF -> {
                if (difficulty == 1) {
                    percent = SIMPLE_PERCENTS.random(random)
                    given = when (percent) {
                        10 -> 10 * random.nextInt(2, 31) // 20..300, кратно 10
                        25 -> 20 * random.nextInt(1, 11) // 20..200, кратно 20
                        50 -> 2 * random.nextInt(5, 101) // 10..200, чётное
                        else -> 10 * random.nextInt(1, 21) // 100%: 10..200
                    }
                } else {
                    percent = 5 * random.nextInt(1, 20) // 5..95, кратно 5
                    given = baseFor(percent, maxBase = 400)
                }
                answer = given * percent / 100
            }

            PercentTaskType.INCREASE, PercentTaskType.DECREASE -> {
                percent = 5 * random.nextInt(1, 20) // 5..95, кратно 5
                given = baseFor(percent, maxBase = 300)
                answer =
                    if (type == PercentTaskType.INCREASE) given + given * percent / 100
                    else given - given * percent / 100
            }

            PercentTaskType.PRICE_INCREASE, PercentTaskType.PRICE_DECREASE -> {
                percent = PRICE_PERCENTS.random(random)
                given = baseFor(percent, minBase = 50, maxBase = 1500)
                answer =
                    if (type == PercentTaskType.PRICE_INCREASE) given + given * percent / 100
                    else given - given * percent / 100
            }

            PercentTaskType.FIND_NUMBER -> {
                percent = 5 * random.nextInt(1, 20) // 5..95, кратно 5
                answer = baseFor(percent, maxBase = 400)
                given = answer * percent / 100
            }
        }

        val text = when (type) {
            PercentTaskType.PERCENT_OF -> "Сколько будет $percent% от $given?"
            PercentTaskType.INCREASE ->
                "Число $given увеличили на $percent%. Какое число получилось?"

            PercentTaskType.DECREASE ->
                "Число $given уменьшили на $percent%. Какое число получилось?"

            PercentTaskType.PRICE_INCREASE ->
                "Товар стоил $given ₽, подорожал на $percent%. Сколько он стоит теперь?"

            PercentTaskType.PRICE_DECREASE ->
                "Товар стоил $given ₽, подешевел на $percent%. Сколько он стоит теперь?"

            PercentTaskType.FIND_NUMBER ->
                "$percent% числа равно $given. Найдите это число."
        }

        val numbers = (generateDistractors(type, percent, given, answer) + answer)
            .shuffled(random)
        return PercentTask(
            type = type,
            percent = percent,
            given = given,
            answer = answer,
            text = text,
            options = numbers,
            correctIndex = numbers.indexOf(answer)
        )
    }

    /**
     * Выбирает число, P% которого заведомо целые: кратное 100/НОД(P, 100),
     * в диапазоне примерно [minBase]..[maxBase].
     */
    private fun baseFor(percent: Int, minBase: Int = 0, maxBase: Int): Int {
        val step = 100 / gcd(percent, 100)
        return step * random.nextInt(minBase / step + 1, maxBase / step + 1)
    }

    /**
     * Подбирает три различных положительных правдоподобных дистрактора:
     * типичные ошибки для типа задачи плюс значения рядом с ответом.
     */
    private fun generateDistractors(
        type: PercentTaskType,
        percent: Int,
        given: Int,
        answer: Int
    ): List<Int> {
        val candidates = mutableListOf<Int>()
        when (type) {
            PercentTaskType.PERCENT_OF -> {
                candidates += given - answer // взяли (100−P)%
                candidates += given // выдали исходное число
                candidates += answer * 2
                candidates += answer / 2
            }

            PercentTaskType.INCREASE, PercentTaskType.PRICE_INCREASE -> {
                val delta = given * percent / 100
                candidates += given - delta // уменьшили вместо увеличения
                candidates += delta // выдали только прибавку
                candidates += given // «ничего не изменилось»
                candidates += answer + delta // увеличили дважды
            }

            PercentTaskType.DECREASE, PercentTaskType.PRICE_DECREASE -> {
                val delta = given * percent / 100
                candidates += given + delta // увеличили вместо уменьшения
                candidates += delta // выдали только скидку
                candidates += given // «ничего не изменилось»
                candidates += answer - delta // уменьшили дважды
            }

            PercentTaskType.FIND_NUMBER -> {
                if (given * percent % 100 == 0) candidates += given * percent / 100
                candidates += given // приняли Y за искомое число
                candidates += given * 2
                candidates += answer * 2
            }
        }
        // Близкие к ответу значения.
        candidates += answer + (answer / 10).coerceAtLeast(1)
        candidates += answer - (answer / 10).coerceAtLeast(1)
        candidates += answer + (answer / 4).coerceAtLeast(2)
        candidates += answer - (answer / 4).coerceAtLeast(2)

        val distractors = candidates
            .filter { it > 0 && it != answer }
            .distinct()
            .shuffled(random)
            .take(DISTRACTOR_COUNT)
            .toMutableSet()
        // Страховка на случай слишком бедного пула кандидатов.
        while (distractors.size < DISTRACTOR_COUNT) {
            val delta = random.nextInt(1, answer.coerceAtLeast(10) + 1)
            val filler = if (random.nextBoolean()) answer + delta else answer - delta
            if (filler > 0 && filler != answer) distractors += filler
        }
        return distractors.toList()
    }

    private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

    private companion object {
        const val DISTRACTOR_COUNT = 3

        /** «Простые» проценты первой сложности. */
        val SIMPLE_PERCENTS = listOf(10, 25, 50, 100)

        /** Проценты наценок/скидок — с ними цены получаются «магазинными». */
        val PRICE_PERCENTS = listOf(10, 15, 20, 25, 30, 40, 50, 60, 70, 75, 80, 90)
    }
}
