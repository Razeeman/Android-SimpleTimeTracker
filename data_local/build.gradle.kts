import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    alias(libs.plugins.gradleLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.data_local"
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.room)
    implementation(libs.ktx.room)

    ksp(libs.kapt.room)
    ksp(libs.kapt.dagger)

    testImplementation(libs.test.junit)
}
