package com.example.smartalarm.feature.games.truefalse

import kotlin.random.Random

/** Результат ответа игрока на текущее утверждение. */
enum class AnswerResult {
    /** Ответ правильный, серия выросла, игра продолжается. */
    CORRECT,

    /** Ответ неправильный, серия сброшена в ноль. */
    WRONG,

    /** Ответ правильный и серия достигла цели — победа. */
    WIN
}

/**
 * Чистая логика партии «Верно или нет»: текущее утверждение, серия правильных
 * ответов подряд и условие победы. Не зависит от Android.
 *
 * Для победы нужно [targetStreak] правильных ответов подряд
 * (5/7/10 для сложности 1/2/3); ошибка сбрасывает серию в ноль.
 *
 * @param difficulty уровень сложности 1..3 (значения вне диапазона приводятся к границе)
 * @param random источник случайности для генератора утверждений
 */
class TrueFalseGame(
    difficulty: Int,
    random: Random = Random.Default
) {

    /** Длина серии правильных ответов подряд, необходимая для победы. */
    val targetStreak: Int = when (difficulty.coerceIn(1, 3)) {
        1 -> 5
        2 -> 7
        else -> 10
    }

    private val generator = StatementGenerator(difficulty, random)

    /** Текущая длина серии правильных ответов подряд. */
    var streak: Int = 0
        private set

    /** Утверждение, на которое игрок отвечает сейчас. */
    var currentStatement: Statement = generator.generate()
        private set

    /**
     * Обрабатывает ответ игрока на [currentStatement].
     *
     * При правильном ответе серия растёт; если она достигла [targetStreak],
     * возвращается [AnswerResult.WIN] (новое утверждение не генерируется).
     * Иначе генерируется следующее утверждение. При ошибке серия сбрасывается
     * в ноль и тоже генерируется следующее утверждение.
     *
     * @param userSaysTrue `true`, если игрок нажал «Верно», `false` — «Неверно»
     * @return исход ответа
     */
    fun answer(userSaysTrue: Boolean): AnswerResult {
        if (userSaysTrue == currentStatement.isTrue) {
            streak++
            if (streak >= targetStreak) return AnswerResult.WIN
            currentStatement = generator.generate()
            return AnswerResult.CORRECT
        }
        streak = 0
        currentStatement = generator.generate()
        return AnswerResult.WRONG
    }
}
