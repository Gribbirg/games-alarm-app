package com.example.smartalarm.feature.games.reaction

import kotlin.random.Random

/**
 * Целевая зона на шкале 0..1: отрезок `[start, end]`, в который нужно
 * попасть бегунком.
 *
 * Не содержит зависимостей от Android; случайность генерации идёт через
 * переданный [Random], что делает логику детерминированной в тестах.
 *
 * @property start левая граница зоны, 0.0..1.0
 * @property end правая граница зоны, `start`..1.0
 */
data class ReactionZone(val start: Double, val end: Double) {

    init {
        require(start in 0.0..1.0) { "start must be in 0..1, got $start" }
        require(end in start..1.0) { "end must be in start..1, got $end" }
    }

    /** Ширина зоны (доля шкалы). */
    val width: Double
        get() = end - start

    /**
     * Проверяет попадание бегунка в зону.
     *
     * Границы включаются: позиция ровно на [start] или [end] — попадание.
     *
     * @param position позиция бегунка 0.0..1.0
     * @return true, если позиция внутри зоны
     */
    fun contains(position: Double): Boolean = position in start..end

    companion object {
        /**
         * Генерирует зону заданной ширины в случайном месте шкалы.
         *
         * Левая граница выбирается равномерно из `[0, 1 - width)`, так что
         * зона целиком лежит внутри шкалы 0..1.
         *
         * @param width ширина зоны, из диапазона (0.0, 1.0]
         * @param random источник случайности
         */
        fun generate(width: Double, random: Random): ReactionZone {
            require(width > 0.0 && width <= 1.0) { "width must be in (0..1], got $width" }
            val start = random.nextDouble() * (1.0 - width)
            return ReactionZone(start, (start + width).coerceAtMost(1.0))
        }
    }
}
