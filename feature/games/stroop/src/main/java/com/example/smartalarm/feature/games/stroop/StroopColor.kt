package com.example.smartalarm.feature.games.stroop

/**
 * Цвет, участвующий в игре «Цвет и слово».
 *
 * Чистая модель без привязки к Android: и «слово» (значение), и «краска»
 * (цвет отрисовки) раунда описываются элементами этого enum. Сопоставление
 * элемента конкретному ARGB-цвету и русскому названию выполняется
 * в UI-слое (см. `StroopColorUi.kt`).
 */
enum class StroopColor {
    RED,
    BLUE,
    GREEN,
    YELLOW,
    PURPLE,
    ORANGE,
}
