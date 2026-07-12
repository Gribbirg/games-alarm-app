package com.example.smartalarm.feature.games.roman

import kotlin.random.Random

/** Направление перевода в вопросе. */
enum class QuestionDirection {
    /** Показана римская запись, варианты ответа — арабские числа. */
    ROMAN_TO_ARABIC,

    /** Показано арабское число, варианты ответа — римские записи. */
    ARABIC_TO_ROMAN
}

/** Исход ответа игрока на вопрос. */
enum class AnswerResult {
    /** Ответ верный, игра продолжается со следующим вопросом. */
    CORRECT,

    /** Ответ неверный (штраф −10 очков), задан новый вопрос. */
    WRONG,

    /** Ответ верный и набрано нужное число верных ответов — победа. */
    WIN
}

/**
 * Один вопрос игры «Римские числа».
 *
 * @property direction направление перевода
 * @property number загаданное число
 * @property prompt текст вопроса — запись [number] в «исходной» системе
 * (римская для [QuestionDirection.ROMAN_TO_ARABIC], арабская иначе)
 * @property options четыре варианта ответа в порядке показа (записи
 * в «целевой» системе), ровно один из них верный
 * @property correctIndex индекс верного варианта в [options]
 */
data class RomanQuestion(
    val direction: QuestionDirection,
    val number: Int,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int
)

/**
 * Чистая логика мини-игры «Римские числа» (без зависимостей от Android).
 *
 * Правила: игроку показывается число в римской или арабской записи
 * (направление каждого вопроса случайно), и он выбирает его перевод из
 * четырёх вариантов. Для победы нужно [targetCorrect] верных ответов
 * (ошибки прогресс не сбрасывают, но штрафуются вызывающей стороной на
 * −10 очков); после любого ответа, кроме победного, задаётся новый вопрос.
 *
 * Сложность задаёт диапазон загадываемых чисел и число верных ответов:
 * 1 — 1..20 и 4 ответа, 2 — 1..100 и 5 ответов, 3 — 1..1000 и 6 ответов.
 *
 * Дистракторы правдоподобные: числа, отличающиеся от загаданного на 1..10,
 * и числа на «римском расстоянии» в один символ (±40, ±50, ±90, ±100,
 * ±400, ±500, ±900) — их римские записи похожи на верную.
 *
 * @param difficulty уровень сложности 1..3 (иные значения трактуются как 3)
 * @param random источник случайности; подставьте [Random] с seed для
 * воспроизводимых партий (используется в тестах)
 */
class RomanGame(difficulty: Int, private val random: Random = Random.Default) {

    /** Максимальное загадываемое число: 20/100/1000 по сложности. */
    val maxNumber: Int = when (difficulty) {
        1 -> 20
        2 -> 100
        else -> 1000
    }

    /** Сколько верных ответов нужно для победы: 4/5/6 по сложности. */
    val targetCorrect: Int = when (difficulty) {
        1 -> 4
        2 -> 5
        else -> 6
    }

    /** Число верных ответов, данных к текущему моменту (0..[targetCorrect]). */
    var correctCount: Int = 0
        private set

    /** Число ошибок, сделанных к текущему моменту. */
    var mistakeCount: Int = 0
        private set

    private var previousNumber = 0

    /** Текущий вопрос, который нужно показать игроку. */
    var currentQuestion: RomanQuestion = generateQuestion()
        private set

    /**
     * Обрабатывает выбор игроком варианта ответа.
     *
     * При верном ответе увеличивается [correctCount]; если достигнут
     * [targetCorrect] — возвращается [AnswerResult.WIN] и вопрос не меняется,
     * иначе задаётся новый вопрос. При ошибке увеличивается [mistakeCount]
     * и тоже задаётся новый вопрос.
     *
     * @param optionIndex индекс выбранного варианта в
     * [RomanQuestion.options] текущего вопроса
     * @return исход ответа
     * @throws IllegalArgumentException если индекс вне 0..3
     */
    fun answer(optionIndex: Int): AnswerResult {
        require(optionIndex in currentQuestion.options.indices) {
            "Индекс варианта вне диапазона: $optionIndex"
        }
        return if (optionIndex == currentQuestion.correctIndex) {
            correctCount++
            if (correctCount >= targetCorrect) {
                AnswerResult.WIN
            } else {
                currentQuestion = generateQuestion()
                AnswerResult.CORRECT
            }
        } else {
            mistakeCount++
            currentQuestion = generateQuestion()
            AnswerResult.WRONG
        }
    }

    private fun generateQuestion(): RomanQuestion {
        var number = random.nextInt(1, maxNumber + 1)
        while (number == previousNumber) {
            number = random.nextInt(1, maxNumber + 1)
        }
        previousNumber = number

        val direction =
            if (random.nextBoolean()) QuestionDirection.ROMAN_TO_ARABIC
            else QuestionDirection.ARABIC_TO_ROMAN

        val numbers = (generateDistractors(number) + number).shuffled(random)
        val prompt: String
        val options: List<String>
        when (direction) {
            QuestionDirection.ROMAN_TO_ARABIC -> {
                prompt = RomanNumerals.toRoman(number)
                options = numbers.map { it.toString() }
            }

            QuestionDirection.ARABIC_TO_ROMAN -> {
                prompt = number.toString()
                options = numbers.map { RomanNumerals.toRoman(it) }
            }
        }
        return RomanQuestion(
            direction = direction,
            number = number,
            prompt = prompt,
            options = options,
            correctIndex = numbers.indexOf(number)
        )
    }

    /**
     * Подбирает три различных правдоподобных дистрактора для [number]
     * в пределах 1..[maxNumber].
     */
    private fun generateDistractors(number: Int): List<Int> {
        val candidates = mutableSetOf<Int>()
        for (offset in SIMILAR_OFFSETS) {
            candidates += number + offset
            candidates += number - offset
        }
        candidates.removeAll { it !in 1..maxNumber || it == number }

        val distractors = candidates.shuffled(random).take(DISTRACTOR_COUNT).toMutableSet()
        // Страховка на случай слишком бедного пула кандидатов.
        while (distractors.size < DISTRACTOR_COUNT) {
            val filler = random.nextInt(1, maxNumber + 1)
            if (filler != number) distractors += filler
        }
        return distractors.toList()
    }

    private companion object {
        const val DISTRACTOR_COUNT = 3

        /**
         * Смещения кандидатов-дистракторов: близкие числа (1..10) и шаги
         * в один римский символ (40, 50, 90, 100, 400, 500, 900), дающие
         * похожие римские записи.
         */
        val SIMILAR_OFFSETS: List<Int> =
            (1..10).toList() + listOf(40, 50, 90, 100, 400, 500, 900)
    }
}
