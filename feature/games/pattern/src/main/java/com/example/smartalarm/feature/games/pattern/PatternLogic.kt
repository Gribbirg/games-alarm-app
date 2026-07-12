package com.example.smartalarm.feature.games.pattern

import kotlin.random.Random

/**
 * Пул эмодзи игры «Закономерность»: визуально хорошо различимые символы,
 * из которых собираются паттерны и посторонние варианты ответа.
 *
 * Все элементы уникальны; пул заведомо больше максимального числа различных
 * символов в паттерне (4), поэтому посторонний вариант находится всегда.
 */
val EMOJI_POOL: List<String> = listOf(
    "🔵", "🔺", "⭐", "🍎", "🌙", "❤️", "🍀", "⚡",
    "🐟", "🎈", "🍩", "🔔", "🌸", "🐝", "⚽", "🎁",
)

/**
 * Шаблон повторяющегося паттерна.
 *
 * Буквы А, Б, В, Г кодируются индексами 0..3: например, шаблон «АБАВ» — это
 * [periodIndices] = `[0, 1, 0, 2]`. Конкретные эмодзи подставляются на место
 * индексов при генерации раунда, при этом все буквы получают РАЗНЫЕ символы.
 *
 * @property name человекочитаемое имя шаблона («АБ», «ААБ», «АБВГ»…).
 * @property periodIndices один период паттерна в виде индексов букв;
 * индексы образуют диапазон 0 until [symbolCount] без пропусков.
 */
data class PatternTemplate(
    val name: String,
    val periodIndices: List<Int>,
) {
    /** Длина периода паттерна. */
    val period: Int get() = periodIndices.size

    /** Сколько различных символов нужно для паттерна. */
    val symbolCount: Int get() = periodIndices.max() + 1
}

/** Шаблоны сложности 1: период 2. */
val DIFFICULTY_1_TEMPLATES: List<PatternTemplate> = listOf(
    PatternTemplate("АБ", listOf(0, 1)),
)

/** Шаблоны сложности 2: период 3. */
val DIFFICULTY_2_TEMPLATES: List<PatternTemplate> = listOf(
    PatternTemplate("АБВ", listOf(0, 1, 2)),
    PatternTemplate("ААБ", listOf(0, 0, 1)),
    PatternTemplate("АББ", listOf(0, 1, 1)),
)

/** Шаблоны сложности 3: период 4. */
val DIFFICULTY_3_TEMPLATES: List<PatternTemplate> = listOf(
    PatternTemplate("АБВГ", listOf(0, 1, 2, 3)),
    PatternTemplate("ААББ", listOf(0, 0, 1, 1)),
    PatternTemplate("АБАВ", listOf(0, 1, 0, 2)),
)

/**
 * Возвращает список шаблонов для сложности [difficulty]
 * (значение вне 1..3 приводится к границам диапазона).
 */
fun templatesFor(difficulty: Int): List<PatternTemplate> =
    when (difficulty.coerceIn(1, 3)) {
        1 -> DIFFICULTY_1_TEMPLATES
        2 -> DIFFICULTY_2_TEMPLATES
        else -> DIFFICULTY_3_TEMPLATES
    }

/**
 * Один раунд игры «Закономерность»: показанный кусок паттерна и 4 варианта
 * продолжения.
 *
 * @property template шаблон паттерна раунда.
 * @property symbols эмодзи, подставленные на место букв шаблона:
 * `symbols[i]` — символ буквы с индексом `i`; все символы различны.
 * @property visibleCount сколько элементов паттерна показано игроку
 * (минимум два полных периода, плюс 0–2 элемента).
 * @property options 4 уникальных варианта ответа в случайном порядке:
 * правильный, другие символы паттерна и хотя бы один посторонний.
 */
data class PatternRound(
    val template: PatternTemplate,
    val symbols: List<String>,
    val visibleCount: Int,
    val options: List<String>,
) {
    /** Длина периода паттерна. */
    val period: Int get() = template.period

    /** Символ паттерна на позиции [position] (позиции нумеруются с 0). */
    fun symbolAt(position: Int): String =
        symbols[template.periodIndices[position % period]]

    /** Показанная игроку часть ряда: первые [visibleCount] элементов паттерна. */
    val visibleSequence: List<String> get() = List(visibleCount) { symbolAt(it) }

    /** Правильный ответ — элемент паттерна, следующий за показанными. */
    val answer: String get() = symbolAt(visibleCount)
}

/** Результат выбора варианта ответа. */
enum class AnswerResult {
    /** Выбран неверный вариант: ошибка засчитана, раунд перегенерирован. */
    WRONG,

    /** Верный ответ в промежуточном раунде — начат следующий раунд. */
    NEXT_ROUND,

    /** Верный ответ в последнем раунде — игра пройдена. */
    WIN,
}

