package com.example.smartalarm.feature.games.capitals

import kotlin.random.Random

/** Направление вопроса викторины. */
enum class QuestionDirection {
    /** «Какой город — столица страны X?» — ответ выбирается среди столиц. */
    COUNTRY_TO_CAPITAL,

    /** «X — столица какой страны?» — ответ выбирается среди стран. */
    CAPITAL_TO_COUNTRY,
}

/**
 * Один вопрос викторины «Столицы».
 *
 * @property entry пара «страна — столица», о которой задан вопрос.
 * @property direction направление вопроса (страна → столица или наоборот).
 * @property options 4 варианта ответа в случайном порядке; ровно один верный,
 * все варианты одного типа (только столицы либо только страны).
 */
data class CapitalsQuestion(
    val entry: CountryCapital,
    val direction: QuestionDirection,
    val options: List<String>,
) {
    /** Верный вариант ответа: столица либо страна пары [entry]. */
    val correctAnswer: String
        get() = when (direction) {
            QuestionDirection.COUNTRY_TO_CAPITAL -> entry.capital
            QuestionDirection.CAPITAL_TO_COUNTRY -> entry.country
        }

    /** Текст вопроса на русском (без склонения названий стран). */
    val text: String
        get() = when (direction) {
            QuestionDirection.COUNTRY_TO_CAPITAL ->
                "Какой город — столица страны ${entry.country}?"

            QuestionDirection.CAPITAL_TO_COUNTRY ->
                "${entry.capital} — столица какой страны?"
        }
}

/** Результат выбора варианта ответа. */
enum class AnswerResult {
    /** Ответ неверный — ошибка засчитана, задан новый вопрос. */
    WRONG,

    /** Ответ верный, но для победы нужны ещё ответы — задан новый вопрос. */
    NEXT_QUESTION,

    /** Ответ верный и последний — игра пройдена. */
    WIN,
}

/**
 * Чистая логика викторины «Столицы» (game id = 29), без зависимостей от Android.
 *
 * Правила: показывается вопрос со случайным направлением — «какой город —
 * столица страны X?» или «X — столица какой страны?» — и 4 вариантами ответа
 * одного типа. Для победы нужно [targetCorrect] верных ответов
 * (4/5/6 по сложности). После любого ответа (верного или ошибочного) задаётся
 * новый вопрос; пары «страна — столица» за игру не повторяются, пока пул
 * не исчерпан. Дистракторы берутся из пула той же сложности, в первую
 * очередь — из региона загаданной страны.
 *
 * Пул сложности N — все пары с `level <= N`: 1 — самые известные страны,
 * 2 — плюс Европа/Азия среднего уровня, 3 — плюс редкие страны.
 *
 * @param difficulty сложность 1..3 (значения вне диапазона приводятся к нему).
 * @param random источник случайности; подставьте [Random] с seed для
 * детерминированных тестов.
 * @param pairs база пар «страна — столица»; по умолчанию [CAPITALS],
 * параметр нужен для тестов.
 */
class CapitalsGame(
    difficulty: Int,
    private val random: Random = Random.Default,
    pairs: List<CountryCapital> = CAPITALS,
) {
    /** Сложность игры, приведённая к диапазону 1..3. */
    val difficulty: Int = difficulty.coerceIn(1, 3)

    /** Сколько верных ответов нужно для победы: 4/5/6 по сложности. */
    val targetCorrect: Int = this.difficulty + 3

    private val pool: List<CountryCapital> = pairs.filter { it.level <= this.difficulty }

    init {
        require(pool.size >= OPTIONS_COUNT) {
            "Пул сложности ${this.difficulty} должен содержать не меньше $OPTIONS_COUNT пар"
        }
    }

    private val usedEntries = mutableSetOf<CountryCapital>()

    /** Сколько верных ответов уже дано. */
    var correctCount: Int = 0
        private set

    /** Сколько ошибочных ответов сделано за игру. */
    var mistakes: Int = 0
        private set

    /** true, когда набрано [targetCorrect] верных ответов; дальнейшие ответы игнорируются. */
    var isFinished: Boolean = false
        private set

    /** Текущий вопрос. */
    var currentQuestion: CapitalsQuestion = generateQuestion()
        private set

    /**
     * Обрабатывает выбор варианта [option] текущего вопроса.
     *
     * @return [AnswerResult.WRONG] при ошибке (счётчик [mistakes] растёт,
     * задаётся новый вопрос), [AnswerResult.NEXT_QUESTION] при верном ответе,
     * когда до победы ещё далеко, [AnswerResult.WIN] при последнем верном
     * ответе. После победы всегда возвращает [AnswerResult.WIN], ничего не меняя.
     */
    fun onAnswer(option: String): AnswerResult {
        if (isFinished)
            return AnswerResult.WIN

        if (option != currentQuestion.correctAnswer) {
            mistakes++
            currentQuestion = generateQuestion()
            return AnswerResult.WRONG
        }

        correctCount++
        return if (correctCount == targetCorrect) {
            isFinished = true
            AnswerResult.WIN
        } else {
            currentQuestion = generateQuestion()
            AnswerResult.NEXT_QUESTION
        }
    }

    private fun generateQuestion(): CapitalsQuestion {
        var available = pool.filterNot { it in usedEntries }
        if (available.isEmpty()) {
            usedEntries.clear()
            available = pool
        }
        val entry = available[random.nextInt(available.size)]
        usedEntries.add(entry)

        val direction =
            if (random.nextBoolean()) QuestionDirection.COUNTRY_TO_CAPITAL
            else QuestionDirection.CAPITAL_TO_COUNTRY

        val others = pool.filter { it != entry }
        val sameRegion = others.filter { it.region == entry.region }.shuffled(random)
        val otherRegions = others.filterNot { it.region == entry.region }.shuffled(random)
        val distractors = (sameRegion + otherRegions).take(OPTIONS_COUNT - 1)

        val answerOf: (CountryCapital) -> String = {
            when (direction) {
                QuestionDirection.COUNTRY_TO_CAPITAL -> it.capital
                QuestionDirection.CAPITAL_TO_COUNTRY -> it.country
            }
        }
        val options = (distractors.map(answerOf) + answerOf(entry)).shuffled(random)

        return CapitalsQuestion(entry, direction, options)
    }

    companion object {
        /** Число вариантов ответа в вопросе. */
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
fun computeCapitalsScore(mistakes: Int, elapsedSeconds: Long, difficulty: Int): Int =
    (-10L * mistakes + (600L - elapsedSeconds) * difficulty).toInt()
