# РазБудильник

[![en](https://img.shields.io/badge/lang-en-blue.svg)](README.md)
[![ru](https://img.shields.io/badge/lang-ru-red.svg)](README.ru.md)

- Андроид-приложение;
- Будильник с играми;
- Выпускной проект трека "Мобильная разработка" образовательной программы "IT Академия Samsung".

### Фишки

1. Для отключения будильника необходимо пройти игру;
2. Игры *(на данный момент одна)* имеют уровни сложности;
3. Прохождение игры оценивается очками;
4. Имеется возможность просмотреть свои результаты, а так же поделиться ими;
5. Удобный интерфейс для установки различных будильников на дни недели;
6. Возможность копирования будильников;
7. Сохранение будильников на облаке *(по кнопке)*;
8. Цвета приложения подстраиваются под системные *(Dynamic Colors)*.

### Планы

1. Увеличение количества игр;
2. Система друзей и команд;
3. Авторизация через почту;
4. Настраиваемое окно приветствия *(отображать погоду, новости)*;
5. Критерии учета результата *(защита от "фарма" очков)*.

### Установка

Apk-файл доступен в <a href="https://github.com/Gribbirg/games-alarm-app/releases">последнем
релизе</a>.

Сборка проекта, используя терминал:

```
./gradlew build
```

Также вы можете собрать apk используя docker контейнер. Для начала запустите docker и соберите образ:

```
docker build . -t games-alarm-app
```

Запустите контейнер:

```
docker run -d --name games-alarm-app games-alarm-app sleep infinity
```

Скопируйте apk:

```
docker cp games-alarm-app:/opt/project/app/build/outputs/apk/debug/app-debug.apk /games-alarm-app-debug.apk
```

Остановите и удалите контейнер:

```
docker stop games-alarm-app
docker rm games-alarm-app
```

## Скриншоты

<img src="https://github.com/Gribbirg/games-alarm-app/assets/115590353/1a842c82-3be6-4812-a4ce-7ead381bdf88" width="240px" alt="Screenshot" />
<img src="https://github.com/Gribbirg/games-alarm-app/assets/115590353/cdb1ab37-a6c4-4e94-824b-ac1d5c1805c8" width="240px" alt="Screenshot" />
<img src="https://github.com/Gribbirg/games-alarm-app/assets/115590353/33ffa7e6-ae14-4d7f-b9be-020556cbab20" width="240px" alt="Screenshot" />
<img src="https://github.com/Gribbirg/games-alarm-app/assets/115590353/a295d149-216d-441f-8622-35f0281cf1d7" width="240px" alt="Screenshot" />
<img src="https://github.com/Gribbirg/games-alarm-app/assets/115590353/43097668-7527-4440-a98e-a8724dba6a45" width="240px" alt="Screenshot" />
<img src="https://github.com/Gribbirg/games-alarm-app/assets/115590353/6c58916c-bc64-4953-9741-a9c1e148e658" width="240px" alt="Screenshot" />
<img src="https://github.com/Gribbirg/games-alarm-app/assets/115590353/0255233c-5466-4746-bd79-5030e05a34d2" width="240px" alt="Screenshot" />

## Видео

[Кликните](https://github.com/Gribbirg/games-alarm-app/assets/115590353/b7cd536a-2099-4cb9-a9fc-2116b01fdb30)

## Технологический стек

1. Kotlin;
2. Firebase;
3. Room;
4. MVVM;
5. Kotlin coroutines;
6. Material design 3;
7. Navigation;
8. Google auth;
9. Alarm Manager;
10. Retrofit.

## Зависимости

Версии зависимостей управляются через version catalog Gradle: см.
[`gradle/libs.versions.toml`](gradle/libs.versions.toml). Основные библиотеки:

```toml
appcompat = "1.7.1"
constraintlayout = "2.2.1"
coreKtx = "1.19.0"
coroutines = "1.11.0"
firebaseBom = "34.15.0"
glide = "5.0.9"
lifecycle = "2.11.0"
material = "1.12.0"
navigation = "2.9.8"
playServicesAuth = "21.3.0"
room = "2.8.4"
```

## Разработчики

<a href="https://github.com/Gribbirg/games-alarm-app/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=Gribbirg/games-alarm-app" alt="contributor" />
</a>
<a href="https://github.com/aviafaviaf">
  <img src="https://contrib.rocks/image?repo=aviafaviaf/weather" alt="contributor" />
</a>