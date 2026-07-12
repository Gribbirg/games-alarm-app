import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.dokka)
}

// Версия приложения хранится в version.properties (корень репозитория) и
// бампится релизным CI. CI также может переопределить её через
// -PappVersionName=... -PappVersionCode=... (снепшот-сборки).
val versionProps = Properties().apply {
    rootProject.file("version.properties").inputStream().use { load(it) }
}
val appVersionName = (project.findProperty("appVersionName") ?: versionProps.getProperty("VERSION_NAME")).toString()
val appVersionCode = (project.findProperty("appVersionCode") ?: versionProps.getProperty("VERSION_CODE")).toString().toInt()

android {
    namespace = "com.example.smartalarm"

    defaultConfig {
        applicationId = "com.example.smartalarm"
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName
    }

    signingConfigs {
        // Ключ подписи передаётся релизным CI через переменные окружения.
        // Без них release-сборка подписывается debug-ключом, чтобы APK
        // оставался устанавливаемым.
        create("release") {
            if (System.getenv("KEYSTORE_FILE") != null) {
                storeFile = file(System.getenv("KEYSTORE_FILE"))
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (System.getenv("KEYSTORE_FILE") != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Модули приложения
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:alarm"))
    implementation(project(":core:ui"))
    implementation(project(":feature:alarms"))
    implementation(project(":feature:games"))
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
    implementation(project(":feature:games:reaction"))
    implementation(project(":feature:games:counter"))
    implementation(project(":feature:games:lights"))
    implementation(project(":feature:games:fifteen"))
    implementation(project(":feature:games:chain"))
    implementation(project(":feature:games:roman"))
    implementation(project(":feature:games:clock"))
    implementation(project(":feature:games:digits"))
    implementation(project(":feature:games:hanoi"))
    implementation(project(":feature:games:targetsum"))
    implementation(project(":feature:records"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:settings"))

    // UI и навигация хост-активностей
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
