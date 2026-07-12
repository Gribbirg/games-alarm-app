# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

"Alarm with games" (project name `SmartAlarm`) — an Android alarm clock app where the user must complete a mini-game to turn off the alarm. Single-module Gradle project (`:app`), Kotlin, package `com.example.smartalarm`. Graduation project of the Samsung IT Academy mobile development track; parts of the UI text are hardcoded in Russian.

## Build and Test Commands

Requires an Android SDK (compileSdk 33) and JDK. Gradle wrapper is 8.2, AGP 8.2.2, Kotlin 1.7.10.

```bash
./gradlew build                    # full build + unit tests + lint
./gradlew assembleDebug            # debug APK -> app/build/outputs/apk/debug/app-debug.apk
./gradlew test                     # JVM unit tests (app/src/test)
./gradlew test --tests "com.example.smartalarm.ExampleUnitTest"   # single test class
./gradlew connectedAndroidTest     # instrumented tests (needs device/emulator)
./gradlew lint                     # Android lint
./gradlew dokkaHtml                # generate KDoc docs (Dokka plugin is applied)
```

CI rules for Claude:

- After pushing to a PR (or a branch that will become one), wait for the `Build` workflow run on that commit to finish and report its result — never hand work back to the user with the build still pending or failing.
- Version bumps: `minor` is the default for regular releases, `patch` is for hotfixes only, `major` is reserved for breaking/disastrous changes.
- Every PR MUST add a changelog fragment: one new file `changelogs/unreleased/<short-slug>.md` with bullet(s) in Russian describing what changed **from the user's point of view** — no code details, class names, or refactoring notes. If the PR is purely technical, still add a file with a line like `- Технические улучшения стабильности и сборки.` See `changelogs/README.md` for the format.

CI (GitHub Actions, `.github/workflows/`): `build.yml` builds a debug APK with a `<version>-SNAPSHOT-<sha>` version on every push to `master` and every PR; `release.yml` (manual `workflow_dispatch`, choose major/minor/patch) bumps `version.properties`, assembles the fragments from `changelogs/unreleased/` into a new `CHANGELOG.md` section (deleting the fragments), commits + tags `v<X.Y.Z>`, builds a release APK and publishes a GitHub Release whose body is that changelog section. The app version lives in `version.properties` (root) and is read by `app/build.gradle`; CI overrides it via `-PappVersionName`/`-PappVersionCode`. Release signing uses the `KEYSTORE_BASE64`/`KEYSTORE_PASSWORD`/`KEY_ALIAS`/`KEY_PASSWORD` secrets, falling back to the debug key when unset. The Dockerfile builds a debug APK in a container (`docker build . -t games-alarm-app`, see README.md for the copy-out steps).

Firebase config (`app/google-services.json`) is committed, so the `com.google.gms.google-services` plugin works out of the box.

## Architecture

MVVM with LiveData. The layers, all under `app/src/main/java/com/example/smartalarm/`:

- **`ui/`** — two activities, each hosting its own Navigation graph:
  - `MainActivity` + `res/navigation/nav_graph.xml`: main app (alarms list, add/edit alarm, records, profile, settings).
  - `GamesActivity` + `res/navigation/nav_game_graph.xml`: launched when an alarm fires; hosts the game flow (`LoadGameFragment` → game fragment, e.g. `CalcGameFragment` → `GameResultFragment`).
  - Fragments use ViewBinding and get state from `ui/viewmodels/*ViewModel` classes exposing `MutableLiveData`.
- **`data/repositories/`** — plain classes (no DI framework), instantiated by ViewModels:
  - `AlarmDbRepository` — Room access.
  - `AlarmCreateRepository` — schedules/cancels system alarms via `AlarmManager.setExactAndAllowWhileIdle`, targeting `AlarmReceiver` with intent extras keyed by string literals ("alarm id", "alarm time", ...). Those same keys are read in `AlarmReceiver` and `GamesActivity` — keep them in sync.
  - `AuthRepository` — Google sign-in (Play Services auth + Firebase Auth).
  - `UsersRealtimeDatabaseRepository` — singleton `object` writing users/records/saved alarms to Firebase Realtime Database (the database URL is hardcoded there).
  - `CalendarRepository` — week-based calendar logic for the alarms-by-day-of-week UI; holiday highlighting comes from the hardcoded `data/constants/Holidays.kt` (Russian holidays, 2024).
- **`data/db/`** — Room database `AlarmsDB` (singleton, version 21, `fallbackToDestructiveMigration` — schema changes wipe user data). Entities: `AlarmSimpleData`, `AlarmInfoData`, `AlarmUserGamesData`, `GameData`, `RecordsData`; single DAO `AlarmsDao`. An `onOpen` callback seeds the games table from `data/constants/AllGamesData.kt` (`ALL_GAMES`) and resets all tables if the game list size changed — adding a new game means adding it to `ALL_GAMES`.
- **`data/data/`** — non-persisted models (e.g. `AlarmData` aggregates the Room entities; `AccountData`, `RecordInternetData` are the Firebase payloads).
- **`services/`** — `AlarmReceiver` (BroadcastReceiver: posts the full-screen notification, starts `GamesActivity`, kicks off sound) plus `AlarmMediaPlayer` and `AlarmVibrator` singletons that own playback/vibration state across the alarm flow.

Alarm lifecycle end-to-end: alarm saved in Room → `AlarmCreateRepository.create()` registers it with `AlarmManager` → `AlarmReceiver.onReceive` fires at the scheduled time → notification + `GamesActivity` + `AlarmMediaPlayer.playAudio` → user finishes the game → result recorded (`RecordsData`, optionally Firebase) and sound stops.

## Gotchas

- Both nav graphs reference `TaskGameFragment`, which has no source file — nav-graph class names are resolved at runtime, so the build passes, but navigating to that destination will crash. Don't treat it as an existing class.
- Room migrations are destructive; bumping the DB version deletes all user alarms/records.
- Public classes/functions carry KDoc comments (Dokka is used to generate docs) — follow that style for new code in `data/` and `services/`.
