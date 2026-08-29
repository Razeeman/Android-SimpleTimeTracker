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
    namespace = "${Base.namespace}.feature_change_reminder"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":feature_dialogs:api"))
    implementation(libs.google.dagger)
    ksp(libs.kapt.dagger)
}
