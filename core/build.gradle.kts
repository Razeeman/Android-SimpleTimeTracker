import com.example.util.simpletimetracker.Base
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
    namespace = "${Base.namespace}.core"
}

// TODO remove api
dependencies {
    api(project(":domain"))
    api(project(":navigation"))
    api(project(":resources"))
    api(project(":feature_base_adapter"))
    api(project(":feature_views"))

    api(Deps.Androidx.appcompat)
    api(Deps.Androidx.recyclerView)
    api(Deps.Androidx.constraintLayout)
    api(Deps.Androidx.cardView)
    api(Deps.Androidx.material)
    api(Deps.Androidx.viewpager2)
    api(Deps.Emoji.emojiBundled)
    api(Deps.Google.flexBox)
    api(Deps.Google.dagger)
    api(Deps.Ktx.core)
    api(Deps.Ktx.fragment)
    api(Deps.Ktx.liveDataCore)
    api(Deps.Ktx.liveData)
    api(Deps.Ktx.viewModel)
    api(Deps.Ktx.activity)
    api(Deps.UiTest.espressoIdling)

    testImplementation(Deps.Test.junit)
    testImplementation(Deps.Test.mockito)
    testImplementation(Deps.Test.mockitoInline)
}
