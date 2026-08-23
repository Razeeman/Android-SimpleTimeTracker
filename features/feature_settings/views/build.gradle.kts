import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    alias(libs.plugins.gradleLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ksp)
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.feature_settings.views"
}

dependencies {
    implementation(project(":feature_settings:api"))
    implementation(project(":feature_views"))
    implementation(project(":feature_base_adapter"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintLayout)
    implementation(libs.androidx.cardView)
    implementation(libs.androidx.material)
    implementation(libs.google.flexBox)
}
