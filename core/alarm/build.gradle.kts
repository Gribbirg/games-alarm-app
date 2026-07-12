plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
}

android {
    namespace = "com.example.smartalarm.core.alarm"
}

dependencies {
    // AlarmData — параметр публичного API AlarmCreateRepository
    api(project(":core:data"))
    implementation(project(":core:common"))
}
