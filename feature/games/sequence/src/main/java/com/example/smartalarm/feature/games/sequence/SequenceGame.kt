package com.example.smartalarm.feature.games.sequence

import kotlin.random.Random

/**
 * Чистое состояние партии «Продолжи ряд»: текущий ряд, прогресс и победа.
 * Не зависит от Android; вся случайность — через переданный [Random].
 *
 * Партия состоит из [totalRounds] рядов (3/4/5 для сложностей 1/2/3,
 * см. [SequenceGenerator.roundsFor]). Правильный ответ открывает следующий
 * ряд; после последнего партия завершена ([isFinished]). Ошибки не меняют
 * ряд, только увеличиваются в счётчике [mistakes].
 *
 * @param difficulty сложность 1..3 (значения вне диапазона приводятся к нему).
 * @param random источник случайности (Random(seed) — для детерминизма).
 */
class SequenceGame(difficulty: Int, private val random: Random) {

    private val difficulty = difficulty.coerceIn(1, 3)

    /** Сколько рядов нужно решить для победы. */
    val totalRounds: Int = SequenceGenerator.roundsFor(this.difficulty)

    /** Сколько рядов уже решено. */
    var roundsSolved: Int = 0
        private set

    /** Сколько неверных ответов дано за партию. */
    var mistakes: Int = 0
        private set

    /** Текущее задание. После победы остаётся последним решённым. */
    var currentTask: SequenceTask = SequenceGenerator.generate(this.difficulty, random)
        private set

    /** true, когда решены все [totalRounds] рядов. */
    val isFinished: Boolean
        get() = roundsSolved >= totalRounds

    /** Номер текущего ряда для интерфейса: 1..[totalRounds]. */
    val currentRoundNumber: Int
        get() = (roundsSolved + 1).coerceAtMost(totalRounds)

    /**
     * Принимает ответ игрока на текущий ряд.
     *
     * Верный ответ засчитывает ряд и, если партия не завершена, генерирует
     * следующий. Неверный увеличивает [mistakes]. После победы вызовы
     * игнорируются (возвращается false).
     *
     * @param option выбранный вариант.
     * @return true, если ответ верный и был засчитан.
     */
    fun submitAnswer(option: Int): Boolean {
        if (isFinished) return false
        return if (currentTask.isCorrect(option)) {
            roundsSolved++
            if (!isFinished)
                currentTask = SequenceGenerator.generate(difficulty, random)
            true
        } else {
            mistakes++
            false
        }
    }
}
