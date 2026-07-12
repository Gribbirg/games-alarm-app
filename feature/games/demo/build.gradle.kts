// Демо-приложение со всеми мини-играми: каждую можно запустить напрямую,
// без будильника и остального приложения. В основной APK не входит.
// Запуск: ./gradlew :feature:games:demo:installDebug
// Новая игра ОБЯЗАНА быть добавлена сюда — см. .claude/rules/games.md.
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.smartalarm.feature.games.demo"

    defaultConfig {
        applicationId = "com.example.smartalarm.feature.games.demo"
        targetSdk = 36
        versionCode = 1
        versionName = "demo"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Все модули игр
    implementation(project(":feature:games:calc"))
    implementation(project(":feature:games:memory"))
    implementation(project(":feature:games:equation"))
    implementation(project(":feature:games:sorting"))
    implementation(project(":feature:games:pairs"))
    implementation(project(":feature:games:sequence"))
    implementation(project(":feature:games:stroop"))
    implementation(project(":feature:games:oddoneout"))
    implementation(project(":feature:games:maze"))
    implementation(project(":feature:games:anagram"))
    implementation(project(":feature:games:truefalse"))

    implementation(project(":core:ui"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material)
}
