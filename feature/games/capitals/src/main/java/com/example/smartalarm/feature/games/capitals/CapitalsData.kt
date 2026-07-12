package com.example.smartalarm.feature.games.capitals

/** Регион мира; используется для подбора дистракторов из «соседних» стран. */
enum class Region {
    /** Европа. */
    EUROPE,

    /** Азия. */
    ASIA,

    /** Африка. */
    AFRICA,

    /** Северная и Южная Америка. */
    AMERICAS,

    /** Австралия и Океания. */
    OCEANIA,
}

/**
 * Пара «страна — столица» из базы игры.
 *
 * @property country название страны на русском (именительный падеж).
 * @property capital официальная столица страны на русском.
 * @property region регион мира — дистракторы по возможности берутся из него же.
 * @property level уровень известности 1..3: 1 — самые известные страны,
 * 2 — Европа и Азия среднего уровня, 3 — редкие (Океания, Африка,
 * Латинская Америка). Пул сложности N включает уровни 1..N.
 */
data class CountryCapital(
    val country: String,
    val capital: String,
    val region: Region,
    val level: Int,
)

/**
 * База пар «страна — столица» игры «Столицы».
 *
 * Только бесспорные официальные столицы: страны с несколькими столицами
 * (ЮАР, Боливия), спорным статусом столицы (Израиль) или переносом столицы
 * в процессе (Индонезия) в базу не включены; совпадающие названия страны и
 * столицы (Тунис, Алжир) тоже исключены, чтобы вопросы не выглядели
 * подсказками. Страны и столицы в базе не повторяются.
 */
