package com.example.smartalarm.feature.games.weekday

import kotlin.random.Random

/**
 * Состояние одной партии игры «День недели». Чистая логика без Android.
 *
 * Партия — последовательность вопросов [WeekdayQuestion]. Для победы нужно
 * дать [questionsToWin] верных ответов (3/4/5 в зависимости от сложности).
 * После любого ответа (и верного, и ошибочного) генерируется новый вопрос,
 * пока партия не выиграна; ошибки счётчик верных ответов не сбрасывают.
 *
 * @param difficulty уровень сложности 1..3
 * @param random источник случайности; передайте `Random(seed)` для воспроизводимости
 */
class WeekdayGame(
    val difficulty: Int,
    private val random: Random = Random.Default
) {

    /** Сколько верных ответов нужно для победы на этой сложности. */
    val questionsToWin: Int = questionsToWin(difficulty)

    /** Сколько верных ответов уже дано. */
    var correctAnswers: Int = 0
        private set

    /** Сколько ошибок сделано за партию. */
    var mistakes: Int = 0
        private set

    /** Текущий вопрос. */
    var currentQuestion: WeekdayQuestion = WeekdayQuestionFactory.generate(difficulty, random)
        private set

    /** true, когда набрано [questionsToWin] верных ответов. */
    val isWon: Boolean
        get() = correctAnswers >= questionsToWin

    /**
     * Принимает ответ игрока на [currentQuestion].
     *
     * При верном ответе увеличивает [correctAnswers], при ошибке — [mistakes].
     * Если партия ещё не выиграна, генерирует следующий вопрос. После победы
     * вызовы игнорируются (возвращается false, состояние не меняется).
     *
     * @param answer выбранный игроком день недели
     * @return true, если ответ верный
     */
    fun answer(answer: Weekday): Boolean {
        if (isWon) return false
        val correct = answer == currentQuestion.correctAnswer
        if (correct) correctAnswers++ else mistakes++
        if (!isWon) {
            currentQuestion = WeekdayQuestionFactory.generate(difficulty, random)
        }
        return correct
    }

    companion object {
        /**
         * Количество верных ответов для победы на сложности [difficulty]:
         * 3, 4 или 5; всё вне диапазона 1..3 трактуется как лёгкий уровень.
         */
        fun questionsToWin(difficulty: Int): Int = when (difficulty) {
            2 -> 4
            3 -> 5
            else -> 3
        }
    }
}
