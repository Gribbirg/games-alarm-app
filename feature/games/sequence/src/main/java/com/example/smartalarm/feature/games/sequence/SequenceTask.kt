package com.example.smartalarm.feature.games.sequence

/**
 * Тип числового ряда мини-игры «Продолжи ряд».
 *
 * Доступность типов зависит от сложности (см. [SequenceGenerator.typesFor]):
 * сложность 1 — только [ARITHMETIC]; сложность 2 добавляет [GEOMETRIC] и
 * [SQUARES]; сложность 3 добавляет [ALTERNATING] и [FIBONACCI_LIKE].
 */
enum class SequenceType {
    /** Арифметическая прогрессия: каждый член отличается на постоянный шаг. */
    ARITHMETIC,

    /** Геометрическая прогрессия: каждый член умножается на постоянное отношение. */
    GEOMETRIC,

    /** Последовательные квадраты натуральных чисел: k², (k+1)², (k+2)², … */
    SQUARES,

    /** Чередующиеся шаги: попеременно +a и −b (a > b, ряд в целом растёт). */
    ALTERNATING,

    /** Фибоначчи-подобный ряд: каждый член — сумма двух предыдущих. */
    FIBONACCI_LIKE
}

/**
 * Одно задание игры «Продолжи ряд»: показанные члены ряда, правильный
 * следующий член и четыре варианта ответа.
 *
 * Инварианты (обеспечиваются [SequenceGenerator]):
 * [options] содержит ровно 4 уникальных значения, одно из которых — [answer].
 *
 * @property type тип ряда.
 * @property terms показанные игроку члены ряда (4–5 чисел).
 * @property answer правильный следующий член ряда.
 * @property options четыре варианта ответа в случайном порядке.
 */
data class SequenceTask(
    val type: SequenceType,
    val terms: List<Int>,
    val answer: Int,
    val options: List<Int>
) {
    /** Текст задания для экрана, например «2, 5, 8, 11, ?». */
    val questionText: String = terms.joinToString(", ") + ", ?"

    /**
     * Проверяет вариант ответа.
     *
     * @param option выбранное игроком число.
     * @return true, если [option] — правильный следующий член ряда.
     */
    fun isCorrect(option: Int): Boolean = option == answer
}
