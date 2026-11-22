import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    alias(libs.plugins.gradleLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.feature_widget"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":resources"))
    implementation(project(":feature_dialogs:api"))
    implementation(project(":feature_views"))
    implementation(project(":feature_running_records:api"))
    implementation(project(":feature_settings:api"))

    implementation(libs.ktx.navigationFragment)
    implementation(libs.ktx.navigationUi)
    implementation(libs.google.dagger)

    ksp(libs.kapt.dagger)
}
