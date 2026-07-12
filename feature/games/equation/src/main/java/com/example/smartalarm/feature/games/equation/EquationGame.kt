package com.example.smartalarm.feature.games.equation

import kotlin.random.Random

/** Результат обработки выбранного игроком варианта ответа. */
enum class AnswerResult {
    /** Ответ верный, но для победы нужно решить ещё уравнения. */
    CORRECT,

    /** Ответ неверный: прогресс не растёт, выдаётся новое уравнение. */
    WRONG,

    /** Ответ верный и решено нужное число уравнений — игра пройдена. */
    WIN
}

/**
 * Состояние одной партии игры «Уравнение»: текущее задание, прогресс и условие победы.
 *
 * Чистая логика без зависимостей от Android. Для победы нужно решить
 * [totalRounds] уравнений (3/4/5 для сложности 1/2/3). После ошибки
 * прогресс не сбрасывается, но выдаётся новое уравнение, чтобы ответ
 * нельзя было подобрать перебором четырёх кнопок.
 *
 * @param difficulty сложность 1..3 (значения вне диапазона приводятся к границам).
 * @param random источник случайности для генерации уравнений.
 */
class EquationGame(difficulty: Int, random: Random = Random.Default) {

    /** Сложность партии, приведённая к диапазону 1..3. */
    val difficulty = difficulty.coerceIn(1, 3)

    private val generator = EquationGenerator(random)

    /** Сколько уравнений нужно решить для победы. */
    val totalRounds = ROUNDS_BY_DIFFICULTY.getValue(this.difficulty)

    /** Сколько уравнений уже решено верно. */
    var solvedCount = 0
        private set

    /** Текущее задание. После победы не обновляется. */
    var currentTask: EquationTask = generator.generate(this.difficulty)
        private set

    /** Достигнута ли победа. */
    val isWon: Boolean
        get() = solvedCount >= totalRounds

    /**
     * Обрабатывает выбранный игроком вариант ответа.
     *
     * @param option текст выбранного варианта (один из [EquationTask.options]).
     * @return результат: верно, неверно или победа. После победы всегда
     * возвращает [AnswerResult.WIN], не меняя состояние.
     */
    fun submitAnswer(option: String): AnswerResult {
        if (isWon) return AnswerResult.WIN
        return if (option == currentTask.answer) {
            solvedCount++
            if (isWon) {
                AnswerResult.WIN
            } else {
                currentTask = generator.generate(difficulty)
                AnswerResult.CORRECT
            }
        } else {
            currentTask = generator.generate(difficulty)
            AnswerResult.WRONG
        }
    }

    companion object {
        private val ROUNDS_BY_DIFFICULTY = mapOf(1 to 3, 2 to 4, 3 to 5)
    }
}
