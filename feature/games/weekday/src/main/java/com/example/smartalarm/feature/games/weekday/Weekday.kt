package com.example.smartalarm.feature.games.weekday

/**
 * День недели с русским названием (в нижнем регистре, именительный падеж).
 *
 * Порядок объявления — с понедельника по воскресенье, поэтому [ordinal]
 * можно использовать как номер дня недели (0 — понедельник, 6 — воскресенье)
 * в модульной арифметике.
 */
enum class Weekday(
    /** Русское название дня недели в нижнем регистре («среда»). */
    val displayName: String
) {
    MONDAY("понедельник"),
    TUESDAY("вторник"),
    WEDNESDAY("среда"),
    THURSDAY("четверг"),
    FRIDAY("пятница"),
    SATURDAY("суббота"),
    SUNDAY("воскресенье");

    /**
     * Возвращает день недели, отстоящий от текущего на [offset] дней.
     *
     * Работает по модулю 7 и корректно обрабатывает любые целые смещения,
     * в том числе отрицательные («назад») и по модулю больше недели:
     * `WEDNESDAY.shifted(9) == FRIDAY`, `MONDAY.shifted(-4) == THURSDAY`,
     * `x.shifted(7 * k) == x` для любого k.
     *
     * @param offset смещение в днях; положительное — вперёд, отрицательное — назад
     */
    fun shifted(offset: Int): Weekday {
        val index = (((ordinal + offset) % DAYS_IN_WEEK) + DAYS_IN_WEEK) % DAYS_IN_WEEK
        return entries[index]
    }

    companion object {
        /** Количество дней в неделе. */
        const val DAYS_IN_WEEK = 7
    }
}
