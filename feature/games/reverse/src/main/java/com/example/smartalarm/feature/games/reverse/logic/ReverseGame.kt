package com.example.smartalarm.feature.games.reverse.logic

import kotlin.math.abs
import kotlin.random.Random

/**
 * Считает, сколько букв общие у двух слов с учётом повторов
 * (размер пересечения мультимножеств букв).
 *
 * Например, у «сорока» и «корова» общие буквы к, о, о, р, а — результат 5.
 * Используется при подборе похожих слов-дистракторов.
 *
 * @param a первое слово
 * @param b второе слово
 * @return число общих букв с учётом повторов
 */
fun commonLetterCount(a: String, b: String): Int {
    val counts = mutableMapOf<Char, Int>()
    for (c in a) counts[c] = (counts[c] ?: 0) + 1
    var common = 0
    for (c in b) {
        val left = counts[c] ?: 0
        if (left > 0) {
            counts[c] = left - 1
            common++
        }
    }
    return common
}

/**
 * Длина общего начала двух слов (число совпадающих первых букв).
 *
 * Например, у «полка» и «палка» общее начало — «п», результат 1.
 *
 * @param a первое слово
 * @param b второе слово
 * @return длина общего префикса
 */
fun commonPrefixLength(a: String, b: String): Int {
    var i = 0
    while (i < a.length && i < b.length && a[i] == b[i]) i++
    return i
}

/** Результат ответа игрока в [ReverseGame.submitAnswer]. */
enum class AnswerResult {
    /** Ответ верный, загадано следующее слово. */
    CORRECT,

    /** Ответ неверный; загадано новое слово, прогресс не сбрасывается. */
    WRONG,

    /** Отгадано последнее слово — игра пройдена. */
    GAME_WON
}

/**
 * Один раунд игры «Наоборот»: загаданное слово и варианты ответа.
 *
 * @property answer загаданное слово (правильный ответ)
 * @property options варианты ответа в порядке показа игроку;
 * ровно [ReverseGame.OPTIONS_COUNT] уникальных слов, среди которых есть [answer]
 */
class ReverseRound(val answer: String, val options: List<String>) {

    /** Слово, показанное игроку, — [answer], записанное задом наперёд. */
    val displayed: String = answer.reversed()
}

/**
 * Чистая логика игры «Наоборот» — без зависимостей от Android.
 *
 * Игроку показывается русское слово, записанное задом наперёд
 * (например, «акорос»), и четыре варианта; нужно выбрать исходное слово
 * («сорока»). Для победы нужно [totalRounds] верных ответов. При ошибке
 * прогресс сохраняется, но загадывается новое слово. Загаданные слова
 * в рамках одной игры не повторяются; если словарь исчерпан (много ошибок),
 * список использованных сбрасывается, но одно и то же слово никогда
 * не выпадает два раза подряд.
 *
 * Дистракторы подбираются похожими на ответ: та же длина ±1, максимум
 * общих букв и общее начало (см. [commonLetterCount], [commonPrefixLength]);
 * из кандидатов исключается слово, совпадающее с показанным перевёртышем,
 * поэтому ни один вариант не равен слову на экране.
 *
 * @param difficulty уровень сложности 1..3 — определяет словарь
 * (см. [ReverseWords.forDifficulty]) и число верных ответов до победы
 * (см. [roundCountForDifficulty])
 * @param random источник случайности (передаётся параметром ради
 * детерминированности в тестах)
 * @param dictionary словарь слов; по умолчанию берётся по сложности,
 * параметр нужен для тестов
 */
