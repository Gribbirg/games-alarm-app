package com.example.smartalarm.feature.games.spelling

/**
 * База слов игры «Как пишется?» — три уровня сложности, в каждом не менее
 * 20 записей. Каждая запись содержит правильное написание и 1–3 заранее
 * составленных вручную неправильных варианта (типичные орфографические
 * ошибки), т.е. на экране игрок видит 2–4 варианта одного слова.
 *
 * Пул уровня N содержит ТОЛЬКО записи уровня N (уровни не смешиваются):
 * так сложность игры чётко соответствует выбранному уровню будильника.
 * Записей на уровень хватает с запасом — за игру нужно 4–6 верных ответов,
 * а при исчерпании пула [SpellingGame] начинает выдавать слова по новому кругу.
 *
 * Слова с одинаковым написанием, но разным смыслом (например, «в течение» /
 * «в течении») снабжены поясняющим контекстом прямо в строке варианта,
 * чтобы правильный ответ был однозначен.
 */
object SpellingData {

    /** Уровень 1 — простые частотные слова школьного уровня. */
    val LEVEL_1: List<SpellingEntry> = listOf(
        SpellingEntry("корова", listOf("карова")),
        SpellingEntry("молоко", listOf("малоко", "молако", "малако")),
        SpellingEntry("собака", listOf("сабака")),
        SpellingEntry("заяц", listOf("заец", "заиц")),
        SpellingEntry("воробей", listOf("варобей", "варабей")),
        SpellingEntry("ворона", listOf("варона")),
        SpellingEntry("мороз", listOf("мароз", "морос")),
        SpellingEntry("пенал", listOf("пинал")),
        SpellingEntry("девочка", listOf("девачка")),
        SpellingEntry("дорога", listOf("дарога")),
        SpellingEntry("город", listOf("горад", "горот")),
        SpellingEntry("работа", listOf("робота")),
        SpellingEntry("берёза", listOf("бирёза")),
        SpellingEntry("капуста", listOf("копуста")),
        SpellingEntry("карандаш", listOf("корандаш", "карондаш")),
        SpellingEntry("машина", listOf("мошина", "машына")),
        SpellingEntry("сорока", listOf("сарока")),
        SpellingEntry("хорошо", listOf("харашо", "харошо", "хорашо")),
        SpellingEntry("ученик", listOf("учиник", "ученник")),
        SpellingEntry("учитель", listOf("учитиль", "учетель")),
        SpellingEntry("яблоко", listOf("яблако")),
        SpellingEntry("медведь", listOf("медветь", "мидведь"))
    )

    /** Уровень 2 — слова со средними по сложности орфограммами. */
    val LEVEL_2: List<SpellingEntry> = listOf(
        SpellingEntry("винегрет", listOf("венигрет", "венегрет", "винигрет")),
        SpellingEntry("искусство", listOf("искуство", "исскуство", "исскусство")),
        SpellingEntry("аппетит", listOf("апетит", "аппитит", "апеттит")),
        SpellingEntry("территория", listOf("територия", "терретория")),
        SpellingEntry("коридор", listOf("корридор", "калидор")),
        SpellingEntry("галерея", listOf("галлерея", "гелерея")),
        SpellingEntry("количество", listOf("колличество", "количиство")),
        SpellingEntry("программа", listOf("програма", "праграмма")),
        SpellingEntry("иммунитет", listOf("имунитет", "иммунетет")),
        SpellingEntry("будущее", listOf("будующее", "будуещее")),
        SpellingEntry("следующий", listOf("следущий", "следуещий")),
        SpellingEntry("девчонка", listOf("девчёнка")),
        SpellingEntry("лестница", listOf("лесница", "лестнеца")),
        SpellingEntry("солнце", listOf("сонце", "солнеце")),
        SpellingEntry("чувство", listOf("чуство", "чювство")),
        SpellingEntry("праздник", listOf("празник", "празднек")),
        SpellingEntry("сердце", listOf("серце")),
        SpellingEntry("окрестность", listOf("окресность")),
        SpellingEntry("участвовать", listOf("учавствовать", "учасвовать")),
        SpellingEntry("здравствуйте", listOf("здраствуйте")),
        SpellingEntry("пожалуйста", listOf("пожалуста", "пожайлуста")),
        SpellingEntry("шоссе", listOf("шосе", "шоссэ")),
        SpellingEntry("теннис", listOf("тенис", "тэннис")),
        SpellingEntry("ресурс", listOf("рессурс")),
        SpellingEntry("грамотный", listOf("граммотный"))
    )

    /** Уровень 3 — сложные случаи, в которых часто ошибаются даже взрослые. */
    val LEVEL_3: List<SpellingEntry> = listOf(
        SpellingEntry("прийти", listOf("придти", "притти")),
        SpellingEntry("в течение недели", listOf("в течении недели", "втечение недели")),
        SpellingEntry("серебряный", listOf("серебрянный")),
        SpellingEntry("деревянный", listOf("деревяный", "диревянный")),
        SpellingEntry("стеклянный", listOf("стекляный")),
        SpellingEntry("оловянный", listOf("оловяный")),
        SpellingEntry("ветреный (день)", listOf("ветренный (день)")),
        SpellingEntry("расчёт", listOf("рассчёт")),
        SpellingEntry("рассчитывать", listOf("расчитывать")),
        SpellingEntry("вследствие дождя", listOf("в следствии дождя", "вследствии дождя")),
        SpellingEntry("впоследствии", listOf("в последствии", "впоследствие")),
        SpellingEntry("по-прежнему", listOf("по прежнему", "попрежнему")),
        SpellingEntry("как будто", listOf("как-будто", "какбудто")),
        SpellingEntry("иметь в виду", listOf("иметь ввиду")),
        SpellingEntry("джентльмен", listOf("джентельмен", "джентельмэн")),
        SpellingEntry("скрупулёзный", listOf("скурпулёзный")),
        SpellingEntry("конфорка", listOf("комфорка", "канфорка")),
        SpellingEntry("дуршлаг", listOf("друшлаг", "дуршлак")),
        SpellingEntry("почерк", listOf("подчерк", "почёрк")),
        SpellingEntry("поскользнуться", listOf("подскользнуться", "поскальзнуться")),
        SpellingEntry("прецедент", listOf("прецендент", "прицедент")),
        SpellingEntry("инцидент", listOf("инциндент", "инцедент")),
        SpellingEntry("дерматин", listOf("дермантин")),
        SpellingEntry("эспрессо", listOf("экспрессо", "эспресо")),
        SpellingEntry("мороженое (десерт)", listOf("мороженное (десерт)", "морожное (десерт)")),
        SpellingEntry("юный", listOf("юнный")),
        SpellingEntry("гостиная", listOf("гостинная")),
        SpellingEntry("в общем", listOf("вообщем", "в-общем"))
    )

    /**
     * Возвращает пул записей для уровня сложности.
     *
     * @param difficulty уровень сложности 1..3; значения вне диапазона
     * приводятся к ближайшей границе
     */
    fun entriesForDifficulty(difficulty: Int): List<SpellingEntry> =
        when (difficulty.coerceIn(1, 3)) {
            1 -> LEVEL_1
            2 -> LEVEL_2
            else -> LEVEL_3
        }
}
