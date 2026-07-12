plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dokka)
}

android {
    namespace = "com.example.smartalarm.core.data"
}

dependencies {
    // Room: сущности и DAO — часть публичного API модуля
    api(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // MutableLiveData в публичном API репозиториев
    api(libs.androidx.lifecycle.livedata.ktx)

    // Firebase и Google-аккаунт: типы видны из AuthRepository
    api(platform(libs.firebase.bom))
    api(libs.firebase.auth)
    api(libs.firebase.database)
    api(libs.play.services.auth)

    implementation(libs.kotlinx.coroutines.android)
}
