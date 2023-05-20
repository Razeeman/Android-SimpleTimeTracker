import com.example.util.simpletimetracker.Deps
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

applyAndroidLibrary()

android {
    buildTypes {
        getByName("debug") {
            buildConfigField("String", "VERSION_NAME", "\"${defaultConfig.versionName}\"")
        }
        getByName("release") {
            buildConfigField("String", "VERSION_NAME", "\"${defaultConfig.versionName}\"")
        }
    }
}

dependencies {
    api(project(":domain"))
    api(project(":navigation"))
    api(project(":feature_base_adapter"))
    api(project(":feature_views"))

    api(Deps.Androidx.appcompat)
    api(Deps.Androidx.recyclerView)
    api(Deps.Androidx.constraintLayout)
    api(Deps.Androidx.cardView)
    api(Deps.Androidx.material)
    api(Deps.Androidx.lifecycleExtensions)
    api(Deps.Androidx.viewpager2)
    api(Deps.Emoji.emojiBundled)
    api(Deps.Google.flexBox)
    api(Deps.Google.dagger)
    api(Deps.Ktx.core)
    api(Deps.Ktx.fragment)
    api(Deps.Ktx.liveDataCore)
    api(Deps.Ktx.liveData)
    api(Deps.Ktx.viewModel)
    api(Deps.UiTest.espressoIdling)

    testImplementation(Deps.Test.junit)
    testImplementation(Deps.Test.mockito)
    testImplementation(Deps.Test.mockitoInline)
}
