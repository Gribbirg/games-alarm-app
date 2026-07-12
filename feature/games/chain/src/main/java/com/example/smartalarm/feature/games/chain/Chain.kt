package com.example.smartalarm.feature.games.chain

/**
 * Цепочка устного счёта: стартовое число и последовательность операций.
 *
 * Чистая модель без зависимостей от Android. Генератор ([ChainGenerator])
 * гарантирует, что стартовое число и все значения из [intermediateResults]
 * лежат в диапазоне 0..999.
 *
 * @property start стартовое число, которое показывается игроку первым.
 * @property steps операции цепочки в порядке показа.
 */
data class Chain(val start: Int, val steps: List<ChainStep>) {

    /** Значения цепочки после каждого шага (по одному на каждый элемент [steps]). */
    val intermediateResults: List<Int> = buildList {
        var value = start
        for (step in steps) {
            value = step.apply(value)
            add(value)
        }
    }

    /** Итог цепочки — число, которое должен ввести игрок. */
    val result: Int = intermediateResults.lastOrNull() ?: start
}
