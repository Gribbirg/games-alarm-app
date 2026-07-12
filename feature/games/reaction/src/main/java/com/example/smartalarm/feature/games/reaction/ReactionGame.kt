package com.example.smartalarm.feature.games.reaction

import kotlin.random.Random

/**
 * Чистая логика игры «Поймай момент»: целевая зона, приём нажатий «Стоп»,
 * подсчёт попаданий/промахов, игровых очков и определение победы.
 *
 * Не содержит зависимостей от Android. Позицию бегунка в момент нажатия
 * вычисляет вызывающая сторона (через [ReactionWave.position]) и передаёт
 * в [press]; вся случайность идёт через переданный [random], что делает
 * логику детерминированной в тестах.
 *
 * Очки: +[SCORE_PER_HIT] за попадание, [SCORE_PER_MISS] за промах;
 * бонус за скорость прохождения добавляет ViewModel при победе.
 *
 * @param difficulty уровень сложности 1..3 (см. [ReactionSettings.forLevel])
 * @param random источник случайности для генерации зон
 */
class ReactionGame(
    difficulty: Int,
    private val random: Random = Random.Default,
) {
    /** Параметры текущего уровня сложности. */
    val settings: ReactionSettings = ReactionSettings.forLevel(difficulty)

    /** Текущая целевая зона; после каждого попадания генерируется заново. */
    var zone: ReactionZone = ReactionZone.generate(settings.zoneWidth, random)
        private set

    /** Число попаданий с начала игры. */
    var hitCount: Int = 0
        private set

    /** Число промахов с начала игры. */
    var missCount: Int = 0
        private set

    /** Накопленные игровые очки (без бонуса за время). */
    var score: Int = 0
        private set

    /** true, когда набрано [ReactionSettings.hitsToWin] попаданий. */
    val isWon: Boolean
        get() = hitCount >= settings.hitsToWin

    /**
     * Принимает нажатие «Стоп» при позиции бегунка [position].
     *
     * Попадание (позиция внутри [zone], границы включительно) увеличивает
     * счётчик попаданий, даёт очки и перегенерирует зону в случайном месте
     * (если игра ещё не выиграна). Промах отнимает очки и зону не меняет.
     * Нажатия после победы игнорируются.
     *
     * @param position позиция бегунка 0.0..1.0 в момент нажатия
     * @return true, если нажатие было попаданием
     */
    fun press(position: Double): Boolean {
        if (isWon) return false

        val hit = zone.contains(position)
        if (hit) {
            hitCount++
            score += SCORE_PER_HIT
            if (!isWon)
                zone = ReactionZone.generate(settings.zoneWidth, random)
        } else {
            missCount++
            score += SCORE_PER_MISS
        }
        return hit
    }

    companion object {
        /** Очки за попадание. */
        const val SCORE_PER_HIT = 10

        /** Очки за промах (отрицательные). */
        const val SCORE_PER_MISS = -10
    }
}
