import com.example.util.simpletimetracker.Deps
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

applyAndroidLibrary()

dependencies {
    implementation(project(":domain"))

    implementation(Deps.Ktx.fragment)
    implementation(Deps.Ktx.navigationFragment)
    implementation(Deps.Ktx.navigationUi)
}
