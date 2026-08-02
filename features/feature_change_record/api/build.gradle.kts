import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    alias(libs.plugins.gradleLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ksp)
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.feature_change_record.api"
}

dependencies {
    implementation(project(":feature_comment_selection:api"))
    implementation(project(":core"))
}
