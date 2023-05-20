import com.example.util.simpletimetracker.Deps
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

applyAndroidLibrary()

dependencies {
    implementation(project(":core"))
    implementation(project(":feature_running_records"))
    implementation(project(":feature_records"))
    implementation(project(":feature_statistics"))
    implementation(project(":feature_settings"))
    implementation(Deps.Google.dagger)
    kapt(Deps.Kapt.dagger)
}
