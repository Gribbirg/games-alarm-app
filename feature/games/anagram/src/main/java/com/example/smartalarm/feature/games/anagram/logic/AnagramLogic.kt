package com.example.smartalarm.feature.games.anagram.logic

import kotlin.random.Random

/**
 * Перемешивает буквы слова так, чтобы порядок отличался от исходного,
 * если это возможно.
 *
 * Если в слове меньше двух разных букв (например, оно состоит из одной
 * повторяющейся буквы), любой порядок совпадает с исходным — тогда буквы
 * возвращаются как есть. Иначе перемешивание повторяется, пока результат
 * не станет отличаться от исходного слова.
 *
 * @param word исходное слово
 * @param random источник случайности (передаётся параметром ради
 * детерминированности в тестах)
 * @return буквы слова в перемешанном порядке
 */
fun shuffleWord(word: String, random: Random): List<Char> {
    val letters = word.toList()
    if (letters.distinct().size < 2) return letters
    var shuffled: List<Char>
    do {
        shuffled = letters.shuffled(random)
    } while (shuffled.joinToString("") == word)
    return shuffled
}

/** Результат проверки собранного слова в [AnagramGame.submit]. */
enum class SubmitResult {
    /** Слово собрано не полностью — проверять нечего, штрафа нет. */
    NOT_COMPLETE,

    /** Собранное слово не совпало с загаданным; набор сброшен. */
    WRONG,

    /** Слово отгадано, загадано следующее. */
    WORD_DONE,

    /** Отгадано последнее слово — игра пройдена. */
    GAME_WON
}

/**
 * Чистая логика игры «Анаграммы» — без зависимостей от Android.
 *
 * Игроку показываются перемешанные буквы загаданного слова; он выбирает
 * буквы по одной (по индексу кнопки, а не по символу — в слове могут быть
 * одинаковые буквы), собирая слово. Для победы нужно отгадать
 * [totalWords] слов, слова в рамках одной игры не повторяются.
 *
 * @param difficulty уровень сложности 1..3 — определяет словарь
 * (см. [AnagramWords.forDifficulty]) и число слов до победы
 * (см. [wordCountForDifficulty])
 * @param random источник случайности для выбора и перемешивания слов
 * @param dictionary словарь слов; по умолчанию берётся по сложности,
 * параметр нужен для тестов
 */
class AnagramGame(
    difficulty: Int,
    private val random: Random = Random.Default,
    dictionary: List<String> = AnagramWords.forDifficulty(difficulty)
) {

    /** Сколько слов нужно отгадать для победы. */
    val totalWords: Int = wordCountForDifficulty(difficulty)

    private val selectedWords: List<String>

    init {
        require(dictionary.size >= totalWords) {
            "В словаре ${dictionary.size} слов, а нужно $totalWords"
        }
        selectedWords = dictionary.shuffled(random).take(totalWords)
    }

    /** Сколько слов уже отгадано. */
    var wordsCompleted: Int = 0
        private set

    /** Загаданное сейчас слово (после победы — последнее слово игры). */
    val currentWord: String
        get() = selectedWords[wordsCompleted.coerceAtMost(totalWords - 1)]

    /** Буквы текущего слова в перемешанном порядке — по одной на кнопку. */
    var shuffledLetters: List<Char> = shuffleWord(currentWord, random)
        private set

    private val typed = mutableListOf<Int>()

    /** Индексы букв из [shuffledLetters], нажатых игроком, в порядке нажатия. */
    val typedIndices: List<Int>
        get() = typed.toList()

    /** Слово, собранное игроком к этому моменту. */
    val typedWord: String
        get() = typed.map { shuffledLetters[it] }.joinToString("")

    /** `true`, если игрок использовал все буквы текущего слова. */
    val isWordComplete: Boolean
        get() = typed.size == shuffledLetters.size

    /** `true`, если отгаданы все [totalWords] слов. */
    val isGameWon: Boolean
        get() = wordsCompleted == totalWords

    /**
     * Обрабатывает нажатие кнопки-буквы.
     *
     * @param index индекс буквы в [shuffledLetters]
     * @return `true`, если буква принята; `false`, если индекс вне диапазона,
     * буква уже использована, слово уже собрано или игра выиграна
     */
    fun pressLetter(index: Int): Boolean {
        if (isGameWon || isWordComplete) return false
        if (index !in shuffledLetters.indices || index in typed) return false
        typed.add(index)
        return true
    }

    /** Сбрасывает набранные буквы текущего слова (кнопка «Сброс»). */
    fun resetInput() {
        typed.clear()
    }

    /**
     * Проверяет собранное слово.
     *
     * При верном слове засчитывает его и, если игра не выиграна, загадывает
     * следующее (буквы заново перемешиваются). При неверном — сбрасывает
     * набор, чтобы игрок собирал слово заново.
     *
     * @return результат проверки, см. [SubmitResult]
     */
    fun submit(): SubmitResult {
        if (isGameWon) return SubmitResult.GAME_WON
        if (!isWordComplete) return SubmitResult.NOT_COMPLETE
        return if (typedWord == currentWord) {
            wordsCompleted++
            typed.clear()
            if (isGameWon) {
                SubmitResult.GAME_WON
            } else {
                shuffledLetters = shuffleWord(currentWord, random)
                SubmitResult.WORD_DONE
            }
        } else {
            typed.clear()
            SubmitResult.WRONG
        }
    }

    companion object {
        /**
         * Число слов до победы для уровня сложности: 2 на первом уровне,
         * 3 на втором и третьем.
         */
        fun wordCountForDifficulty(difficulty: Int): Int =
            if (difficulty <= 1) 2 else 3
    }
}
