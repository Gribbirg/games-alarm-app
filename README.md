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

```groovy
dependencies {

  // Lifecycle
  implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.6.1'
  implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1'
  implementation 'androidx.lifecycle:lifecycle-extensions:2.2.0'

  // Firebase
  implementation 'com.google.firebase:firebase-auth-ktx:22.0.0'
  implementation 'com.google.firebase:firebase-database-ktx:20.2.1'
  implementation 'com.google.firebase:firebase-auth:22.0.0'

  // Google play auth
  implementation 'com.google.android.gms:play-services-auth:20.5.0'

  // Glide
  implementation 'com.github.bumptech.glide:glide:4.13.2'

  // Room
  def room_version = "2.5.1"
  implementation "androidx.room:room-runtime:$room_version"
  kapt "androidx.room:room-compiler:$room_version"
  implementation "androidx.room:room-rxjava2:$room_version"

  // Navigation
  def nav_version = "2.5.3"
  implementation "androidx.navigation:navigation-fragment-ktx:$nav_version"
  implementation "androidx.navigation:navigation-ui-ktx:$nav_version"

  // UI
  implementation 'androidx.appcompat:appcompat:1.6.1'
  implementation 'com.google.android.material:material:1.9.0'
  implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

  // Retrofit
  implementation 'com.squareup.retrofit2:retrofit:2.9.0'
  implementation 'com.squareup.retrofit2:converter-scalars:2.9.0'

  // Kotlin
  implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))

  // Test
  testImplementation 'junit:junit:4.13.2'
  androidTestImplementation 'androidx.test.ext:junit:1.1.5'
  androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'

  // Other
  implementation 'androidx.core:core-ktx:1.10.1'
  implementation 'androidx.legacy:legacy-support-v4:1.0.0'
}
```

## Contributors

<a href="https://github.com/Gribbirg/games-alarm-app/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=Gribbirg/games-alarm-app" alt="contributor" />
</a>
<a href="https://github.com/aviafaviaf">
  <img src="https://contrib.rocks/image?repo=aviafaviaf/weather" alt="contributor" />
</a>