package com.example.smartalarm.feature.games.chain

/**
 * Один шаг цепочки: операция и её операнд.
 *
 * Чистая модель без зависимостей от Android.
 *
 * @property op операция шага.
 * @property operand операнд, к которому применяется операция.
 */
data class ChainStep(val op: ChainOp, val operand: Int) {

    /** Текст шага, как он показывается игроку, например «+ 7» или «× 2». */
    val text: String
        get() = "${op.symbol} $operand"

    /**
     * Применяет шаг к текущему значению цепочки.
     *
     * @param value текущее значение цепочки.
     * @return новое значение цепочки.
     */
    fun apply(value: Int): Int = op.apply(value, operand)
}
