package com.example.smartalarm.feature.games.clock

/**
 * Время на аналоговом циферблате: часы 1..12 и минуты 0..59.
 *
 * Чистая модель без Android-зависимостей: считает углы стрелок,
 * форматирует себя в строку «ЧЧ:ММ» и умеет строить «похожие» времена
 * для дистракторов (сдвиг, перепутанные стрелки, зеркало).
 *
 * @property hours часы в 12-часовом формате, от 1 до 12
 * @property minutes минуты, от 0 до 59
 * @throws IllegalArgumentException если часы или минуты вне допустимого диапазона
 */
data class ClockTime(val hours: Int, val minutes: Int) {

    init {
        require(hours in 1..12) { "hours must be in 1..12, got $hours" }
        require(minutes in 0..59) { "minutes must be in 0..59, got $minutes" }
    }

    /**
     * Угол минутной стрелки в градусах по часовой стрелке от отметки «12»:
     * 6° за каждую минуту (0..354).
     */
    val minuteHandAngle: Float
        get() = minutes * DEGREES_PER_MINUTE

    /**
     * Угол часовой стрелки в градусах по часовой стрелке от отметки «12»:
     * 30° за каждый час плюс 0,5° за каждую минуту — часовая стрелка
     * смещается пропорционально минутам, как на настоящих часах.
     */
    val hourHandAngle: Float
        get() = (hours % 12) * DEGREES_PER_HOUR + minutes * HOUR_HAND_DEGREES_PER_MINUTE

    /** Число минут, прошедших от «12:00» (0..719). Удобно для арифметики на циферблате. */
    val totalMinutes: Int
        get() = (hours % 12) * 60 + minutes

    /**
     * Время, сдвинутое на [deltaMinutes] минут (можно отрицательное значение);
     * циферблат замкнут, поэтому результат всегда валиден.
     */
    fun shiftedBy(deltaMinutes: Int): ClockTime = fromTotalMinutes(totalMinutes + deltaMinutes)

    /**
     * Время с «перепутанными» стрелками: часовая стрелка встаёт туда, куда
     * указывала минутная, и наоборот. Если минуты не кратны 5, номер часа
     * берётся округлением вниз (минутная стрелка между часовыми отметками).
     */
    fun withSwappedHands(): ClockTime {
        val newHours = (minutes / 5).let { if (it == 0) 12 else it }
        val newMinutes = (hours % 12) * 5
        return ClockTime(newHours, newMinutes)
    }

    /**
     * Зеркальное время: обе стрелки отражаются относительно вертикальной
     * оси «12–6». Например, 4:40 превращается в 7:20.
     */
    fun mirrored(): ClockTime = fromTotalMinutes(HALF_DAY_MINUTES - totalMinutes)

    /** Строка «ЧЧ:ММ» с ведущими нулями, например «07:05» или «12:30». */
    override fun toString(): String = "%02d:%02d".format(hours, minutes)

    companion object {
        /** Градусов поворота минутной стрелки за одну минуту. */
        const val DEGREES_PER_MINUTE = 6f

        /** Градусов поворота часовой стрелки за один час. */
        const val DEGREES_PER_HOUR = 30f

        /** Градусов смещения часовой стрелки за одну минуту. */
        const val HOUR_HAND_DEGREES_PER_MINUTE = 0.5f

        /** Минут в половине суток — один полный оборот часовой стрелки. */
        const val HALF_DAY_MINUTES = 720

        /**
         * Строит время по числу минут от «12:00». Аргумент нормализуется
         * по модулю [HALF_DAY_MINUTES], поэтому допустимы любые значения,
         * включая отрицательные.
         */
        fun fromTotalMinutes(totalMinutes: Int): ClockTime {
            val normalized = totalMinutes.mod(HALF_DAY_MINUTES)
            val hours = normalized / 60
            return ClockTime(if (hours == 0) 12 else hours, normalized % 60)
        }
    }
}
