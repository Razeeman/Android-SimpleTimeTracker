import com.example.util.simpletimetracker.Deps
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

applyAndroidLibrary()

dependencies {
    implementation(project(":feature_views"))
    implementation(project(":domain"))

    implementation(Deps.Androidx.recyclerView)
    implementation(Deps.Androidx.constraintLayout)
    implementation(Deps.Androidx.cardView)
    implementation(Deps.Androidx.material)
    implementation(Deps.Google.flexBox)
    implementation(Deps.Ktx.core)
}
