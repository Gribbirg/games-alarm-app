import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension

// Корневой скрипт: версии плагинов и общие Android-настройки всех модулей.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.dokka)
}

// Общие настройки Android для всех модулей (:app и библиотеки). Kotlin
// компилируется встроенной поддержкой AGP (built-in Kotlin), отдельный
// плагин org.jetbrains.kotlin.android не нужен; jvmTarget берётся из
// compileOptions.targetCompatibility. Блоки для application и library
// продублированы: общий предок CommonExtension генерик, и его сигнатура
// меняется между версиями AGP.
subprojects {
    plugins.withId("com.android.application") {
        extensions.configure<ApplicationExtension> {
            compileSdk = 37
            defaultConfig {
                minSdk = 26
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
        dependencies.add("implementation", libs.androidx.core.ktx)
    }
    plugins.withId("com.android.library") {
        extensions.configure<LibraryExtension> {
            compileSdk = 37
            defaultConfig {
                minSdk = 26
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
        dependencies.add("implementation", libs.androidx.core.ktx)
    }
}
