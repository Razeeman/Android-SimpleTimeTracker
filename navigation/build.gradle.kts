import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.deps

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
}

android {
    compileSdkVersion(Base.currentSDK)

    defaultConfig {
        minSdkVersion(Base.minSDK)
        targetSdkVersion(Base.currentSDK)
        versionCode = Base.versionCode
        versionName = Base.versionName
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))

    implementation(deps.google.dagger)
    implementation(deps.ktx.navigation_fragment)
    implementation(deps.ktx.navigation_ui)
    kapt(deps.kapt.dagger)

    testImplementation(deps.test.junit)
    androidTestImplementation(deps.uitest.junit)
    androidTestImplementation(deps.uitest.espresso)
}
