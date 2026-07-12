package com.example.smartalarm.feature.games.dice

import kotlin.random.Random

/** Id игры «Кубики» в `ALL_GAMES` (`:core:data`). */
const val DICE_GAME_ID = 31

/**
 * Один бросок (раунд) игры «Кубики».
 *
 * @property values значения выпавших костей по порядку, каждое 1..6.
 * @property sum правильный ответ: сумма значений с учётом специального
 * правила сложности 3 (чётные значения удвоены), см. [diceSum].
 */
data class DiceRound(
    val values: List<Int>,
    val sum: Int,
)

/** Результат проверки введённой игроком суммы. */
enum class AnswerResult {
    /** Поле пустое или не число: ошибка не засчитана, бросок не изменился. */
    EMPTY,

    /** Сумма неверная: ошибка засчитана, кости того же раунда переброшены. */
    WRONG,

    /** Сумма верная, начат следующий раунд. */
    NEXT_ROUND,

    /** Сумма верная в последнем раунде — игра пройдена. */
    WIN,
}

/**
 * Чистая логика игры «Кубики» (game id = [DICE_GAME_ID]), без зависимостей
 * от Android.
 *
 * В каждом раунде «выпадает» [diceCount] игральных костей; игрок должен
 * сложить их значения и ввести сумму. Параметры по сложности:
 * 1 — 3 кости и 3 раунда; 2 — 5 костей и 4 раунда; 3 — 7 костей и 5 раундов,
 * плюс специальное правило [doubleEven]: кости с чётным значением считаются
 * удвоенными (2 → 4, 4 → 8, 6 → 12).
 *
 * После неверного ответа кости того же раунда перебрасываются (новый бросок
 * гарантированно отличается от предыдущего), поэтому подбор суммы перебором
 * не работает; номер раунда при этом не меняется. Пустой или нечисловой ввод
 * ошибкой не считается и бросок не меняет.
 *
 * @param difficulty сложность 1..3 (значения вне диапазона приводятся к нему).
 * @param random источник случайности; подставьте [Random] с seed для
 * детерминированных тестов.
 */
class DiceGame(
    difficulty: Int,
    private val random: Random = Random.Default,
) {
    /** Сложность игры, приведённая к диапазону 1..3. */
    val difficulty: Int = difficulty.coerceIn(1, 3)

    /** Сколько костей выпадает в каждом раунде (3, 5 или 7). */
    val diceCount: Int = 2 * this.difficulty + 1

    /** Сколько раундов нужно пройти для победы (3, 4 или 5). */
    val totalRounds: Int = this.difficulty + 2

    /**
     * Специальное правило сложности 3: кости с чётным значением
     * считаются удвоенными.
     */
    val doubleEven: Boolean = this.difficulty == 3

    /** Номер текущего раунда, начиная с 1. */
    var roundNumber: Int = 1
        private set

    /** Сколько неверных ответов дано за игру. */
    var mistakes: Int = 0
        private set

    /** true, когда все раунды пройдены; дальнейшие ответы игнорируются. */
    var isFinished: Boolean = false
        private set

    /** Текущий бросок. */
    var currentRound: DiceRound = generateRound(previous = null)
        private set

    /**
     * Проверяет введённую игроком сумму [input].
     *
     * @return [AnswerResult.EMPTY], если [input] пуст или не парсится в число —
     * состояние игры не меняется; [AnswerResult.WRONG] при неверной сумме —
     * счётчик [mistakes] растёт и кости перебрасываются (номер раунда не
     * меняется); [AnswerResult.NEXT_ROUND] при верной сумме в промежуточном
     * раунде; [AnswerResult.WIN] при верной сумме в последнем раунде.
     * После победы всегда возвращает [AnswerResult.WIN], ничего не меняя.
     */
    fun answer(input: String): AnswerResult {
        if (isFinished)
            return AnswerResult.WIN

        val guess = input.trim().toIntOrNull() ?: return AnswerResult.EMPTY

        if (guess != currentRound.sum) {
            mistakes++
            currentRound = generateRound(previous = currentRound.values)
            return AnswerResult.WRONG
        }

        return if (roundNumber == totalRounds) {
            isFinished = true
            AnswerResult.WIN
        } else {
            roundNumber++
            currentRound = generateRound(previous = currentRound.values)
            AnswerResult.NEXT_ROUND
        }
    }

    /**
     * Бросает [diceCount] костей и считает правильную сумму; перебрасывает,
     * пока новый бросок совпадает с [previous], чтобы после ошибки или смены
     * раунда кости гарантированно изменились.
     */
    private fun generateRound(previous: List<Int>?): DiceRound {
        var values: List<Int>
        do {
            values = rollDice(diceCount, random)
        } while (values == previous)
        return DiceRound(values, diceSum(values, doubleEven))
    }
}

/**
 * Итоговый счёт игры по образцу арифметики:
 * каждая ошибка стоит 10 очков, финиш добавляет (600 − прошло секунд) × сложность.
 *
 * @param mistakes число неверных ответов за игру.
 * @param elapsedSeconds сколько секунд заняла игра.
 * @param difficulty сложность 1..3.
 */
fun computeDiceScore(mistakes: Int, elapsedSeconds: Long, difficulty: Int): Int =
    (-10L * mistakes + (600L - elapsedSeconds) * difficulty).toInt()
