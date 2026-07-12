package com.example.smartalarm.feature.games.stroop

/**
 * Режим вопроса в раунде игры «Цвет и слово».
 */
enum class StroopQuestionMode {
    /** Обычное задание: выбрать цвет, КОТОРЫМ написано слово (цвет краски). */
    INK,

    /**
     * Инвертированное задание (только на сложности 3): выбрать цвет,
     * который слово ОЗНАЧАЕТ, игнорируя цвет краски.
     */
    MEANING,
}

/**
 * Один раунд игры «Цвет и слово».
 *
 * Инвариант: [word] всегда не равно [ink] — слово окрашено «неправильным»
 * цветом, в этом и состоит эффект Струпа.
 *
 * @property word цвет, название которого написано на экране (значение слова)
 * @property ink цвет краски, которым слово отрисовано
 * @property mode режим вопроса: спрашивается краска или значение
 * @property options варианты ответа для кнопок; уникальны, перемешаны
 *   и всегда содержат правильный ответ
 */
data class StroopRound(
    val word: StroopColor,
    val ink: StroopColor,
    val mode: StroopQuestionMode,
    val options: List<StroopColor>,
) {
    /** Правильный ответ раунда с учётом режима вопроса. */
    val correctAnswer: StroopColor
        get() = when (mode) {
            StroopQuestionMode.INK -> ink
            StroopQuestionMode.MEANING -> word
        }

    /**
     * Проверяет вариант ответа игрока.
     *
     * @param answer выбранный игроком цвет
     * @return true, если ответ соответствует режиму вопроса
     */
    fun isCorrect(answer: StroopColor): Boolean = answer == correctAnswer
}
