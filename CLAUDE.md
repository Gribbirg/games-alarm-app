# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

"Alarm with games" (project name `SmartAlarm`) — an Android alarm clock app where the user must complete a mini-game to turn off the alarm. Multi-module Gradle project (`:app` + `core`/`feature` library modules), Kotlin, base package `com.example.smartalarm`. Graduation project of the Samsung IT Academy mobile development track; parts of the UI text are hardcoded in Russian.

## Build and Test Commands

Requires an Android SDK (compileSdk 33) and JDK. Gradle wrapper is 8.2, AGP 8.2.2, Kotlin 1.7.10.

```bash
./gradlew build                    # full build + unit tests + lint
./gradlew assembleDebug            # debug APK -> app/build/outputs/apk/debug/app-debug.apk
./gradlew test                     # JVM unit tests (app/src/test)
./gradlew test --tests "com.example.smartalarm.ExampleUnitTest"   # single test class
./gradlew connectedAndroidTest     # instrumented tests (needs device/emulator)
./gradlew lint                     # Android lint
./gradlew dokkaHtml                # generate KDoc docs (Dokka plugin is applied per module)
```

Rules for Claude live in `.claude/rules/` (auto-loaded): `ci.md` (wait for the `Build` workflow after pushing), `versioning.md` (version bump policy), `changelog.md` (per-PR changelog fragments).

CI (GitHub Actions, `.github/workflows/`): `build.yml` builds a debug APK with a `<version>-SNAPSHOT-<sha>` version on every push to `master` and every PR; `release.yml` (manual `workflow_dispatch`, choose major/minor/patch) bumps `version.properties`, assembles the fragments from `changelogs/unreleased/` into a new `CHANGELOG.md` section (deleting the fragments), commits + tags `v<X.Y.Z>`, builds a release APK and publishes a GitHub Release whose body is that changelog section. The app version lives in `version.properties` (root) and is read by `app/build.gradle`; CI overrides it via `-PappVersionName`/`-PappVersionCode`. Release signing uses the `KEYSTORE_BASE64`/`KEYSTORE_PASSWORD`/`KEY_ALIAS`/`KEY_PASSWORD` secrets, falling back to the debug key when unset. The Dockerfile builds a debug APK in a container (`docker build . -t games-alarm-app`, see README.md for the copy-out steps).

Firebase config (`app/google-services.json`) is committed, so the `com.google.gms.google-services` plugin works out of the box (applied only in `:app`).

## Architecture

MVVM with LiveData, split into Gradle modules. Dependency versions live in `gradle/libs.versions.toml` (version catalog); Android settings shared by every module (compileSdk, minSdk, JVM target, kotlin-bom, core-ktx) are in `gradle/android-module-common.gradle`, applied via `apply from:` after each module's `android { namespace ... }` block. `android.nonTransitiveRClass=false` deliberately: feature code reaches shared resources (`:core:ui` strings/drawables/nav ids) through its own module `R`.

Module dependency direction is strictly `app → feature:* → core:* → (core:data → core:common)`; features never depend on each other.

### Core modules

- **`:core:common`** (`com.example.smartalarm.core.common`) — dependency-free utilities (`RealPathUtil`).
- **`:core:data`** (`com.example.smartalarm.core.data`) — all data layer, no DI framework (repositories are instantiated by ViewModels):
  - `db/` — Room database `AlarmsDB` (singleton, version 21, `fallbackToDestructiveMigration` — schema changes wipe user data). Entities: `AlarmSimpleData`, `AlarmInfoData`, `AlarmUserGamesData`, `GameData`, `RecordsData`; single DAO `AlarmsDao`. An `onOpen` callback seeds the games table from `constants/AllGamesData.kt` (`ALL_GAMES`) and resets all tables if the game list size changed — adding a new game means adding it to `ALL_GAMES`.
  - `model/` — non-persisted models (`AlarmData` aggregates the Room entities; `AccountData`, `RecordInternetData` are the Firebase payloads).
  - `repositories/` — `AlarmDbRepository` (Room access), `AuthRepository` (Google sign-in: Play Services auth + Firebase Auth), `UsersRealtimeDatabaseRepository` (singleton `object` writing to Firebase Realtime Database; the database URL is hardcoded there), `CalendarRepository` (week-based calendar logic + top-level date helpers; holiday highlighting comes from the hardcoded `constants/Holidays.kt`, Russian holidays 2024).
