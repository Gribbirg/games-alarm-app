package com.example.smartalarm.feature.games.clock

import kotlin.random.Random

/**
 * Один раунд игры «Который час»: время на циферблате и варианты ответа.
 *
 * @property time правильное время, показанное стрелками
 * @property options перемешанные варианты ответа (ровно [ClockGame.OPTIONS_COUNT]),
 * все уникальны, и ровно один из них равен [time]
 */
data class ClockRound(val time: ClockTime, val options: List<ClockTime>)

/**
 * Чистая логика игры «Который час» (game id 18) без Android-зависимостей.
 *
 * Игрок должен [totalRounds] раз правильно прочитать время на циферблате,
 * выбрав его из [OPTIONS_COUNT] цифровых вариантов. Сложность задаёт шаг
 * минут загаданного времени и число раундов:
 *
 * | Сложность | Шаг минут | Раундов |
 * |-----------|-----------|---------|
 * | 1         | 30        | 3       |
 * | 2         | 15        | 4       |
 * | 3         | 5         | 5       |
 *
 * Дистракторы строятся «похожими» на правильный ответ: перепутанные стрелки,
 * сдвиг на ±шаг сложности, зеркальное время; если похожих вариантов не хватает
 * (совпали с правильным или между собой), недостающие добираются случайными
 * валидными временами с шагом 5 минут. Ошибка не продвигает игру: генерируется
 * новый раунд с тем же номером, чтобы ответ нельзя было подобрать перебором.
 *
 * @param difficulty уровень сложности 1..3 (прочие значения трактуются как 1)
 * @param random источник случайности; передайте [Random] с seed
 * для воспроизводимых партий (например, в тестах)
 */
class ClockGame(difficulty: Int, private val random: Random = Random.Default) {

    /** Шаг минут загаданного времени: 30, 15 или 5 в зависимости от сложности. */
    val minuteStep: Int = when (difficulty) {
        2 -> 15
        3 -> 5
        else -> 30
    }

    /** Сколько раундов нужно пройти для победы: 3, 4 или 5. */
    val totalRounds: Int = when (difficulty) {
        2 -> 4
        3 -> 5
        else -> 3
    }

    /** Номер текущего раунда, начиная с 1. После победы равен [totalRounds] + 1. */
    var roundNumber: Int = 1
        private set

    /** Текущий раунд: время на циферблате и варианты ответа. */
    var currentRound: ClockRound = generateRound(previous = null)
        private set

    /** true, когда все раунды пройдены и игра выиграна. */
    val isFinished: Boolean
        get() = roundNumber > totalRounds

    /**
     * Обрабатывает выбор игрока.
     *
     * Правильный ответ переводит игру к следующему раунду (или завершает её);
     * при ошибке номер раунда не меняется, но задача заменяется новой.
     *
     * @param option выбранный игроком вариант ответа
     * @return true, если ответ правильный
     * @throws IllegalStateException если игра уже завершена
     */
    fun answer(option: ClockTime): Boolean {
        check(!isFinished) { "Game is already finished" }
        val isCorrect = option == currentRound.time
        if (isCorrect) {
            roundNumber++
        }
        if (!isFinished) {
            currentRound = generateRound(previous = currentRound.time)
        }
        return isCorrect
    }

    private fun generateRound(previous: ClockTime?): ClockRound {
        var time = randomTime(minuteStep)
        while (time == previous) {
            time = randomTime(minuteStep)
        }

        val options = mutableListOf(time)
        val lookAlikes = listOf(
            time.withSwappedHands(),
            time.shiftedBy(minuteStep),
            time.shiftedBy(-minuteStep),
            time.mirrored()
        )
        for (candidate in lookAlikes) {
            if (options.size == OPTIONS_COUNT) break
            if (candidate !in options) options.add(candidate)
        }
        while (options.size < OPTIONS_COUNT) {
            val candidate = randomTime(FALLBACK_MINUTE_STEP)
            if (candidate !in options) options.add(candidate)
        }
        options.shuffle(random)
        return ClockRound(time, options)
    }

    private fun randomTime(step: Int): ClockTime = ClockTime(
        hours = (1..12).random(random),
        minutes = (0 until 60 / step).random(random) * step
    )

    companion object {
        /** Число вариантов ответа в каждом раунде. */
        const val OPTIONS_COUNT = 4

        /** Шаг минут запасных случайных дистракторов. */
        private const val FALLBACK_MINUTE_STEP = 5
    }
}
