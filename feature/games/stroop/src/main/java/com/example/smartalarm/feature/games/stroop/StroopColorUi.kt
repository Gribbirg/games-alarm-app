package com.example.smartalarm.feature.games.stroop

/**
 * Сопоставление [StroopColor] элементам отображения (UI-слой):
 * русское название и захардкоженные ARGB-цвета отрисовки.
 */

/** Русское название цвета для слова-задания и подписей кнопок. */
fun StroopColor.displayName(): String = when (this) {
    StroopColor.RED -> "КРАСНЫЙ"
    StroopColor.BLUE -> "СИНИЙ"
    StroopColor.GREEN -> "ЗЕЛЁНЫЙ"
    StroopColor.YELLOW -> "ЖЁЛТЫЙ"
    StroopColor.PURPLE -> "ФИОЛЕТОВЫЙ"
    StroopColor.ORANGE -> "ОРАНЖЕВЫЙ"
}

/** ARGB-цвет отрисовки (краска слова и фон кнопки варианта). */
fun StroopColor.argb(): Int = when (this) {
    StroopColor.RED -> 0xFFE53935.toInt()
    StroopColor.BLUE -> 0xFF1E88E5.toInt()
    StroopColor.GREEN -> 0xFF43A047.toInt()
    StroopColor.YELLOW -> 0xFFFBC02D.toInt()
    StroopColor.PURPLE -> 0xFF8E24AA.toInt()
    StroopColor.ORANGE -> 0xFFFB8C00.toInt()
}

/** Контрастный цвет текста поверх фона [argb] (для подписей кнопок). */
fun StroopColor.onArgb(): Int = when (this) {
    StroopColor.YELLOW, StroopColor.ORANGE -> 0xFF212121.toInt()
    else -> 0xFFFFFFFF.toInt()
}
