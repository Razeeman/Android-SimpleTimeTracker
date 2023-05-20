import com.example.util.simpletimetracker.Deps
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

applyAndroidLibrary()

android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }
}

dependencies {
    implementation(project(":core"))

    implementation(Deps.Androidx.room)
    implementation(Deps.Ktx.room)

    kapt(Deps.Kapt.room)
    kapt(Deps.Kapt.dagger)
}
