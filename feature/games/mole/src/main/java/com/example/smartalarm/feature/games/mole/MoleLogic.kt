package com.example.smartalarm.feature.games.mole

import kotlin.random.Random

/**
 * Параметры сложности игры «Поймай крота».
 *
 * @property gridSize сторона квадратной сетки нор (сетка gridSize x gridSize)
 * @property showTimeMs сколько миллисекунд крот виден в норе, прежде чем спрятаться
 * @property targetHits сколько попаданий нужно для победы
 */
data class MoleDifficulty(
    val gridSize: Int,
    val showTimeMs: Long,
    val targetHits: Int,
) {

    /** Общее количество нор на поле. */
    val holeCount: Int get() = gridSize * gridSize

    companion object {
        /**
         * Возвращает параметры для уровня сложности.
         *
         * Уровни: 1 — 3x3, крот виден 1200 мс, 5 попаданий;
         * 2 — 3x3, 900 мс, 7 попаданий; 3 — 4x4, 650 мс, 10 попаданий.
         * Значения вне диапазона приводятся к ближайшему уровню.
         *
         * @param level уровень сложности 1..3
         * @return параметры сложности
         */
        fun forLevel(level: Int): MoleDifficulty = when (level.coerceIn(1, 3)) {
            1 -> MoleDifficulty(gridSize = 3, showTimeMs = 1200L, targetHits = 5)
            2 -> MoleDifficulty(gridSize = 3, showTimeMs = 900L, targetHits = 7)
            else -> MoleDifficulty(gridSize = 4, showTimeMs = 650L, targetHits = 10)
        }
    }
}

/** Результат нажатия на нору. */
enum class MoleTapResult {
    /** Попадание: крот был в этой норе. */
    HIT,

    /** Промах: нора пуста (или крота сейчас вообще нет на поле). */
    MISS,
}

/**
 * Чистая логика игры «Поймай крота»: где крот, счёт попаданий и промахов,
 * условие победы. Не знает ничего про Android — тайминги показа/скрытия
 * крота задаёт вызывающая сторона (фрагмент).
 *
 * @property difficulty параметры сложности
 * @param random источник случайности (подставляется с seed в тестах)
 */
class MoleGame(
    val difficulty: MoleDifficulty,
    private val random: Random = Random.Default,
) {

    /** Индекс норы с кротом (0 until [MoleDifficulty.holeCount]) или [NO_MOLE], если крот спрятан. */
    var currentHole: Int = NO_MOLE
        private set

    /** Нора, где крот появлялся в прошлый раз (чтобы не выскочить там же снова). */
    private var lastHole: Int = NO_MOLE

    /** Количество пойманных кротов. */
    var hits: Int = 0
        private set

    /** Количество промахов (нажатий мимо крота). */
    var misses: Int = 0
        private set

    /** true, когда набрано [MoleDifficulty.targetHits] попаданий. */
    val isWon: Boolean get() = hits >= difficulty.targetHits

    /**
     * Показывает крота в случайной норе, гарантированно не совпадающей
     * с предыдущей (в том числе если крот успел спрятаться сам).
     *
     * @return индекс норы, где появился крот
     */
    fun showMole(): Int {
        currentHole = nextHole(lastHole)
        lastHole = currentHole
        return currentHole
    }

    /** Прячет крота (крот «ушёл сам» — без штрафа, счётчики не меняются). */
    fun hideMole() {
        currentHole = NO_MOLE
    }

    /**
     * Обрабатывает нажатие на нору.
     *
     * Попадание засчитывается, только если крот сейчас именно в этой норе;
     * при попадании крот сразу прячется. Любое другое нажатие (пустая нора
     * или крота нет на поле) — промах.
     *
     * @param hole индекс нажатой норы
     * @return [MoleTapResult.HIT] или [MoleTapResult.MISS]
     */
    fun tap(hole: Int): MoleTapResult =
        if (currentHole != NO_MOLE && hole == currentHole) {
            hits++
            currentHole = NO_MOLE
            MoleTapResult.HIT
        } else {
            misses++
            MoleTapResult.MISS
        }

    /**
     * Выбирает случайную нору, не равную [previous].
     *
     * Равномерно выбирает из holeCount - 1 нор (или из всех, если
     * предыдущей норы ещё не было).
     */
    private fun nextHole(previous: Int): Int {
        if (previous == NO_MOLE) return random.nextInt(difficulty.holeCount)
        val candidate = random.nextInt(difficulty.holeCount - 1)
        return if (candidate >= previous) candidate + 1 else candidate
    }

    companion object {
        /** Значение [currentHole], когда крота нет на поле. */
        const val NO_MOLE = -1

        /** Штраф к очкам за один промах. */
        const val MISS_PENALTY = 10
    }
}

/**
 * Бонус очков за победу (как в остальных играх):
 * (600 − прошедшие_секунды) × уровень_сложности.
 *
 * @param elapsedSeconds секунд с начала игры
 * @param difficultyLevel уровень сложности 1..3
 * @return бонус (может быть отрицательным, если игра длилась дольше 10 минут)
 */
fun finishBonus(elapsedSeconds: Long, difficultyLevel: Int): Int =
    ((600 - elapsedSeconds) * difficultyLevel).toInt()
