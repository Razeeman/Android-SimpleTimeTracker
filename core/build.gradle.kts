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
    api(project(":domain"))
    api(project(":navigation"))

    api(Deps.Androidx.appcompat)
    api(Deps.Androidx.recyclerView)
    api(Deps.Androidx.constraintLayout)
    api(Deps.Androidx.cardView)
    api(Deps.Androidx.material)
    api(Deps.Androidx.lifecycleExtensions)
    api(Deps.Androidx.viewpager2)
    api(Deps.Emoji.emojiCompat)
    api(Deps.Emoji.emojiBundled)
    api(Deps.Google.flexBox)
    api(Deps.Google.dagger)
    api(Deps.Ktx.core)
    api(Deps.Ktx.fragment)
    api(Deps.Ktx.liveDataCore)
    api(Deps.Ktx.liveData)
    api(Deps.Ktx.viewModel)
    api(Deps.Ktx.liveData)
    api(Deps.Ktx.viewModel)
    api(Deps.UiTest.espressoIdling)

    testImplementation(Deps.Test.junit)
}
