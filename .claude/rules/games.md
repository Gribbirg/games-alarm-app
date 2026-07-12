# Игры

## Структура

- Каждая мини-игра — отдельный модуль `:feature:games:<имя>` (пакет
  `com.example.smartalarm.feature.games.<имя>`): фрагмент игры, её ViewModel,
  модели и ресурсы. Образец — `:feature:games:calc`.
- Хаб `:feature:games` (выбор игр, загрузка, результат) НЕ зависит от модулей
  игр: навигация к игре идёт через nav-графы по имени класса.
- `:feature:games:demo` — демо-приложение со всеми мини-играми: любую можно
  запустить сразу, без будильника и Firebase. Каждая игра ОБЯЗАНА быть
  добавлена в демо; PR с новой игрой без неё не принимается.
  Запуск: `./gradlew :feature:games:demo:installDebug`. В основной APK демо
  не входит (`:app` от него не зависит); CI собирает его вместе со всеми
  модулями.

## Контракт экрана игры

Аргументы фрагмента (ключи с пробелами — исторические, не менять):
`alarm id` (Long), `start time` (Long, 0 = проба), `test` (Boolean — запуск
из выбора игр, а не будильником), `difficulty` (Int 1..3), `music path`
(String).

Обязанности (см. `CalcGameFragment` как образец):

- в `onResume` убрать уведомление будильника (кроме `test`);
- в `onPause` перезапустить будильник через ViewModel, если игра не пройдена
  (кроме `test`);
- каждые две минуты бездействия заново включать мелодию (`AlarmMediaPlayer`);
- по системной «назад» возвращаться к выбору игр;
- при победе положить в аргументы `time` (String), `score` (Int), `game id`
  (Int — id из `ALL_GAMES`) и перейти к результату; в `test` и обычном режиме
  это разные action'ы, так как игра есть в обоих nav-графах.

## Чеклист новой игры

1. Модуль `:feature:games:<имя>`: `include` в `settings.gradle`,
   `implementation project(...)` в `app/build.gradle`.
2. Запись в `ALL_GAMES` (`:core:data`). ВНИМАНИЕ: изменение размера списка
   игр сбрасывает БД у пользователей при следующем открытии.
3. Destination + action'ы в ОБОИХ nav-графах `:core:ui`
   (`nav_graph.xml` — режим пробы, `nav_game_graph.xml` — будильник),
   по образцу калькулятора.
4. Ветки для id игры в `LoadGameFragment` (`when (it[0])`) и
   `GameChoiceFragment` (`when (gameData.id)`) в `:feature:games`.
5. Демо: зависимость в `feature/games/demo/build.gradle`, destination с
   переиспользованными id в `nav_games_demo_graph.xml` (action'ы игры
   перенаправляются на экраны демо) и строка в `DEMO_GAMES`
   (`DemoLauncherFragment`).
6. Changelog-фрагмент (см. `changelog.md`).
