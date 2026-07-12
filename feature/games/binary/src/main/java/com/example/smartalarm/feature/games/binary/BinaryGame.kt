package com.example.smartalarm.feature.games.binary

import kotlin.random.Random

/** Направление перевода в вопросе. */
enum class QuestionDirection {
    /** Показана двоичная запись, варианты ответа — десятичные числа. */
    BINARY_TO_DECIMAL,

    /** Показано десятичное число, варианты ответа — двоичные записи. */
    DECIMAL_TO_BINARY
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
 * Один вопрос игры «Двоичный код».
 *
 * @property direction направление перевода
 * @property number загаданное число
 * @property prompt текст вопроса — запись [number] в «исходной» системе
 * (двоичная для [QuestionDirection.BINARY_TO_DECIMAL], десятичная иначе),
 * без индексов систем счисления — их добавляет UI
 * @property options четыре варианта ответа в порядке показа (записи в
 * «целевой» системе; двоичные — без ведущих нулей), ровно один верный
 * @property correctIndex индекс верного варианта в [options]
 */
data class BinaryQuestion(
    val direction: QuestionDirection,
    val number: Int,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int
)

/**
 * Чистая логика мини-игры «Двоичный код» (без зависимостей от Android).
 *
 * Правила: игроку показывается число в двоичной или десятичной записи
 * (направление каждого вопроса случайно), и он выбирает его перевод из
 * четырёх вариантов. Для победы нужно [targetCorrect] верных ответов
 * (ошибки прогресс не сбрасывают, но штрафуются вызывающей стороной на
 * −10 очков); после любого ответа, кроме победного, задаётся новый вопрос.
 *
 * Сложность задаёт диапазон загадываемых чисел и число верных ответов:
 * 1 — 1..15 (4 бита) и 3 ответа, 2 — 1..63 (6 бит) и 4 ответа,
 * 3 — 1..255 (8 бит) и 5 ответов.
 *
 * Дистракторы правдоподобные: числа, отличающиеся от загаданного на
 * ±1/±2, число с перевёрнутой битовой строкой и числа с одним изменённым
 * битом — их записи в обеих системах похожи на верную.
 *
 * @param difficulty уровень сложности 1..3 (иные значения трактуются как 3)
 * @param random источник случайности; подставьте [Random] с seed для
 * воспроизводимых партий (используется в тестах)
 */
class BinaryGame(difficulty: Int, private val random: Random = Random.Default) {

    /** Максимальное загадываемое число: 15/63/255 по сложности. */
    val maxNumber: Int = when (difficulty) {
        1 -> 15
        2 -> 63
        else -> 255
    }

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

    private var previousNumber = 0

    /** Текущий вопрос, который нужно показать игроку. */
    var currentQuestion: BinaryQuestion = generateQuestion()
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
     * [BinaryQuestion.options] текущего вопроса
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

    private fun generateQuestion(): BinaryQuestion {
        var number = random.nextInt(1, maxNumber + 1)
        while (number == previousNumber) {
            number = random.nextInt(1, maxNumber + 1)
        }
        previousNumber = number

        val direction =
            if (random.nextBoolean()) QuestionDirection.BINARY_TO_DECIMAL
            else QuestionDirection.DECIMAL_TO_BINARY

        val numbers = (generateDistractors(number) + number).shuffled(random)
        val prompt: String
        val options: List<String>
        when (direction) {
            QuestionDirection.BINARY_TO_DECIMAL -> {
                prompt = BinaryNumbers.toBinary(number)
                options = numbers.map { it.toString() }
            }

            QuestionDirection.DECIMAL_TO_BINARY -> {
                prompt = number.toString()
                options = numbers.map { BinaryNumbers.toBinary(it) }
            }
        }
        return BinaryQuestion(
            direction = direction,
            number = number,
            prompt = prompt,
            options = options,
            correctIndex = numbers.indexOf(number)
        )
    }

    /**
     * Подбирает три различных правдоподобных дистрактора для [number]
     * в пределах 1..[maxNumber]: соседние числа (±1/±2), число с
     * перевёрнутой битовой строкой и числа с одним изменённым битом.
     */
    private fun generateDistractors(number: Int): List<Int> {
        val candidates = mutableSetOf<Int>()

        // Соседние значения: ±1, ±2.
        for (offset in 1..2) {
            candidates += number + offset
            candidates += number - offset
        }

        // Перевёрнутая битовая строка (110 → 011 = 11 без ведущего нуля).
        val reversedBits = BinaryNumbers.toBinary(number).reversed().trimStart('0')
        if (reversedBits.isNotEmpty()) {
            candidates += BinaryNumbers.fromBinary(reversedBits)
        }

        // Один изменённый бит: каждый существующий разряд и один разряд
        // над старшим (кандидаты вне 1..maxNumber отсеются ниже).
        val bitLength = BinaryNumbers.toBinary(number).length
        for (bit in 0..bitLength) {
            candidates += number xor (1 shl bit)
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
    }
}
