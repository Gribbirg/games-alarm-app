package com.example.smartalarm.feature.games.chain

import kotlin.random.Random

/** Результат проверки введённого игроком итога цепочки. */
enum class ChainAnswerResult {
    /**
     * Ответа нет: строка пустая или не является числом, либо фаза ввода
     * ещё не наступила. Состояние игры не меняется, штрафа нет.
     */
    EMPTY,

    /** Ответ неверный: начисляется штраф и генерируется новая цепочка с начала. */
    WRONG,

    /** Цепочка решена верно, но для победы нужно решить ещё одну. */
    CHAIN_SOLVED,

    /** Решено нужное число цепочек — игра пройдена. */
    WIN
}

/**
 * Состояние одной партии игры «Цепочка» (устный счёт).
 *
 * Чистая логика без зависимостей от Android. Игроку показывается стартовое
 * число, затем по одной — операции цепочки; каждая следующая открывается
 * вызовом [showNextStep] (в UI — кнопка «Дальше»: показ по кнопке, а не по
 * таймеру, выбран сознательно — только что проснувшийся игрок сам управляет
 * темпом и не может «проспать» шаг). После последней операции ещё один вызов
 * [showNextStep] переводит игру в фазу ввода ([isInputPhase]), где игрок
 * вводит итог цепочки и он проверяется через [submitAnswer].
 *
 * Для победы нужно решить [totalChains] цепочек: **одну** на сложностях 1–2 и
 * **две** на сложности 3. Решение: на сложности 3 цепочка из 7 операций с
 * большими числами сама по себе требует заметной концентрации, но одну цепочку
 * всё же можно угадать «на удаче»; вторая цепочка гарантирует, что игрок
 * действительно проснулся, при этом не растягивает игру чрезмерно.
 *
 * Ошибка не отнимает уже решённые цепочки — генерируется только новая текущая
 * цепочка, и показ начинается заново со стартового числа.
 *
 * @param difficulty сложность 1..3 (значения вне диапазона приводятся к границам).
 * @param random источник случайности для генерации цепочек.
 */
class ChainGame(difficulty: Int, random: Random = Random.Default) {

    /** Сложность партии, приведённая к диапазону 1..3. */
    val difficulty = difficulty.coerceIn(1, 3)

    private val generator = ChainGenerator(random)

    /** Сколько цепочек нужно решить для победы (1 на сложностях 1–2, 2 на сложности 3). */
    val totalChains = CHAINS_BY_DIFFICULTY.getValue(this.difficulty)

    /** Сколько цепочек уже решено верно. */
    var solvedChains = 0
        private set

    /** Текущая цепочка. Меняется после ошибки или решённой цепочки. */
    var currentChain: Chain = generator.generate(this.difficulty)
        private set

    /** Сколько операций текущей цепочки уже показано (0 — показано только стартовое число). */
    var shownSteps = 0
        private set

    /** Наступила ли фаза ввода итога (все операции показаны, ждём ответ). */
    var isInputPhase = false
        private set

    /** Достигнута ли победа. */
    val isWon: Boolean
        get() = solvedChains >= totalChains

    /**
     * Текст, который сейчас должен видеть игрок: стартовое число,
     * текущая операция («+ 7», «× 2»…) или приглашение «= ?» в фазе ввода.
     */
    val currentDisplay: String
        get() = when {
            isInputPhase -> "= ?"
            shownSteps == 0 -> currentChain.start.toString()
            else -> currentChain.steps[shownSteps - 1].text
        }

    /**
     * Открывает следующую операцию цепочки; после последней операции
     * переводит игру в фазу ввода.
     *
     * @return `true`, если состояние изменилось; `false`, если игра уже
     * в фазе ввода или пройдена.
     */
    fun showNextStep(): Boolean {
        if (isWon || isInputPhase) return false
        if (shownSteps < currentChain.steps.size)
            shownSteps++
        else
            isInputPhase = true
        return true
    }

    /**
     * Проверяет введённый игроком итог цепочки.
     *
     * @param input введённая строка; пробелы по краям игнорируются.
     * @return результат: [ChainAnswerResult.EMPTY] — нет ответа (состояние не
     * меняется), [ChainAnswerResult.WRONG] — неверно (новая цепочка),
     * [ChainAnswerResult.CHAIN_SOLVED] — верно, но нужна ещё цепочка (новая
     * цепочка), [ChainAnswerResult.WIN] — победа. После победы всегда
     * возвращает [ChainAnswerResult.WIN], не меняя состояние.
     */
    fun submitAnswer(input: String): ChainAnswerResult {
        if (isWon) return ChainAnswerResult.WIN
        if (!isInputPhase) return ChainAnswerResult.EMPTY
        val answer = input.trim().toIntOrNull() ?: return ChainAnswerResult.EMPTY
        return if (answer == currentChain.result) {
            solvedChains++
            if (isWon) {
                ChainAnswerResult.WIN
            } else {
                newChain()
                ChainAnswerResult.CHAIN_SOLVED
            }
        } else {
            newChain()
            ChainAnswerResult.WRONG
        }
    }

    /** Генерирует новую цепочку и начинает её показ со стартового числа. */
    private fun newChain() {
        currentChain = generator.generate(difficulty)
        shownSteps = 0
        isInputPhase = false
    }

    companion object {
        private val CHAINS_BY_DIFFICULTY = mapOf(1 to 1, 2 to 1, 3 to 2)
    }
}
