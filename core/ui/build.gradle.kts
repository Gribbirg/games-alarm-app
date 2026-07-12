plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
}

android {
    namespace = "com.example.smartalarm.core.ui"

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Модели AccountData/RecordInternetData — в публичном API AllRecordsAdapter
    api(project(":core:data"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.material)
    implementation(libs.glide)
}