/**
 * Чистая логика игры «Закономерность» (game id = 24), без зависимостей
 * от Android.
 *
 * Игроку показывают ряд эмодзи, построенный по повторяющемуся шаблону
 * (см. [templatesFor]), и 4 варианта следующего элемента. Число раундов —
 * `difficulty + 2` (3/4/5). При ошибке раунд генерируется заново, поэтому
 * перебор вариантов не помогает пройти игру. Шаблон не повторяется два
 * раунда подряд, если на этой сложности шаблонов больше одного.
 *
 * @param difficulty сложность 1..3 (значения вне диапазона приводятся к нему).
 * @param random источник случайности; подставьте [Random] с seed для
 * детерминированных тестов.
 */
class PatternGame(
    difficulty: Int,
    private val random: Random = Random.Default,
) {
    /** Сложность игры, приведённая к диапазону 1..3. */
    val difficulty: Int = difficulty.coerceIn(1, 3)

    /** Сколько раундов нужно пройти для победы. */
    val totalRounds: Int = this.difficulty + 2

    private val templates: List<PatternTemplate> = templatesFor(this.difficulty)

    /** Номер текущего раунда, начиная с 1. */
    var roundNumber: Int = 1
        private set

    /** Сколько ошибочных ответов сделано за игру. */
    var mistakes: Int = 0
        private set

    /** true, когда все раунды пройдены; дальнейшие ответы игнорируются. */
    var isFinished: Boolean = false
        private set

    /** Текущий раунд. */
    var currentRound: PatternRound = generateRound(previousTemplate = null)
        private set

    /**
     * Обрабатывает выбор варианта с индексом [optionIndex]
     * (0 until 4, индекс в [PatternRound.options]).
     *
     * @return [AnswerResult.WRONG] при ошибке — счётчик [mistakes] растёт,
     * а раунд генерируется заново (перебор вариантов не работает);
     * [AnswerResult.NEXT_ROUND] при верном ответе в промежуточном раунде;
     * [AnswerResult.WIN] при верном ответе в последнем раунде.
     * После победы всегда возвращает [AnswerResult.WIN], ничего не меняя.
     */
    fun onOptionClicked(optionIndex: Int): AnswerResult {
        if (isFinished)
            return AnswerResult.WIN

        if (currentRound.options[optionIndex] != currentRound.answer) {
            mistakes++
            currentRound = generateRound(previousTemplate = currentRound.template)
            return AnswerResult.WRONG
        }

        return if (roundNumber == totalRounds) {
            isFinished = true
            AnswerResult.WIN
        } else {
            roundNumber++
            currentRound = generateRound(previousTemplate = currentRound.template)
            AnswerResult.NEXT_ROUND
        }
    }

    private fun generateRound(previousTemplate: PatternTemplate?): PatternRound {
        val candidates =
            if (templates.size > 1 && previousTemplate != null)
                templates.filter { it != previousTemplate }
            else
                templates
        val template = candidates[random.nextInt(candidates.size)]

        val symbols = EMOJI_POOL.shuffled(random).take(template.symbolCount)
        val visibleCount = 2 * template.period + random.nextInt(3)

        val answer = symbols[template.periodIndices[visibleCount % template.period]]
        val options = generateOptions(answer, symbols)

        return PatternRound(
            template = template,
            symbols = symbols,
            visibleCount = visibleCount,
            options = options,
        )
    }

    /**
     * Собирает 4 уникальных варианта: правильный ответ, до двух других
     * символов паттерна и посторонние эмодзи из пула (минимум один).
     */
    private fun generateOptions(answer: String, patternSymbols: List<String>): List<String> {
        val options = mutableListOf(answer)

        for (symbol in patternSymbols.filter { it != answer }.shuffled(random)) {
            if (options.size >= OPTIONS_COUNT - 1) break
            options.add(symbol)
        }

        for (symbol in EMOJI_POOL.filter { it !in patternSymbols }.shuffled(random)) {
            if (options.size >= OPTIONS_COUNT) break
            options.add(symbol)
        }

        return options.shuffled(random)
    }

    companion object {
        /** Число вариантов ответа в каждом раунде. */
        const val OPTIONS_COUNT = 4
    }
}

/**
 * Итоговый счёт игры по образцу арифметики:
 * каждая ошибка стоит 10 очков, финиш добавляет (600 − прошло секунд) × сложность.
 *
 * @param mistakes число ошибочных ответов за игру.
 * @param elapsedSeconds сколько секунд заняла игра.
 * @param difficulty сложность 1..3.
 */
fun computePatternScore(mistakes: Int, elapsedSeconds: Long, difficulty: Int): Int =
    (-10L * mistakes + (600L - elapsedSeconds) * difficulty).toInt()
