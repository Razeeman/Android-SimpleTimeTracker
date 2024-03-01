import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.Deps
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.feature_wear"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":wearrpc"))
    implementation(Deps.Google.services)
    implementation(Deps.Google.gson)
    implementation(Deps.Google.dagger)
    kapt(Deps.Kapt.dagger)

    testImplementation(Deps.Test.junit)
    testImplementation(Deps.Test.coroutines)
}