- **`:core:alarm`** (`com.example.smartalarm.core.alarm`) — scheduling and ringing: `AlarmCreateRepository` (schedules/cancels system alarms via `AlarmManager.setExactAndAllowWhileIdle`, targeting `AlarmReceiver`), `AlarmReceiver` (BroadcastReceiver, declared in this module's manifest: posts the full-screen notification, starts the games activity, kicks off sound), `AlarmMediaPlayer`/`AlarmVibrator` singletons owning playback/vibration state. Intent extras keys shared by these classes live in `AlarmIntentKeys` — always use the constants. The module doesn't know the app's activities: `:app` registers `GamesActivity` in `AlarmScreenRouter` from `App.onCreate`.
- **`:core:ui`** (`com.example.smartalarm.core.ui`) — shared UI resources (themes incl. `AppTheme`, colors, strings, common drawables) and both navigation graphs (`nav_graph.xml`, `nav_game_graph.xml` — they reference feature fragments by fully-qualified class name, resolved at runtime, so no code dependency on features), plus `AllRecordsAdapter` used by both records and profile screens.

### Feature modules

Each is `com.example.smartalarm.feature.<name>` with its own fragments, ViewModels (exposing `MutableLiveData`), adapters, layouts and menus; all use ViewBinding (binding classes are generated per module, e.g. `com.example.smartalarm.feature.alarms.databinding.*`):

- **`:feature:alarms`** — alarms list (`AlarmsFragment`) and add/edit alarm (`AddAlarmFragment`).
- **`:feature:games`** — the game *flow hub*: loading (`LoadGameFragment` → game fragment → `GameResultFragment`) and per-alarm game selection (`GameChoiceFragment` + `GameAdapter`); used from both nav graphs. It contains no concrete games and has no code dependency on them — navigation to a game goes through the nav graphs by class name.
- **`:feature:games:calc`** (`com.example.smartalarm.feature.games.calc`) — the arithmetic game (`CalcGameFragment`, `CalcGameViewModel`, `ArifData`). Each game lives in its own module nested under `:feature:games:*`; adding a game means a new module here, an entry in `ALL_GAMES` (`:core:data`) and destinations in both nav graphs (`:core:ui`).
- **`:feature:records`** — local/online records (`RecordsFragment`).
- **`:feature:profile`** — Google account profile (`ProfileFragment`, `ProfileOtherFragment`).
- **`:feature:settings`** — `SettingsFragment`.

### App module

`:app` (`com.example.smartalarm`) is the composition root: `App` (notification channel + `AlarmScreenRouter` registration), two host activities with their layouts and the bottom-nav menu, launcher icons, manifest with all permissions:

- `MainActivity` hosts `nav_graph.xml` (alarms list, add/edit alarm, records, profile, settings).
- `GamesActivity` hosts `nav_game_graph.xml`; launched when an alarm fires.

Alarm lifecycle end-to-end: alarm saved in Room → `AlarmCreateRepository.create()` registers it with `AlarmManager` → `AlarmReceiver.onReceive` fires at the scheduled time → notification + `GamesActivity` (via `AlarmScreenRouter`) + `AlarmMediaPlayer.playAudio` → user finishes the game → result recorded (`RecordsData`, optionally Firebase) and sound stops.

## Gotchas

- Both nav graphs reference `TaskGameFragment` (`com.example.smartalarm.feature.games.task.TaskGameFragment`), which has no source file — nav-graph class names are resolved at runtime, so the build passes, but navigating to that destination will crash. Don't treat it as an existing class.
- Room migrations are destructive; bumping the DB version deletes all user alarms/records.
- Public classes/functions carry KDoc comments (Dokka is used to generate docs) — follow that style for new code in `core` modules.
- New shared resources go to `:core:ui`; resources used by a single feature stay in that feature. The nav graphs live in `:core:ui`, so a new destination means editing the graph there and depending on the action ids via the feature's transitive `R`.
