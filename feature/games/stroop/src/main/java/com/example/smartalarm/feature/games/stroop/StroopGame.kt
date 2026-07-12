package com.example.smartalarm.feature.games.stroop

import kotlin.random.Random

/**
 * Чистая логика игры «Цвет и слово» (эффект Струпа): генерация раундов,
 * проверка ответов, подсчёт прогресса и игровых очков.
 *
 * Не содержит зависимостей от Android — вся случайность идёт через
 * переданный [random], что делает логику детерминированной в тестах.
 *
 * Очки: +[SCORE_PER_CORRECT] за верный ответ, [SCORE_PER_MISTAKE] за ошибку;
 * бонус за скорость прохождения добавляет ViewModel при победе.
 *
 * @param difficulty уровень сложности 1..3 (см. [StroopSettings.forLevel])
 * @param random источник случайности
 */
class StroopGame(
    difficulty: Int,
    private val random: Random = Random.Default,
) {
    /** Параметры текущего уровня сложности. */
    val settings: StroopSettings = StroopSettings.forLevel(difficulty)

    /** Число верных ответов с начала игры. */
    var correctCount: Int = 0
        private set

    /** Число ошибок с начала игры. */
    var mistakeCount: Int = 0
        private set

    /** Накопленные игровые очки (без бонуса за время). */
    var score: Int = 0
        private set

    /** Текущий раунд. */
    var currentRound: StroopRound = generateRound()
        private set

    /** true, когда набрано [StroopSettings.roundsToWin] верных ответов. */
    val isWon: Boolean
        get() = correctCount >= settings.roundsToWin

    /**
     * Принимает ответ игрока на текущий раунд.
     *
     * Верный ответ увеличивает счётчик прогресса и очки; ошибка отнимает очки.
     * В обоих случаях генерируется новый раунд (после ошибки — чтобы нельзя
     * было победить перебором кнопок), если игра ещё не выиграна.
     *
     * @param answer выбранный игроком цвет
     * @return true, если ответ был верным
     */
    fun answer(answer: StroopColor): Boolean {
        val correct = currentRound.isCorrect(answer)
        if (correct) {
            correctCount++
            score += SCORE_PER_CORRECT
        } else {
            mistakeCount++
            score += SCORE_PER_MISTAKE
        }
        if (!isWon)
            currentRound = generateRound()
        return correct
    }

    /**
     * Генерирует новый раунд: значение слова и цвет краски всегда различны,
     * варианты ответа уникальны, перемешаны и содержат правильный ответ
     * вместе с «ловушкой» (вторым цветом пары слово/краска).
     */
    private fun generateRound(): StroopRound {
        val pool = settings.colorPool
        val word = pool.random(random)
        val ink = (pool - word).random(random)
        val mode =
            if (random.nextDouble() < settings.meaningChance) StroopQuestionMode.MEANING
            else StroopQuestionMode.INK

        val fillers = (pool - word - ink).shuffled(random)
        val options = (listOf(word, ink) + fillers)
            .take(settings.optionsCount)
            .shuffled(random)

        return StroopRound(word, ink, mode, options)
    }

    companion object {
        /** Очки за верный ответ. */
        const val SCORE_PER_CORRECT = 10

        /** Очки за ошибку (отрицательные). */
        const val SCORE_PER_MISTAKE = -10
    }
}
