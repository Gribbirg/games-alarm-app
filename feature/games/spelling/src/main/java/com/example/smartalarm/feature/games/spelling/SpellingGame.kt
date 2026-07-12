package com.example.smartalarm.feature.games.spelling

import kotlin.random.Random

/**
 * Чистая логика игры «Как пишется?» (без зависимостей от Android).
 *
 * Правила:
 * - каждый вопрос — варианты написания одного слова (правильный + заранее
 *   составленные неправильные), перемешанные случайно;
 * - для победы нужно [targetCorrect] верных ответов;
 * - при любом ответе (верном или неверном) показывается новое слово;
 * - записи в рамках игры не повторяются; если пул исчерпан (много ошибок),
 *   использованные записи сбрасываются и слова идут по новому кругу.
 *
 * @property entries пул записей, из которого выбираются вопросы (не пустой)
 * @property targetCorrect число верных ответов, необходимое для победы (> 0)
 * @param random источник случайности (передаётся параметром, чтобы логику
 * можно было детерминированно тестировать с фиксированным seed)
 */
class SpellingGame(
    private val entries: List<SpellingEntry>,
    val targetCorrect: Int,
    private val random: Random = Random.Default
) {

    /** Число верных ответов, данных с начала игры. */
    var correctCount: Int = 0
        private set

    /** Число ошибок, совершённых с начала игры. */
    var mistakeCount: Int = 0
        private set

    /** Победил ли уже игрок. */
    val isWon: Boolean
        get() = correctCount >= targetCorrect

    private val usedIndices = mutableSetOf<Int>()

    /** Текущий вопрос, который нужно показать игроку. */
    var currentQuestion: SpellingQuestion
        private set

    init {
        require(entries.isNotEmpty()) { "Пул записей не должен быть пустым" }
        require(targetCorrect > 0) { "Число верных ответов должно быть положительным" }
        currentQuestion = makeQuestion()
    }

    /**
     * Обрабатывает ответ игрока на [currentQuestion].
     *
     * При [AnswerResult.CORRECT] и [AnswerResult.WRONG] в [currentQuestion]
     * уже лежит следующий вопрос; при [AnswerResult.WIN] вопрос не меняется.
     *
     * @param optionIndex индекс выбранного варианта в `currentQuestion.options`
     * @return результат ответа
     */
    fun answer(optionIndex: Int): AnswerResult {
        if (isWon) return AnswerResult.WIN

        return if (optionIndex == currentQuestion.correctIndex) {
            correctCount++
            if (isWon) {
                AnswerResult.WIN
            } else {
                currentQuestion = makeQuestion()
                AnswerResult.CORRECT
            }
        } else {
            mistakeCount++
            currentQuestion = makeQuestion()
            AnswerResult.WRONG
        }
    }

    private fun makeQuestion(): SpellingQuestion {
        if (usedIndices.size == entries.size) usedIndices.clear()
        val available = entries.indices.filter { it !in usedIndices }
        val entryIndex = available[random.nextInt(available.size)]
        usedIndices.add(entryIndex)

        val entry = entries[entryIndex]
        val options = (entry.wrong + entry.correct).shuffled(random)
        return SpellingQuestion(options, options.indexOf(entry.correct))
    }

    companion object {
        /** Штраф очков за одну ошибку (как в игре-калькуляторе). */
        const val MISTAKE_PENALTY = 10

        /**
         * Число верных ответов для победы на уровне сложности:
         * 1 → 4, 2 → 5, 3 → 6.
         *
         * @param difficulty уровень сложности 1..3 (вне диапазона приводится
         * к ближайшей границе)
         */
        fun targetCorrectFor(difficulty: Int): Int = 3 + difficulty.coerceIn(1, 3)

        /**
         * Бонус очков за завершение игры (по образцу игры-калькулятора):
         * `(600 − прошло_секунд) × difficulty`. Может быть отрицательным,
         * если игра длилась дольше 10 минут.
         *
         * @param elapsedSeconds сколько секунд шла игра
         * @param difficulty уровень сложности 1..3
         */
        fun finishBonus(elapsedSeconds: Long, difficulty: Int): Int =
            ((600 - elapsedSeconds) * difficulty).toInt()
    }
}
