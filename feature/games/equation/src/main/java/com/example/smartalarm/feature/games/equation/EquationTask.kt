package com.example.smartalarm.feature.games.equation

/**
 * Одно уравнение с пропущенным элементом и вариантами ответа.
 *
 * Чистая модель без зависимостей от Android.
 *
 * @property text текст уравнения с символом `?` на месте пропущенного элемента,
 * например `12 + ? = 20` или `12 ? 3 = 4`.
 * @property answer верный ответ — то, что нужно подставить вместо `?`
 * (число в виде строки либо символ оператора).
 * @property options четыре уникальных варианта ответа в случайном порядке;
 * ровно один из них равен [answer].
 */
data class EquationTask(
    val text: String,
    val answer: String,
    val options: List<String>
)