val CAPITALS: List<CountryCapital> = listOf(
    // Уровень 1 — самые известные страны (25).
    CountryCapital("Россия", "Москва", Region.EUROPE, 1),
    CountryCapital("Франция", "Париж", Region.EUROPE, 1),
    CountryCapital("Великобритания", "Лондон", Region.EUROPE, 1),
    CountryCapital("Германия", "Берлин", Region.EUROPE, 1),
    CountryCapital("Италия", "Рим", Region.EUROPE, 1),
    CountryCapital("Испания", "Мадрид", Region.EUROPE, 1),
    CountryCapital("Португалия", "Лиссабон", Region.EUROPE, 1),
    CountryCapital("Греция", "Афины", Region.EUROPE, 1),
    CountryCapital("Австрия", "Вена", Region.EUROPE, 1),
    CountryCapital("Польша", "Варшава", Region.EUROPE, 1),
    CountryCapital("Чехия", "Прага", Region.EUROPE, 1),
    CountryCapital("Украина", "Киев", Region.EUROPE, 1),
    CountryCapital("Беларусь", "Минск", Region.EUROPE, 1),
    CountryCapital("США", "Вашингтон", Region.AMERICAS, 1),
    CountryCapital("Канада", "Оттава", Region.AMERICAS, 1),
    CountryCapital("Мексика", "Мехико", Region.AMERICAS, 1),
    CountryCapital("Бразилия", "Бразилиа", Region.AMERICAS, 1),
    CountryCapital("Аргентина", "Буэнос-Айрес", Region.AMERICAS, 1),
    CountryCapital("Япония", "Токио", Region.ASIA, 1),
    CountryCapital("Китай", "Пекин", Region.ASIA, 1),
    CountryCapital("Индия", "Нью-Дели", Region.ASIA, 1),
    CountryCapital("Турция", "Анкара", Region.ASIA, 1),
    CountryCapital("Казахстан", "Астана", Region.ASIA, 1),
    CountryCapital("Египет", "Каир", Region.AFRICA, 1),
    CountryCapital("Австралия", "Канберра", Region.OCEANIA, 1),

    // Уровень 2 — Европа и Азия среднего уровня (37).
    CountryCapital("Швеция", "Стокгольм", Region.EUROPE, 2),
    CountryCapital("Норвегия", "Осло", Region.EUROPE, 2),
    CountryCapital("Финляндия", "Хельсинки", Region.EUROPE, 2),
    CountryCapital("Дания", "Копенгаген", Region.EUROPE, 2),
    CountryCapital("Исландия", "Рейкьявик", Region.EUROPE, 2),
    CountryCapital("Ирландия", "Дублин", Region.EUROPE, 2),
    CountryCapital("Швейцария", "Берн", Region.EUROPE, 2),
    CountryCapital("Бельгия", "Брюссель", Region.EUROPE, 2),
    CountryCapital("Нидерланды", "Амстердам", Region.EUROPE, 2),
    CountryCapital("Венгрия", "Будапешт", Region.EUROPE, 2),
    CountryCapital("Румыния", "Бухарест", Region.EUROPE, 2),
    CountryCapital("Болгария", "София", Region.EUROPE, 2),
    CountryCapital("Сербия", "Белград", Region.EUROPE, 2),
    CountryCapital("Хорватия", "Загреб", Region.EUROPE, 2),
    CountryCapital("Словакия", "Братислава", Region.EUROPE, 2),
    CountryCapital("Словения", "Любляна", Region.EUROPE, 2),
    CountryCapital("Эстония", "Таллин", Region.EUROPE, 2),
    CountryCapital("Латвия", "Рига", Region.EUROPE, 2),
    CountryCapital("Литва", "Вильнюс", Region.EUROPE, 2),
    CountryCapital("Молдова", "Кишинёв", Region.EUROPE, 2),
    CountryCapital("Армения", "Ереван", Region.ASIA, 2),
    CountryCapital("Грузия", "Тбилиси", Region.ASIA, 2),
    CountryCapital("Азербайджан", "Баку", Region.ASIA, 2),
    CountryCapital("Узбекистан", "Ташкент", Region.ASIA, 2),
    CountryCapital("Киргизия", "Бишкек", Region.ASIA, 2),
    CountryCapital("Таджикистан", "Душанбе", Region.ASIA, 2),
    CountryCapital("Туркменистан", "Ашхабад", Region.ASIA, 2),
    CountryCapital("Монголия", "Улан-Батор", Region.ASIA, 2),
    CountryCapital("Южная Корея", "Сеул", Region.ASIA, 2),
    CountryCapital("Таиланд", "Бангкок", Region.ASIA, 2),
    CountryCapital("Вьетнам", "Ханой", Region.ASIA, 2),
    CountryCapital("Филиппины", "Манила", Region.ASIA, 2),
    CountryCapital("Малайзия", "Куала-Лумпур", Region.ASIA, 2),
    CountryCapital("Иран", "Тегеран", Region.ASIA, 2),
    CountryCapital("Ирак", "Багдад", Region.ASIA, 2),
    CountryCapital("Саудовская Аравия", "Эр-Рияд", Region.ASIA, 2),
    CountryCapital("Непал", "Катманду", Region.ASIA, 2),

    // Уровень 3 — редкие страны: Океания, Африка, Латинская Америка (33).
    CountryCapital("Новая Зеландия", "Веллингтон", Region.OCEANIA, 3),
    CountryCapital("Фиджи", "Сува", Region.OCEANIA, 3),
    CountryCapital("Папуа — Новая Гвинея", "Порт-Морсби", Region.OCEANIA, 3),
    CountryCapital("Самоа", "Апиа", Region.OCEANIA, 3),
    CountryCapital("Тонга", "Нукуалофа", Region.OCEANIA, 3),
    CountryCapital("Вануату", "Порт-Вила", Region.OCEANIA, 3),
    CountryCapital("Марокко", "Рабат", Region.AFRICA, 3),
    CountryCapital("Кения", "Найроби", Region.AFRICA, 3),
    CountryCapital("Эфиопия", "Аддис-Абеба", Region.AFRICA, 3),
    CountryCapital("Нигерия", "Абуджа", Region.AFRICA, 3),
    CountryCapital("Гана", "Аккра", Region.AFRICA, 3),
    CountryCapital("Сенегал", "Дакар", Region.AFRICA, 3),
    CountryCapital("Мали", "Бамако", Region.AFRICA, 3),
    CountryCapital("Ангола", "Луанда", Region.AFRICA, 3),
    CountryCapital("Зимбабве", "Хараре", Region.AFRICA, 3),
    CountryCapital("Замбия", "Лусака", Region.AFRICA, 3),
    CountryCapital("Уганда", "Кампала", Region.AFRICA, 3),
    CountryCapital("Мадагаскар", "Антананариву", Region.AFRICA, 3),
    CountryCapital("Мозамбик", "Мапуту", Region.AFRICA, 3),
    CountryCapital("Намибия", "Виндхук", Region.AFRICA, 3),
    CountryCapital("Судан", "Хартум", Region.AFRICA, 3),
    CountryCapital("Камерун", "Яунде", Region.AFRICA, 3),
    CountryCapital("Перу", "Лима", Region.AMERICAS, 3),
    CountryCapital("Чили", "Сантьяго", Region.AMERICAS, 3),
    CountryCapital("Колумбия", "Богота", Region.AMERICAS, 3),
    CountryCapital("Венесуэла", "Каракас", Region.AMERICAS, 3),
    CountryCapital("Эквадор", "Кито", Region.AMERICAS, 3),
    CountryCapital("Уругвай", "Монтевидео", Region.AMERICAS, 3),
    CountryCapital("Парагвай", "Асунсьон", Region.AMERICAS, 3),
    CountryCapital("Куба", "Гавана", Region.AMERICAS, 3),
    CountryCapital("Ямайка", "Кингстон", Region.AMERICAS, 3),
    CountryCapital("Коста-Рика", "Сан-Хосе", Region.AMERICAS, 3),
    CountryCapital("Никарагуа", "Манагуа", Region.AMERICAS, 3),
)
