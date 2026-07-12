package com.example.smartalarm.feature.games.digits

import kotlin.random.Random

/**
 * Результат проверки ответа игрока.
 */
enum class AnswerResult {
    /** Верный ответ в последнем раунде — игра пройдена. */
    WIN,

    /** Верный ответ, начат следующий раунд с новым, более длинным числом. */
    NEXT_ROUND,

    /** Неверный ответ: раунд не меняется, загадано новое число той же длины. */
    WRONG
}

/**
 * Состояние партии «Запомни число»: текущий раунд, загаданное число и переходы
 * между раундами. Чистый Kotlin без Android-зависимостей — живёт во ViewModel
 * (переживает поворот экрана) и тестируется на JVM.
 *
 * @param difficulty сложность 1..3, определяет длины чисел по раундам
 * @param random источник случайности (передаётся для детерминизма в тестах)
 */
class DigitsGameEngine(
    private val difficulty: Int,
    private val random: Random = Random.Default
) {

    /** Номер текущего раунда, начиная с 0. */
    var round = 0
        private set

    /** Загаданное число текущего раунда (строка цифр без ведущего нуля). */
    var currentNumber = newNumber()
        private set

    /** Пройдена ли игра (верный ответ в последнем раунде). */
    var isWon = false
        private set

    /**
     * Проверяет ответ игрока и переводит игру в следующее состояние:
     * - верный ответ в последнем раунде — победа ([AnswerResult.WIN]);
     * - верный ответ — следующий раунд с числом на одну цифру длиннее
     *   ([AnswerResult.NEXT_ROUND]);
     * - неверный ответ — новое число той же длины, раунд не меняется
     *   ([AnswerResult.WRONG]).
     *
     * После победы всегда возвращает [AnswerResult.WIN], не меняя состояние.
     *
     * @param answer введённый игроком ответ
     * @return результат проверки
     */
    fun submitAnswer(answer: String): AnswerResult {
        if (isWon)
            return AnswerResult.WIN
        return if (DigitsLogic.isAnswerCorrect(currentNumber, answer)) {
            if (DigitsLogic.isLastRound(round)) {
                isWon = true
                AnswerResult.WIN
            } else {
                round++
                currentNumber = newNumber()
                AnswerResult.NEXT_ROUND
            }
        } else {
            currentNumber = newNumber()
            AnswerResult.WRONG
        }
    }

    private fun newNumber(): String =
        DigitsLogic.generateNumber(DigitsLogic.numberLength(difficulty, round), random)
}