class ReverseGame(
    difficulty: Int,
    private val random: Random = Random.Default,
    private val dictionary: List<String> = ReverseWords.forDifficulty(difficulty)
) {

    /** Сколько верных ответов нужно для победы. */
    val totalRounds: Int = roundCountForDifficulty(difficulty)

    /** Сколько верных ответов уже дано. */
    var correctCount: Int = 0
        private set

    private val usedAnswers = mutableSetOf<String>()
    private var lastAnswer: String? = null

    /** Текущий раунд (после победы — последний раунд игры). */
    var currentRound: ReverseRound
        private set

    init {
        require(dictionary.size >= maxOf(totalRounds, OPTIONS_COUNT)) {
            "В словаре ${dictionary.size} слов, а нужно не меньше " +
                    "${maxOf(totalRounds, OPTIONS_COUNT)}"
        }
        currentRound = newRound()
    }

    /** `true`, если дано [totalRounds] верных ответов. */
    val isGameWon: Boolean
        get() = correctCount >= totalRounds

    /**
     * Обрабатывает выбор варианта игроком.
     *
     * При верном ответе прогресс растёт и загадывается следующее слово;
     * при неверном — прогресс сохраняется, но слово меняется на новое.
     * После победы состояние не меняется.
     *
     * @param optionIndex индекс выбранного варианта в
     * [ReverseRound.options] текущего раунда
     * @return результат ответа, см. [AnswerResult]
     * @throws IllegalArgumentException если индекс вне диапазона вариантов
     */
    fun submitAnswer(optionIndex: Int): AnswerResult {
        if (isGameWon) return AnswerResult.GAME_WON
        require(optionIndex in currentRound.options.indices) {
            "Индекс варианта $optionIndex вне диапазона ${currentRound.options.indices}"
        }
        return if (currentRound.options[optionIndex] == currentRound.answer) {
            correctCount++
            if (isGameWon) {
                AnswerResult.GAME_WON
            } else {
                currentRound = newRound()
                AnswerResult.CORRECT
            }
        } else {
            currentRound = newRound()
            AnswerResult.WRONG
        }
    }

    /**
     * Загадывает новое слово, не встречавшееся в этой игре, и собирает
     * варианты: ответ плюс [DISTRACTOR_COUNT] дистракторов в случайном
     * порядке. При исчерпании словаря использованные слова сбрасываются,
     * но предыдущее слово не может выпасть снова сразу же.
     */
    private fun newRound(): ReverseRound {
        var available = dictionary.filterNot { it in usedAnswers }
        if (available.isEmpty()) {
            usedAnswers.clear()
            available = dictionary.filterNot { it == lastAnswer }
        }
        val answer = available.random(random)
        usedAnswers += answer
        lastAnswer = answer
        val options = (pickDistractors(answer) + answer).shuffled(random)
        return ReverseRound(answer, options)
    }

    /**
     * Подбирает [DISTRACTOR_COUNT] дистракторов, похожих на ответ.
     *
     * Кандидаты — слова словаря, кроме самого ответа и слова, совпадающего
     * с его перевёртышем (иначе вариант дублировал бы слово на экране).
     * Предпочитаются слова той же длины ±1; они ранжируются по числу общих
     * букв и общему началу, из [POOL_SIZE] лучших случайно берутся три —
     * так варианты остаются похожими, но не одними и теми же каждый раз.
     */
    private fun pickDistractors(answer: String): List<String> {
        val displayed = answer.reversed()
        val candidates = dictionary.filter { it != answer && it != displayed }
        val preferred = candidates.filter { abs(it.length - answer.length) <= 1 }
        val base = if (preferred.size >= DISTRACTOR_COUNT) preferred else candidates
        return base
            .shuffled(random)
            .sortedByDescending {
                commonLetterCount(answer, it) * 2 + commonPrefixLength(answer, it) * 3
            }
            .take(POOL_SIZE)
            .shuffled(random)
            .take(DISTRACTOR_COUNT)
    }

    companion object {
        /** Число вариантов ответа в раунде. */
        const val OPTIONS_COUNT = 4

        /** Число дистракторов среди вариантов. */
        const val DISTRACTOR_COUNT = OPTIONS_COUNT - 1

        /** Размер пула лучших кандидатов, из которого выбираются дистракторы. */
        private const val POOL_SIZE = 8

        /**
         * Число верных ответов до победы для уровня сложности:
         * 3 на первом уровне, 4 на втором, 5 на третьем.
         */
        fun roundCountForDifficulty(difficulty: Int): Int =
            difficulty.coerceIn(1, 3) + 2
    }
}
