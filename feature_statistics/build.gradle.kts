import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.deps

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
}

android {
    compileSdkVersion(Base.currentSDK)

    defaultConfig {
        minSdkVersion(Base.minSDK)
        targetSdkVersion(Base.currentSDK)
        versionCode = Base.versionCode
        versionName = Base.versionName
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))
    implementation(project(":navigation"))

    implementation(deps.androidx.appcompat)
    implementation(deps.androidx.constraintlayout)
    implementation(deps.androidx.recyclerview)
    implementation(deps.androidx.cardView)
    implementation(deps.androidx.material)
    implementation(deps.google.dagger)
    implementation(deps.ktx.fragment)
    implementation(deps.ktx.livedata)
    implementation(deps.ktx.viewmodel)
    kapt(deps.kapt.dagger)

    testImplementation(deps.test.junit)
    androidTestImplementation(deps.uitest.junit)
    androidTestImplementation(deps.uitest.espresso)
}
