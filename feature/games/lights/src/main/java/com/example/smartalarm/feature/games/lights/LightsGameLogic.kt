package com.example.smartalarm.feature.games.lights

import kotlin.random.Random

/**
 * Возвращает сторону квадратного поля для уровня сложности.
 *
 * 1 — 3×3, 2 — 4×4, 3 — 5×5. Неизвестная сложность трактуется как самая лёгкая.
 *
 * @param difficulty уровень сложности (1..3)
 * @return сторона поля в клетках
 */
fun lightsSizeFor(difficulty: Int): Int = when (difficulty) {
    2 -> 4
    3 -> 5
    else -> 3
}

/**
 * Возвращает число K случайных «запутывающих» нажатий для уровня сложности.
 *
 * Поле генерируется K нажатиями по различным клеткам из полностью погашенного
 * состояния, поэтому те же K нажатий гарантированно решают головоломку.
 * 1 — 3 нажатия, 2 — 5, 3 — 7. Неизвестная сложность трактуется как самая лёгкая.
 *
 * @param difficulty уровень сложности (1..3)
 * @return число запутывающих нажатий K
 */
fun lightsScrambleCountFor(difficulty: Int): Int = when (difficulty) {
    2 -> 5
    3 -> 7
    else -> 3
}

/**
 * Возвращает «пар» — число нажатий, укладываясь в которое игрок не получает
 * штрафа: 3×K, где K — [lightsScrambleCountFor]. Каждое нажатие сверх пара
 * штрафуется (−10 очков, см. `LightsGameViewModel.mistake`).
 *
 * @param difficulty уровень сложности (1..3)
 * @return допустимое число нажатий без штрафа
 */
fun lightsPressParFor(difficulty: Int): Int = 3 * lightsScrambleCountFor(difficulty)

/**
 * Переключает в [lights] лампочку с плоским индексом [index] и её
 * ортогональных соседей (вверх/вниз/влево/вправо, без выхода за край поля).
 *
 * @param lights состояние поля: true — лампочка горит
 * @param size сторона квадратного поля
 * @param index плоский индекс клетки (`row * size + col`), должен быть валиден
 */
internal fun toggleWithNeighbors(lights: BooleanArray, size: Int, index: Int) {
    val row = index / size
    val col = index % size
    lights[index] = !lights[index]
    if (row > 0) lights[index - size] = !lights[index - size]
    if (row < size - 1) lights[index + size] = !lights[index + size]
    if (col > 0) lights[index - 1] = !lights[index - 1]
    if (col < size - 1) lights[index + 1] = !lights[index + 1]
}

/**
 * Результат генерации поля «Погаси свет».
 *
 * @property size сторона квадратного поля
 * @property lights стартовое состояние поля (плоский список `size*size`,
 * true — лампочка горит); гарантированно содержит хотя бы одну горящую лампочку
 * @property scramble плоские индексы K различных клеток, нажатиями по которым
 * поле было получено из погашенного состояния; повторение этих же нажатий
 * (в любом порядке) полностью гасит поле — гарантия решаемости
 */
data class GeneratedLightsField(
    val size: Int,
    val lights: List<Boolean>,
    val scramble: List<Int>
)

/**
 * Генерирует гарантированно решаемое поле «Погаси свет».
 *
 * Из полностью погашенного поля выполняется [scrambleCount] нажатий по
 * различным случайным клеткам. Так как нажатия самообратны и коммутируют,
 * повторение того же набора нажатий гасит поле — решение всегда существует
 * и не длиннее K ходов. Если после запутывания поле случайно оказалось
 * полностью погашенным (набор клеток образовал «тихий узор»), генерация
 * повторяется с новыми клетками.
 *
 * @param size сторона квадратного поля (больше 0)
 * @param scrambleCount число K различных запутывающих нажатий (1..size²)
 * @param random источник случайности (передаётся для детерминизма в тестах)
 * @return поле с непустым набором горящих лампочек и его решением
 */
fun generateLightsField(
    size: Int,
    scrambleCount: Int,
    random: Random = Random.Default
): GeneratedLightsField {
    require(size > 0) { "size must be positive, got $size" }
    require(scrambleCount in 1..size * size) {
        "scrambleCount must be in 1..${size * size}, got $scrambleCount"
    }

    while (true) {
        val scramble = (0 until size * size).shuffled(random).take(scrambleCount)
        val lights = BooleanArray(size * size)
        for (index in scramble)
            toggleWithNeighbors(lights, size, index)
        if (lights.any { it })
            return GeneratedLightsField(size, lights.toList(), scramble)
    }
}

/**
 * Состояние партии «Погаси свет» (Lights Out).
 *
 * Чистая логика без зависимостей от Android: квадратное поле лампочек,
 * нажатие на клетку переключает её и четырёх ортогональных соседей.
 * Цель — погасить все лампочки. Класс также считает сделанные нажатия
 * для начисления штрафов сверх «пара» ([lightsPressParFor]).
 *
 * @property size сторона квадратного поля
 * @param initialLights стартовое состояние поля, плоский список размера
 * `size * size` (см. [generateLightsField])
 */
class LightsGame(val size: Int, initialLights: List<Boolean>) {

    init {
        require(initialLights.size == size * size) {
            "expected ${size * size} cells, got ${initialLights.size}"
        }
    }

    private val lights = initialLights.toBooleanArray()

    /** Число сделанных игроком нажатий за партию. */
    var pressCount: Int = 0
        private set

    /** Общее число клеток поля. */
    val cellCount: Int
        get() = size * size

    /** Число горящих лампочек. */
    val litCount: Int
        get() = lights.count { it }

    /** true, если все лампочки погашены — игра выиграна. */
    val isWon: Boolean
        get() = lights.none { it }

    /**
     * Проверяет, горит ли лампочка.
     *
     * @param index плоский индекс клетки (`row * size + col`)
     * @return true, если лампочка горит; false для погашенной или индекса вне поля
     */
    fun isOn(index: Int): Boolean = index in lights.indices && lights[index]

    /**
     * Ход игрока — нажатие на клетку: переключает её и ортогональных соседей.
     *
     * @param index плоский индекс клетки
     * @return true, если ход выполнен; false, если индекс вне поля
     * (такой клик не считается нажатием)
     */
    fun press(index: Int): Boolean {
        if (index !in lights.indices) return false
        toggleWithNeighbors(lights, size, index)
        pressCount++
        return true
    }
}
