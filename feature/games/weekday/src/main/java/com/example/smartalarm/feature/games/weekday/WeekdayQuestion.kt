package com.example.smartalarm.feature.games.weekday

import kotlin.random.Random

/**
 * Один вопрос игры «День недели».
 *
 * @property startDay день, с которого ведётся отсчёт («Сегодня — …»)
 * @property offset смещение в днях; положительное — в будущее, отрицательное — в прошлое
 * @property text полный текст вопроса на русском
 * @property correctAnswer верный ответ, всегда равен `startDay.shifted(offset)`
 * @property options четыре уникальных варианта ответа в случайном порядке,
 * среди которых обязательно есть [correctAnswer]
 */
data class WeekdayQuestion(
    val startDay: Weekday,
    val offset: Int,
    val text: String,
    val correctAnswer: Weekday,
    val options: List<Weekday>
)

/**
 * Генерация вопросов игры «День недели»: диапазоны смещений по сложности,
 * формулировка текста и подбор вариантов ответа. Чистая логика без Android.
 */
object WeekdayQuestionFactory {

    /** Количество вариантов ответа в вопросе. */
    const val OPTIONS_COUNT = 4

    /**
     * Диапазон допустимых модулей смещения для уровня сложности [difficulty]
     * (1..3; всё вне диапазона трактуется как лёгкий уровень).
     */
    fun offsetRangeFor(difficulty: Int): IntRange = when (difficulty) {
        2 -> 5..30
        3 -> 20..100
        else -> 1..7
    }

    /**
     * Генерирует случайный вопрос для уровня сложности [difficulty].
     *
     * Модуль смещения берётся из [offsetRangeFor], направление (вперёд/назад)
     * выбирается равновероятно. На сложности 3 половина вопросов формулируется
     * в виде «через N недель и M дней» вместо количества дней.
     *
     * @param random источник случайности; передайте `Random(seed)` для воспроизводимости
     */
    fun generate(difficulty: Int, random: Random): WeekdayQuestion {
        val range = offsetRangeFor(difficulty)
        val magnitude = random.nextInt(range.first, range.last + 1)
        val offset = if (random.nextBoolean()) magnitude else -magnitude
        val startDay = Weekday.entries.random(random)
        val asWeeks = difficulty >= 3 && random.nextBoolean()
        val correctAnswer = startDay.shifted(offset)
        return WeekdayQuestion(
            startDay = startDay,
            offset = offset,
            text = questionText(startDay, offset, asWeeks),
            correctAnswer = correctAnswer,
            options = buildOptions(correctAnswer, random)
        )
    }

    /**
     * Строит текст вопроса.
     *
     * Примеры:
     * - `questionText(WEDNESDAY, 9, false)` →
     *   «Сегодня — среда. Какой день недели будет через 9 дней?»
     * - `questionText(MONDAY, -4, false)` →
     *   «Сегодня — понедельник. Какой день недели был 4 дня назад?»
     * - `questionText(FRIDAY, 25, true)` →
     *   «Сегодня — пятница. Какой день недели будет через 3 недели и 4 дня?»
     *
     * @param startDay стартовый день
     * @param offset ненулевое смещение в днях (знак задаёт направление)
     * @param asWeeks если true, смещение записывается как недели и дни
     */
    fun questionText(startDay: Weekday, offset: Int, asWeeks: Boolean): String {
        require(offset != 0) { "Смещение не может быть нулевым" }
        val span = spanText(if (offset > 0) offset else -offset, asWeeks)
        val tail = if (offset > 0) "будет через $span?" else "был $span назад?"
        return "Сегодня — ${startDay.displayName}. Какой день недели $tail"
    }

    /**
     * Возвращает [OPTIONS_COUNT] уникальных вариантов ответа в случайном
     * порядке; вариант [correctAnswer] присутствует всегда.
     */
    fun buildOptions(correctAnswer: Weekday, random: Random): List<Weekday> {
        val wrong = Weekday.entries
            .filter { it != correctAnswer }
            .shuffled(random)
            .take(OPTIONS_COUNT - 1)
        return (wrong + correctAnswer).shuffled(random)
    }

    /**
     * Текст величины смещения: «9 дней» или, при [asWeeks], «3 недели и 4 дня»
     * («3 недели», если дней 0). Форма слова — винительный падеж, одинаковый
     * для «через …» и «… назад».
     *
     * @param magnitude модуль смещения, положительное число
     */
    fun spanText(magnitude: Int, asWeeks: Boolean): String {
        require(magnitude > 0) { "Модуль смещения должен быть положительным" }
        if (!asWeeks) return "$magnitude ${daysWord(magnitude)}"
        val weeks = magnitude / Weekday.DAYS_IN_WEEK
        val days = magnitude % Weekday.DAYS_IN_WEEK
        if (weeks == 0) return "$days ${daysWord(days)}"
        val weeksPart = "$weeks ${weeksWord(weeks)}"
        return if (days == 0) weeksPart else "$weeksPart и $days ${daysWord(days)}"
    }

    /** Форма слова «день» для числительного [n]: день/дня/дней. */
    fun daysWord(n: Int): String = pluralize(n, "день", "дня", "дней")

    /**
     * Форма слова «неделя» в винительном падеже для числительного [n]:
     * неделю/недели/недель («через 1 неделю», «2 недели назад»).
     */
    fun weeksWord(n: Int): String = pluralize(n, "неделю", "недели", "недель")

    private fun pluralize(n: Int, one: String, few: String, many: String): String {
        val mod100 = n % 100
        if (mod100 in 11..14) return many
        return when (n % 10) {
            1 -> one
            2, 3, 4 -> few
            else -> many
        }
    }
}
