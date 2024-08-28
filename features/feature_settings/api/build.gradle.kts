import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.feature_settings.api"
}

dependencies {
    implementation(project(":core"))
}
