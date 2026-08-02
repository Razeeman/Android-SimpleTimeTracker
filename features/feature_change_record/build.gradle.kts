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
    namespace = "${Base.namespace}.feature_change_record"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":feature_change_record:api"))
    implementation(project(":feature_comment_selection:api"))
    implementation(project(":feature_dialogs:api"))
    implementation(libs.google.dagger)
    ksp(libs.kapt.dagger)
}
