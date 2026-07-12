package com.example.smartalarm.feature.games.reaction

/**
 * Чистая функция движения бегунка по шкале — треугольная волна.
 *
 * Позиция зависит только от прошедшего времени, поэтому логика движения
 * детерминированна и тестируема без Android: UI лишь тикает таймером
 * и спрашивает позицию для текущего момента.
 */
object ReactionWave {

    /**
     * Позиция бегунка на шкале в момент [elapsedMs].
     *
     * Треугольная волна с периодом [periodMs]: за первую половину периода
     * бегунок равномерно движется от 0 к 1, за вторую — обратно от 1 к 0,
     * затем цикл повторяется:
     *
     * - `position(0) == 0.0`
     * - `position(periodMs / 2) == 1.0`
     * - `position(periodMs) == 0.0`
     *
     * @param elapsedMs миллисекунды с начала движения; отрицательные значения
     *   приводятся к 0
     * @param periodMs период полного цикла «туда-обратно», строго больше 0
     * @return позиция на шкале в диапазоне 0.0..1.0
     */
    fun position(elapsedMs: Long, periodMs: Long): Double {
        require(periodMs > 0) { "periodMs must be positive, got $periodMs" }
        val phase = (elapsedMs.coerceAtLeast(0L) % periodMs).toDouble() / periodMs
        return if (phase <= 0.5) 2 * phase else 2 * (1 - phase)
    }
}
