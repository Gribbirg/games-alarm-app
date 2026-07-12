package com.example.smartalarm.feature.games.spelling

/**
 * Готовый к показу вопрос игры «Как пишется?»: перемешанные варианты
 * написания одного слова, из которых ровно один правильный.
 *
 * @property options варианты написания в порядке показа (2–4 штуки)
 * @property correctIndex индекс правильного варианта в [options]
 */
data class SpellingQuestion(
    val options: List<String>,
    val correctIndex: Int
)
