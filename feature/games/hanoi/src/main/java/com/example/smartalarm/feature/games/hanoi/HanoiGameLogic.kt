package com.example.smartalarm.feature.games.hanoi

/**
 * Результат попытки хода в «Ханойской башне».
 */
enum class MoveResult {
    /** Ход выполнен, игра продолжается. */
    OK,

    /** Ход невозможен: состояние игры не изменилось, ход не засчитан. */
    ILLEGAL,

    /** Ход выполнен и вся башня собрана на третьем стержне — победа. */
    WIN
}

/**
 * Чистая логика игры «Ханойская башня»: три стержня и [diskCount] дисков.
 *
 * Диски обозначаются размерами `1..diskCount` (1 — самый маленький).
 * В начале игры все диски лежат на стержне 0 по убыванию размера
 * (внизу самый большой). Цель — перенести всю башню на стержень 2,
 * при этом класть больший диск на меньший запрещено.
 *
 * Класс не зависит от Android и покрыт JVM-юнит-тестами.
 *
 * @property diskCount число дисков (не меньше 1)
 */
class HanoiGameLogic(val diskCount: Int) {

    init {
        require(diskCount >= 1) { "diskCount must be >= 1, got $diskCount" }
    }

    /** Стержни: списки размеров дисков снизу вверх. */
    private val rods: List<MutableList<Int>> = listOf(
        (diskCount downTo 1).toMutableList(),
        mutableListOf(),
        mutableListOf()
    )

    /** Число выполненных (легальных) ходов; нелегальные попытки не считаются. */
    var moveCount: Int = 0
        private set

    /** Минимально возможное число ходов для победы: `2^diskCount - 1`. */
    val minMoves: Int = (1 shl diskCount) - 1

    /** `true`, когда вся башня собрана на третьем стержне (индекс 2). */
    val isWon: Boolean
        get() = rods[2].size == diskCount

    /**
     * Возвращает размер верхнего диска стержня [rod] (0..2)
     * или `null`, если стержень пуст.
     */
    fun topDisk(rod: Int): Int? = rods[rod].lastOrNull()

    /**
     * Возвращает неизменяемый снимок состояния: три списка размеров
     * дисков снизу вверх. Используется UI для отрисовки.
     */
    fun rodsSnapshot(): List<List<Int>> = rods.map { it.toList() }

    /**
     * Пытается переложить верхний диск со стержня [from] на стержень [to].
     *
     * Ход нелегален (возвращается [MoveResult.ILLEGAL], состояние и
     * [moveCount] не меняются), если:
     * - индексы вне диапазона 0..2 или `from == to`;
     * - стержень [from] пуст;
     * - верхний диск [to] меньше перекладываемого.
     *
     * Иначе диск перекладывается, [moveCount] увеличивается на 1 и
     * возвращается [MoveResult.WIN], если после хода [isWon],
     * или [MoveResult.OK].
     */
    fun move(from: Int, to: Int): MoveResult {
        if (from !in 0..2 || to !in 0..2 || from == to) return MoveResult.ILLEGAL
        val disk = rods[from].lastOrNull() ?: return MoveResult.ILLEGAL
        val target = rods[to].lastOrNull()
        if (target != null && target < disk) return MoveResult.ILLEGAL

        rods[from].removeAt(rods[from].size - 1)
        rods[to].add(disk)
        moveCount++
        return if (isWon) MoveResult.WIN else MoveResult.OK
    }

    companion object {
        /**
         * Число дисков для уровня сложности будильника:
         * 1 → 3 диска, 2 → 4 диска, 3 (и всё остальное) → 5 дисков.
         */
        fun disksForDifficulty(difficulty: Int): Int = when (difficulty) {
            1 -> 3
            2 -> 4
            else -> 5
        }
    }
}
