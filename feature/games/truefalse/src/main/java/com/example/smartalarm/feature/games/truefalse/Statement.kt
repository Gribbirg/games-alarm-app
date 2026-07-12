package com.example.smartalarm.feature.games.truefalse

/**
 * Арифметическое утверждение, показываемое игроку в игре «Верно или нет».
 *
 * @property text текст утверждения, например «12 + 35 = 47» или «17 > 4 × 4»
 * @property isTrue фактическая истинность утверждения (правильный ответ игрока)
 */
data class Statement(
    val text: String,
    val isTrue: Boolean
)
