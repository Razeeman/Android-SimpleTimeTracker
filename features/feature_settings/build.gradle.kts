import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    alias(libs.plugins.gradleLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinParcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.feature_settings"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":feature_settings:api"))
    implementation(project(":feature_settings:views"))
    implementation(project(":feature_records:api"))
    implementation(project(":feature_statistics:api"))
    implementation(project(":feature_statistics_detail:api"))
    implementation(libs.google.dagger)
    ksp(libs.kapt.dagger)
}
