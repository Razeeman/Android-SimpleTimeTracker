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

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))
    implementation(project(":feature_running_records"))
    implementation(project(":feature_records"))
    implementation(project(":feature_statistics"))
    implementation(project(":feature_settings"))

    implementation(Deps.Androidx.appcompat)
    implementation(Deps.Androidx.constraintLayout)
    implementation(Deps.Androidx.recyclerView)
    implementation(Deps.Androidx.viewpager2)
    implementation(Deps.Androidx.material)
    implementation(Deps.Google.dagger)
    implementation(Deps.Ktx.fragment)
    implementation(Deps.Ktx.liveData)
    implementation(Deps.Ktx.viewModel)
    kapt(Deps.Kapt.dagger)

    testImplementation(Deps.Test.junit)
    androidTestImplementation(Deps.UiTest.junit)
    androidTestImplementation(Deps.UiTest.espresso)
}
