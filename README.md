# Alarm with games

[![en](https://img.shields.io/badge/lang-en-blue.svg)](README.md)
[![ru](https://img.shields.io/badge/lang-ru-red.svg)](README.ru.md)

- Android application;
- Graduation project of the track "Mobile Development" of the educational program "IT Academy
  Samsung".

### Features

1. To turn off the alarm, you must complete the game;
2. Games *(currently one)* have difficulty levels;
3. Completion of the game is assessed by points;
4. It is possible to view your results, as well as share them;
5. Convenient interface for setting various alarms for days of the week;
6. Ability to copy alarms;
7. Saving alarms on the cloud *(by button)*;
8. Application colors are adjusted to the system ones *(Dynamic Colors)*.

### Future plans

1. Increase in the number of games;
2. System of friends and teams;
3. Authorization via email;
4. Customizable welcome window after the alarm *(display weather, news)*;
5. Criteria for taking into account the result *(protection from “farm” points)*.

### Installation

Apk file is available via <a href="https://github.com/Gribbirg/games-alarm-app/releases">latest
release</a>.

Build project from command line:

```
./gradlew build
```

Also you can build the apk using docker container. First launch docker and build image:

```
docker build . -t games-alarm-app
```

Then run a container:

```
docker run -d --name games-alarm-app games-alarm-app sleep infinity
```

Copy apk:

```
docker cp games-alarm-app:/opt/project/app/build/outputs/apk/debug/app-debug.apk /games-alarm-app-debug.apk
```

Stop and delete container:

```
docker stop games-alarm-app
docker rm games-alarm-app
```

## Screenshots

<img src="https://github.com/Gribbirg/games-alarm-app/assets/115590353/1a842c82-3be6-4812-a4ce-7ead381bdf88" width="240px" alt="Screenshot" />
<img src="https://github.com/Gribbirg/games-alarm-app/assets/115590353/cdb1ab37-a6c4-4e94-824b-ac1d5c1805c8" width="240px" alt="Screenshot" />
<img src="https://github.com/Gribbirg/games-alarm-app/assets/115590353/33ffa7e6-ae14-4d7f-b9be-020556cbab20" width="240px" alt="Screenshot" />
<img src="https://github.com/Gribbirg/games-alarm-app/assets/115590353/a295d149-216d-441f-8622-35f0281cf1d7" width="240px" alt="Screenshot" />
<img src="https://github.com/Gribbirg/games-alarm-app/assets/115590353/43097668-7527-4440-a98e-a8724dba6a45" width="240px" alt="Screenshot" />
<img src="https://github.com/Gribbirg/games-alarm-app/assets/115590353/6c58916c-bc64-4953-9741-a9c1e148e658" width="240px" alt="Screenshot" />
<img src="https://github.com/Gribbirg/games-alarm-app/assets/115590353/0255233c-5466-4746-bd79-5030e05a34d2" width="240px" alt="Screenshot" />

## Video

[Click here](https://github.com/Gribbirg/games-alarm-app/assets/115590353/b7cd536a-2099-4cb9-a9fc-2116b01fdb30)

## Stack

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

## Dependencies

Dependency versions are managed with the Gradle version catalog: see
[`gradle/libs.versions.toml`](gradle/libs.versions.toml). Key libraries:

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

## Contributors

<a href="https://github.com/Gribbirg/games-alarm-app/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=Gribbirg/games-alarm-app" alt="contributor" />
</a>
<a href="https://github.com/aviafaviaf">
  <img src="https://contrib.rocks/image?repo=aviafaviaf/weather" alt="contributor" />
</a>