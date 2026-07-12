package com.example.smartalarm.feature.games.oddoneout

import kotlin.random.Random

/**
 * Один раунд игры «Найди лишнее»: квадратная сетка из одинаковых символов,
 * среди которых ровно один отличается.
 *
 * @property gridSide сторона квадратной сетки (3, 4 или 5).
 * @property baseSymbol символ, заполняющий все ячейки, кроме одной.
 * @property oddSymbol «лишний» символ, который нужно найти.
 * @property oddIndex индекс лишнего символа в сетке (0 until gridSide * gridSide,
 * по строкам слева направо).
 * @property pair пара пула, из которой собран раунд (нужна, чтобы не
 * повторять одну и ту же пару два раунда подряд).
 */
data class OddOneOutRound(
    val gridSide: Int,
    val baseSymbol: String,
    val oddSymbol: String,
    val oddIndex: Int,
    val pair: EmojiPair,
) {
    /** Общее число ячеек сетки. */
    val cellCount: Int get() = gridSide * gridSide

    /** Символы всех ячеек по порядку: [baseSymbol] везде, [oddSymbol] на [oddIndex]. */
    val symbols: List<String>
        get() = List(cellCount) { if (it == oddIndex) oddSymbol else baseSymbol }
}

/** Результат нажатия на ячейку сетки. */
enum class ClickResult {
    /** Нажата не та ячейка — ошибка, раунд продолжается. */
    WRONG,

    /** Лишний символ найден, начат следующий раунд. */
    NEXT_ROUND,

    /** Лишний символ найден в последнем раунде — игра пройдена. */
    WIN,
}

/**
 * Чистая логика игры «Найди лишнее» (game id = 8), без зависимостей от Android.
 *
 * Параметры по сложности: 1 — сетка 3×3 и 3 раунда (заметные отличия),
 * 2 — 4×4 и 4 раунда, 3 — 5×5 и 5 раундов (самые похожие пары).
 * Пары берутся из [EMOJI_PAIRS] с `similarity == difficulty`; одна и та же
 * пара не выпадает два раунда подряд.
 *
 * @param difficulty сложность 1..3 (значения вне диапазона приводятся к нему).
 * @param random источник случайности; подставьте [Random] с seed для
 * детерминированных тестов.
 */
class OddOneOutGame(
    difficulty: Int,
    private val random: Random = Random.Default,
) {
    /** Сложность игры, приведённая к диапазону 1..3. */
    val difficulty: Int = difficulty.coerceIn(1, 3)

    /** Сторона квадратной сетки. */
    val gridSide: Int = this.difficulty + 2

    /** Сколько раундов нужно пройти для победы. */
    val totalRounds: Int = this.difficulty + 2

    private val pool: List<EmojiPair> =
        EMOJI_PAIRS.filter { it.similarity == this.difficulty }

    /** Номер текущего раунда, начиная с 1. */
    var roundNumber: Int = 1
        private set

    /** Сколько ошибочных нажатий сделано за игру. */
    var mistakes: Int = 0
        private set

    /** true, когда все раунды пройдены; дальнейшие нажатия игнорируются. */
    var isFinished: Boolean = false
        private set

    /** Текущий раунд. */
    var currentRound: OddOneOutRound = generateRound(previousPair = null)
        private set

    /**
     * Обрабатывает нажатие на ячейку [index] текущей сетки.
     *
     * @return [ClickResult.WRONG] при ошибке (счётчик [mistakes] растёт),
     * [ClickResult.NEXT_ROUND] при верном нажатии в промежуточном раунде,
     * [ClickResult.WIN] при верном нажатии в последнем раунде.
     * После победы всегда возвращает [ClickResult.WIN], ничего не меняя.
     */
    fun onCellClicked(index: Int): ClickResult {
        if (isFinished)
            return ClickResult.WIN

        if (index != currentRound.oddIndex) {
            mistakes++
            return ClickResult.WRONG
        }

        return if (roundNumber == totalRounds) {
            isFinished = true
            ClickResult.WIN
        } else {
            roundNumber++
            currentRound = generateRound(previousPair = currentRound.pair)
            ClickResult.NEXT_ROUND
        }
    }

    private fun generateRound(previousPair: EmojiPair?): OddOneOutRound {
        val candidates =
            if (pool.size > 1 && previousPair != null) pool.filter { it != previousPair }
            else pool
        val pair = candidates[random.nextInt(candidates.size)]
        val swap = random.nextBoolean()
        return OddOneOutRound(
            gridSide = gridSide,
            baseSymbol = if (swap) pair.second else pair.first,
            oddSymbol = if (swap) pair.first else pair.second,
            oddIndex = random.nextInt(gridSide * gridSide),
            pair = pair,
        )
    }
}

/**
 * Итоговый счёт игры по образцу арифметики:
 * каждая ошибка стоит 10 очков, финиш добавляет (600 − прошло секунд) × сложность.
 *
 * @param mistakes число ошибочных нажатий за игру.
 * @param elapsedSeconds сколько секунд заняла игра.
 * @param difficulty сложность 1..3.
 */
fun computeOddOneOutScore(mistakes: Int, elapsedSeconds: Long, difficulty: Int): Int =
    (-10L * mistakes + (600L - elapsedSeconds) * difficulty).toInt()
