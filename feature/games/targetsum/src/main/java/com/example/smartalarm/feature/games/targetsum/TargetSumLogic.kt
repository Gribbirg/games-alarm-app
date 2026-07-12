package com.example.smartalarm.feature.games.targetsum

import kotlin.random.Random

/**
 * Результат нажатия «Проверить» в игре «Набери сумму».
 */
enum class CheckResult {
    /** Сумма выделенных чисел не равна цели: начислен штраф, выделение сохранено. */
    MISTAKE,

    /** Сумма верна, впереди следующий раунд (нужно вызвать [TargetSumGame.startNextRound]). */
    ROUND_COMPLETE,

    /** Сумма верна в последнем раунде — победа. */
    WIN
}

/**
 * Один раунд игры «Набери сумму».
 *
 * Раунд по построению всегда имеет решение: сначала выбирается случайное
 * подмножество ячеек [solution], затем цель [target] считается как сумма
 * их значений. Игроку при этом засчитывается ЛЮБОЕ подмножество с суммой,
 * равной цели, — не только построенное.
 *
 * @property numbers значения ячеек поля (слева направо, сверху вниз)
 * @property target целевая сумма, которую нужно набрать
 * @property solution индексы ячеек построенного решения (для тестов/отладки)
 */
data class TargetSumRound(
    val numbers: List<Int>,
    val target: Int,
    val solution: Set<Int>
)

/**
 * Генерирует раунд по настройкам [settings]: все [TargetSumSettings.gridSize]
 * ячеек заполняются случайными числами из
 * `[TargetSumSettings.minNumber]..[TargetSumSettings.maxNumber]`, затем
 * случайные [TargetSumSettings.solutionSize] индексов объявляются решением,
 * а цель — сумма их значений. Существование решения гарантировано
 * по построению.
 *
 * @param random источник случайности; в тестах передаётся `Random(seed)`
 */
fun generateTargetSumRound(settings: TargetSumSettings, random: Random): TargetSumRound {
    val numbers = List(settings.gridSize) {
        random.nextInt(settings.minNumber, settings.maxNumber + 1)
    }
    val solution = numbers.indices.shuffled(random)
        .take(settings.solutionSize)
        .toSet()
    return TargetSumRound(numbers, solution.sumOf { numbers[it] }, solution)
}

/**
 * Чистая логика игры «Набери сумму» (без зависимостей от Android).
 *
 * Держит текущий раунд ([currentRound]) и множество выделенных ячеек,
 * обрабатывает выделение/снятие ([toggle]), проверку суммы ([check]),
 * переключение раундов и подсчёт очков. Вся случайность идёт через
 * [random], поэтому с фиксированным seed поведение детерминировано.
 *
 * Подсчёт очков — по образцу арифметики (calc): каждая неверная проверка —
 * −[MISTAKE_PENALTY]; при победе прибавляется
 * `(600 − прошло_секунд) × сложность` (см. [finalScore]).
 *
 * @param difficulty уровень сложности 1..3 (вне диапазона приводится к границе)
 * @param random источник случайности; в тестах передаётся `Random(seed)`
 */
class TargetSumGame(
    difficulty: Int,
    private val random: Random = Random.Default
) {
    /** Настройки игры, вычисленные из уровня сложности. */
    val settings: TargetSumSettings = TargetSumSettings.forDifficulty(difficulty)

    /** Номер текущего раунда, начиная с 1. */
    var roundNumber: Int = 1
        private set

    /** Текущий раунд: числа, цель и построенное решение. */
    var currentRound: TargetSumRound = generateTargetSumRound(settings, random)
        private set

    private val selected = mutableSetOf<Int>()

    /** Накопленные штрафы (0 или меньше); итог считает [finalScore]. */
    var score: Int = 0
        private set

    /** Число неверных проверок за игру. */
    var mistakes: Int = 0
        private set

    /** Значения ячеек текущего раунда. */
    val numbers: List<Int>
        get() = currentRound.numbers

    /** Целевая сумма текущего раунда. */
    val target: Int
        get() = currentRound.target

    /** Индексы выделенных ячеек (копия — менять состояние можно только через [toggle]). */
    val selectedIndices: Set<Int>
        get() = selected.toSet()

    /** Сумма значений выделенных ячеек (0, если ничего не выделено). */
    val selectedSum: Int
        get() = selected.sumOf { currentRound.numbers[it] }

    /**
     * Переключает выделение ячейки [index]: невыделенная выделяется,
     * выделенная — снимается. Индекс вне поля игнорируется.
     *
     * @return `true`, если после вызова ячейка выделена
     */
    fun toggle(index: Int): Boolean {
        if (index !in currentRound.numbers.indices)
            return false
        if (!selected.add(index))
            selected.remove(index)
        return index in selected
    }

    /** Снимает выделение со всех ячеек. */
    fun clearSelection() {
        selected.clear()
    }

    /**
     * Проверяет текущее выделение.
     *
     * Если сумма выделенных чисел не равна цели (в том числе при пустом
     * выделении — цель всегда положительна) — [CheckResult.MISTAKE]: штраф
     * −[MISTAKE_PENALTY], выделение сохраняется, чтобы игрок мог его
     * поправить. Если равна — [CheckResult.ROUND_COMPLETE] или, в последнем
     * раунде, [CheckResult.WIN]. Принимается любое подмножество с точной
     * суммой, не только построенное при генерации.
     */
    fun check(): CheckResult {
        if (selectedSum != currentRound.target) {
            mistakes++
            score -= MISTAKE_PENALTY
            return CheckResult.MISTAKE
        }
        return if (roundNumber >= settings.roundsCount)
            CheckResult.WIN
        else
            CheckResult.ROUND_COMPLETE
    }

    /**
     * Переходит к следующему раунду: генерируется новое поле и цель,
     * выделение сбрасывается.
     *
     * @throws IllegalStateException если текущий раунд был последним
     */
    fun startNextRound() {
        if (roundNumber >= settings.roundsCount)
            throw IllegalStateException("Игра уже завершена")
        roundNumber++
        selected.clear()
        currentRound = generateTargetSumRound(settings, random)
    }

    /**
     * Итоговый счёт: накопленные штрафы плюс
     * `(600 − [elapsedSeconds]) × сложность`.
     */
    fun finalScore(elapsedSeconds: Long): Int =
        score + ((600 - elapsedSeconds) * settings.difficulty).toInt()

    companion object {
        /** Штраф за неверную проверку. */
        const val MISTAKE_PENALTY = 10
    }
}
