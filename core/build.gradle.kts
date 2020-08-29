import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.Deps

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
    implementation(project(":navigation"))

    implementation(Deps.Androidx.appcompat)
    implementation(Deps.Androidx.recyclerView)
    implementation(Deps.Androidx.constraintLayout)
    implementation(Deps.Androidx.cardView)
    implementation(Deps.Androidx.material)
    implementation(Deps.Google.flexBox)
    implementation(Deps.Ktx.liveData)
    implementation(Deps.Ktx.viewModel)
    implementation(Deps.UiTest.espressoIdling)

    testImplementation(Deps.Test.junit)
    androidTestImplementation(Deps.UiTest.junit)
    androidTestImplementation(Deps.UiTest.espresso)
}
