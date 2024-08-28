import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.Deps
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.feature_settings.views"
}

dependencies {
    implementation(project(":feature_settings:api"))
    implementation(project(":feature_views"))
    implementation(project(":feature_base_adapter"))

    implementation(Deps.Androidx.appcompat)
    implementation(Deps.Androidx.constraintLayout)
    implementation(Deps.Androidx.cardView)
    implementation(Deps.Androidx.material)
}
