package com.example.smartalarm.feature.games.counter

import kotlin.random.Random

/** Минимальное число целевых эмодзи на поле. */
const val MIN_TARGET_COUNT = 3

/** Сколько вариантов ответа предлагается игроку. */
const val OPTIONS_COUNT = 4

/**
 * Один раунд игры «Сосчитай»: квадратная сетка, заполненная вперемешку
 * эмодзи нескольких видов, и вопрос «Сколько на поле [targetEmoji]?»
 * с четырьмя вариантами ответа.
 *
 * @property gridSide сторона квадратной сетки (4, 5 или 6).
 * @property targetEmoji целевой эмодзи, экземпляры которого нужно сосчитать.
 * @property cells содержимое всех ячеек по порядку (по строкам слева направо);
 * размер списка — `gridSide * gridSide`.
 * @property correctCount сколько раз [targetEmoji] встречается в [cells]
 * (правильный ответ).
 * @property options четыре уникальных варианта ответа в случайном порядке;
 * ровно один из них равен [correctCount], все неотрицательны.
 */
data class CounterRound(
    val gridSide: Int,
    val targetEmoji: String,
    val cells: List<String>,
    val correctCount: Int,
    val options: List<Int>,
) {
    /** Общее число ячеек сетки. */
    val cellCount: Int get() = gridSide * gridSide
}

/** Результат выбора варианта ответа. */
enum class AnswerResult {
    /** Ответ неверный: ошибка засчитана, поле перегенерировано заново. */
    WRONG,

    /** Ответ верный, начат следующий раунд. */
    NEXT_ROUND,

    /** Ответ верный в последнем раунде — игра пройдена. */
    WIN,
}

/**
 * Чистая логика игры «Сосчитай» (game id = 13), без зависимостей от Android.
 *
 * Параметры по сложности:
 * 1 — сетка 4×4, 3 раунда, 3 вида эмодзи из трёх разных групп (виды заметно
 * отличаются); 2 — 5×5, 4 раунда, 4 вида из двух групп (по два похожих);
 * 3 — 6×6, 5 раундов, 5 видов из одной группы (все виды похожи друг на друга).
 *
 * В каждом раунде целевой эмодзи встречается от [MIN_TARGET_COUNT] раз
 * до строго меньше половины поля, каждый вид-дистрактор — хотя бы один раз.
 * После неверного ответа поле генерируется заново с другим целевым эмодзи,
 * поэтому перебор четырёх вариантов не работает; номер раунда при этом
 * не меняется. Целевой эмодзи не повторяется два раунда подряд.
 *
 * @param difficulty сложность 1..3 (значения вне диапазона приводятся к нему).
 * @param random источник случайности; подставьте [Random] с seed для
 * детерминированных тестов.
 */
class CounterGame(
    difficulty: Int,
    private val random: Random = Random.Default,
) {
    /** Сложность игры, приведённая к диапазону 1..3. */
    val difficulty: Int = difficulty.coerceIn(1, 3)

    /** Сторона квадратной сетки (4, 5 или 6). */
    val gridSide: Int = this.difficulty + 3

    /** Сколько раундов нужно пройти для победы (3, 4 или 5). */
    val totalRounds: Int = this.difficulty + 2

    /** Сколько видов эмодзи на поле, включая целевой (3, 4 или 5). */
    val kindCount: Int = this.difficulty + 2

    /** Номер текущего раунда, начиная с 1. */
    var roundNumber: Int = 1
        private set

    /** Сколько неверных ответов дано за игру. */
    var mistakes: Int = 0
        private set

    /** true, когда все раунды пройдены; дальнейшие ответы игнорируются. */
    var isFinished: Boolean = false
        private set

    /** Текущий раунд. */
    var currentRound: CounterRound = generateRound(previousTarget = null)
        private set

    /**
     * Обрабатывает выбор игроком варианта ответа [option].
     *
     * @return [AnswerResult.WRONG] при неверном ответе — счётчик [mistakes]
     * растёт и генерируется новое поле (номер раунда не меняется);
     * [AnswerResult.NEXT_ROUND] при верном ответе в промежуточном раунде;
     * [AnswerResult.WIN] при верном ответе в последнем раунде.
     * После победы всегда возвращает [AnswerResult.WIN], ничего не меняя.
     */
    fun answer(option: Int): AnswerResult {
        if (isFinished)
            return AnswerResult.WIN

        if (option != currentRound.correctCount) {
            mistakes++
            currentRound = generateRound(previousTarget = currentRound.targetEmoji)
            return AnswerResult.WRONG
        }

        return if (roundNumber == totalRounds) {
            isFinished = true
            AnswerResult.WIN
        } else {
            roundNumber++
            currentRound = generateRound(previousTarget = currentRound.targetEmoji)
            AnswerResult.NEXT_ROUND
        }
    }

    /**
     * Генерирует раунд: выбирает виды эмодзи (целевой — первый и не равный
     * [previousTarget]), заполняет поле и строит варианты ответа.
     */
    private fun generateRound(previousTarget: String?): CounterRound {
        var kinds: List<String>
        do {
            kinds = pickKinds()
        } while (kinds.first() == previousTarget)

        val target = kinds.first()
        val distractors = kinds.drop(1)
        val cellCount = gridSide * gridSide

        // От MIN_TARGET_COUNT до строго меньше половины поля.
        val targetCount = random.nextInt(MIN_TARGET_COUNT, cellCount / 2)

        val cells = mutableListOf<String>()
        repeat(targetCount) { cells.add(target) }
        distractors.forEach { cells.add(it) } // каждый дистрактор минимум один раз
        repeat(cellCount - cells.size) { cells.add(distractors.random(random)) }
        cells.shuffle(random)

        return CounterRound(
            gridSide = gridSide,
            targetEmoji = target,
            cells = cells,
            correctCount = targetCount,
            options = generateOptions(targetCount),
        )
    }

    /**
     * Выбирает [kindCount] видов эмодзи для раунда (в случайном порядке):
     * сложность 1 — по одному из трёх разных групп, 2 — по два из двух групп,
     * 3 — все пять из одной группы.
     */
    private fun pickKinds(): List<String> = when (difficulty) {
        1 -> COUNTER_EMOJI_GROUPS.shuffled(random).take(3)
            .map { it.emojis.random(random) }
            .shuffled(random)

        2 -> COUNTER_EMOJI_GROUPS.shuffled(random).take(2)
            .flatMap { it.emojis.shuffled(random).take(2) }
            .shuffled(random)

        else -> COUNTER_EMOJI_GROUPS.random(random)
            .emojis.shuffled(random).take(KINDS_ON_MAX_DIFFICULTY)
    }

    /**
     * Строит [OPTIONS_COUNT] уникальных вариантов ответа вокруг [correct]:
     * правильный плюс три из шести соседей `correct ± 1..3`. Так как
     * `correct` не меньше [MIN_TARGET_COUNT] (= 3), все кандидаты неотрицательны.
     */
    private fun generateOptions(correct: Int): List<Int> {
        val candidates = ((correct - 3)..(correct + 3))
            .filter { it != correct && it >= 0 }
        return (candidates.shuffled(random).take(OPTIONS_COUNT - 1) + correct)
            .shuffled(random)
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
fun computeCounterScore(mistakes: Int, elapsedSeconds: Long, difficulty: Int): Int =
    (-10L * mistakes + (600L - elapsedSeconds) * difficulty).toInt()
