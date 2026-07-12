package com.example.smartalarm.feature.games.reaction

/**
 * Параметры игры «Поймай момент» для одного уровня сложности.
 *
 * @property hitsToWin сколько попаданий нужно для победы
 * @property zoneWidth ширина целевой зоны (доля шкалы 0..1)
 * @property periodMs период полного цикла бегунка «туда-обратно», мс
 */
data class ReactionSettings(
    val hitsToWin: Int,
    val zoneWidth: Double,
    val periodMs: Long,
) {
    companion object {
        /**
         * Возвращает параметры для уровня сложности.
         *
         * - 1: 3 попадания, зона 20% шкалы, период 2400 мс
         *   (проход края-в-край за 1,2 с);
         * - 2: 4 попадания, зона 14% шкалы, период 1800 мс (проход за 0,9 с);
         * - 3: 5 попаданий, зона 10% шкалы, период 1300 мс (проход за 0,65 с).
         *
         * @param difficulty уровень сложности; значения вне 1..3 приводятся
         *   к ближайшей границе диапазона
         */
        fun forLevel(difficulty: Int): ReactionSettings = when (difficulty.coerceIn(1, 3)) {
            1 -> ReactionSettings(hitsToWin = 3, zoneWidth = 0.20, periodMs = 2400)
            2 -> ReactionSettings(hitsToWin = 4, zoneWidth = 0.14, periodMs = 1800)
            else -> ReactionSettings(hitsToWin = 5, zoneWidth = 0.10, periodMs = 1300)
        }
    }
}
