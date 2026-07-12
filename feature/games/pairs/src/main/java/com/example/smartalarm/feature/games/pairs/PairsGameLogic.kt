package com.example.smartalarm.feature.games.pairs

import kotlin.random.Random

/**
 * Пул символов (эмодзи), из которого набираются пары для колоды.
 *
 * Содержит больше символов, чем нужно даже на максимальной сложности (8 пар),
 * поэтому набор карточек меняется от игры к игре.
 */
val PAIRS_SYMBOLS: List<String> = listOf(
    "🍎", "🍌", "🍇", "🍓", "🍋", "🥝",
    "🐶", "🐱", "🦊", "🐼", "🐸", "🐧",
)

/**
 * Возвращает число пар для уровня сложности.
 *
 * 1 — 3 пары (6 карт), 2 — 6 пар (12 карт), 3 — 8 пар (16 карт).
 * Неизвестная сложность трактуется как самая лёгкая.
 *
 * @param difficulty уровень сложности (1..3)
 * @return число пар в колоде
 */
fun pairsCountFor(difficulty: Int): Int = when (difficulty) {
    2 -> 6
    3 -> 8
    else -> 3
}

/**
 * Возвращает число столбцов игровой сетки для уровня сложности.
 *
 * 1 — сетка 2×3, 2 — 3×4, 3 — 4×4 (столбцы × строки).
 * Неизвестная сложность трактуется как самая лёгкая.
 *
 * @param difficulty уровень сложности (1..3)
 * @return число столбцов сетки
 */
fun pairsColumnsFor(difficulty: Int): Int = when (difficulty) {
    2 -> 3
    3 -> 4
    else -> 2
}

/**
 * Генерирует перемешанную колоду карточек для уровня сложности.
 *
 * Из [PAIRS_SYMBOLS] случайно выбираются [pairsCountFor] символов, каждый
 * кладётся в колоду ровно дважды, затем колода перемешивается.
 *
 * @param difficulty уровень сложности (1..3)
 * @param random источник случайности (передаётся для детерминизма в тестах)
 * @return список символов карточек; размер — удвоенное число пар
 */
fun generatePairsDeck(difficulty: Int, random: Random = Random.Default): List<String> {
    val symbols = PAIRS_SYMBOLS.shuffled(random).take(pairsCountFor(difficulty))
    return symbols.flatMap { listOf(it, it) }.shuffled(random)
}

/**
 * Результат хода — попытки открыть карточку в [PairsGame].
 */
sealed class PairsMoveResult {

    /**
     * Открыта первая карточка хода; ждём вторую.
     *
     * @property index индекс открытой карточки
     */
    data class FirstRevealed(val index: Int) : PairsMoveResult()

    /**
     * Открыта вторая карточка, символы совпали — пара остаётся открытой.
     *
     * @property first индекс первой карточки пары
     * @property second индекс второй карточки пары
     * @property isWin true, если это была последняя пара и игра выиграна
     */
    data class Match(val first: Int, val second: Int, val isWin: Boolean) : PairsMoveResult()

    /**
     * Открыта вторая карточка, символы не совпали.
     *
     * Обе карточки остаются открытыми, а новые ходы игнорируются, пока
     * не будет вызван [PairsGame.hideMismatched].
     *
     * @property first индекс первой карточки
     * @property second индекс второй карточки
     */
    data class Mismatch(val first: Int, val second: Int) : PairsMoveResult()

    /**
     * Клик проигнорирован: карточка уже открыта/найдена, индекс вне поля
     * или на поле показывается несовпавшая пара.
     */
    object Ignored : PairsMoveResult()
}

/**
 * Конечный автомат игры «Найди пару».
 *
 * Чистая логика без зависимостей от Android: хранит состояние каждой
 * карточки (закрыта / открыта в текущем ходе / найдена) и считает ошибки.
 * За один ход открываются две карточки; совпавшие остаются открытыми
 * навсегда, несовпавшие остаются на поле до вызова [hideMismatched]
 * (UI в это время показывает их игроку и блокирует ввод).
 *
 * @property cards колода: символ карточки по индексу; ожидается чётный размер,
 * где каждый символ встречается ровно дважды (см. [generatePairsDeck])
 */
class PairsGame(val cards: List<String>) {

    private val matched = BooleanArray(cards.size)

    /** Индекс первой открытой карточки текущего хода (null — ход не начат). */
    private var firstIndex: Int? = null

    /** Индекс второй карточки несовпавшей пары (null — несовпадение не показывается). */
    private var secondIndex: Int? = null

    /** Число ошибок — несовпавших пар — за игру. */
    var mistakes: Int = 0
        private set

    /** Общее число пар в колоде. */
    val totalPairsCount: Int
        get() = cards.size / 2

    /** Число уже найденных пар. */
    val matchedPairsCount: Int
        get() = matched.count { it } / 2

    /** true, если все пары найдены — игра выиграна. */
    val isWon: Boolean
        get() = cards.isNotEmpty() && matched.all { it }

    /** true, если на поле показывается несовпавшая пара и ввод должен быть заблокирован. */
    val hasPendingMismatch: Boolean
        get() = secondIndex != null

    /**
     * Проверяет, найдена ли карточка (входит в совпавшую пару).
     *
     * @param index индекс карточки
     * @return true, если карточка уже в найденной паре
     */
    fun isMatched(index: Int): Boolean = index in cards.indices && matched[index]

    /**
     * Проверяет, открыта ли карточка сейчас (найдена или участвует в текущем ходе).
     *
     * @param index индекс карточки
     * @return true, если карточку нужно показывать лицом вверх
     */
    fun isRevealed(index: Int): Boolean =
        isMatched(index) || index == firstIndex || index == secondIndex

    /**
     * Ход игрока — попытка открыть карточку.
     *
     * @param index индекс карточки
     * @return результат хода, см. [PairsMoveResult]
     */
    fun openCard(index: Int): PairsMoveResult {
        if (index !in cards.indices) return PairsMoveResult.Ignored
        if (hasPendingMismatch) return PairsMoveResult.Ignored
        if (matched[index] || index == firstIndex) return PairsMoveResult.Ignored

        val first = firstIndex
        return if (first == null) {
            firstIndex = index
            PairsMoveResult.FirstRevealed(index)
        } else if (cards[first] == cards[index]) {
            matched[first] = true
            matched[index] = true
            firstIndex = null
            PairsMoveResult.Match(first, index, isWon)
        } else {
            mistakes++
            secondIndex = index
            PairsMoveResult.Mismatch(first, index)
        }
    }

    /**
     * Закрывает показываемую несовпавшую пару и снимает блокировку ходов.
     *
     * UI вызывает это по таймеру после [PairsMoveResult.Mismatch].
     *
     * @return пара закрытых индексов или null, если несовпадение не показывалось
     */
    fun hideMismatched(): Pair<Int, Int>? {
        val first = firstIndex
        val second = secondIndex
        if (first == null || second == null) return null
        firstIndex = null
        secondIndex = null
        return first to second
    }
}
