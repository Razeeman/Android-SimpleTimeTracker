import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.Deps

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    compileSdk = Base.currentSDK

    defaultConfig {
        minSdk = Base.minSDK
        targetSdk = Base.currentSDK
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":feature_views"))
    implementation(project(":domain"))

    implementation(Deps.Androidx.recyclerView)
    implementation(Deps.Androidx.constraintLayout)
    implementation(Deps.Androidx.cardView)
    implementation(Deps.Androidx.material)
    implementation(Deps.Google.flexBox)
}
