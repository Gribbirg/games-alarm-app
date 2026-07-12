package com.example.smartalarm.feature.games.stroop

/**
 * Параметры игры «Цвет и слово» для одного уровня сложности.
 *
 * @property roundsToWin сколько верных ответов нужно для победы
 * @property colorPool пул цветов, из которого генерируются раунды
 * @property meaningChance вероятность (0.0..1.0) инвертированного задания
 *   «выбери значение слова» вместо обычного «выбери цвет краски»
 * @property optionsCount число вариантов ответа (кнопок) в раунде
 */
data class StroopSettings(
    val roundsToWin: Int,
    val colorPool: List<StroopColor>,
    val meaningChance: Double,
    val optionsCount: Int,
) {
    companion object {
        /**
         * Возвращает параметры для уровня сложности.
         *
         * - 1: 6 верных ответов, 4 цвета (красный, синий, зелёный, жёлтый),
         *   только обычные задания;
         * - 2: 8 верных ответов, 5 цветов (+ фиолетовый), только обычные задания;
         * - 3: 10 верных ответов, 6 цветов (+ оранжевый), с вероятностью 30%
         *   раунд инвертированный («что означает слово?»).
         *
         * Всегда 4 варианта ответа.
         *
         * @param difficulty уровень сложности; значения вне 1..3 приводятся
         *   к ближайшей границе диапазона
         */
        fun forLevel(difficulty: Int): StroopSettings = when (difficulty.coerceIn(1, 3)) {
            1 -> StroopSettings(
                roundsToWin = 6,
                colorPool = listOf(
                    StroopColor.RED,
                    StroopColor.BLUE,
                    StroopColor.GREEN,
                    StroopColor.YELLOW,
                ),
                meaningChance = 0.0,
                optionsCount = 4,
            )

            2 -> StroopSettings(
                roundsToWin = 8,
                colorPool = listOf(
                    StroopColor.RED,
                    StroopColor.BLUE,
                    StroopColor.GREEN,
                    StroopColor.YELLOW,
                    StroopColor.PURPLE,
                ),
                meaningChance = 0.0,
                optionsCount = 4,
            )

            else -> StroopSettings(
                roundsToWin = 10,
                colorPool = StroopColor.entries.toList(),
                meaningChance = 0.3,
                optionsCount = 4,
            )
        }
    }
}
