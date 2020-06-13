import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.deps

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
}

android {
    compileSdkVersion(Base.currentSDK)

    defaultConfig {
        applicationId = "com.example.util.simpletimetracker"
        minSdkVersion(Base.minSDK)
        targetSdkVersion(Base.currentSDK)
        versionCode = Base.versionCode
        versionName = Base.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))
    implementation(project(":navigation"))
    implementation(project(":data_local"))
    implementation(project(":feature_main"))
    implementation(project(":feature_running_records"))
    implementation(project(":feature_change_record_type"))
    implementation(project(":feature_records"))
    implementation(project(":feature_change_record"))
    implementation(project(":feature_statistics"))
    implementation(project(":feature_settings"))
    implementation(project(":feature_dialogs"))
    implementation(project(":feature_widget"))

    implementation(deps.androidx.appcompat)
    implementation(deps.androidx.constraintlayout)
    implementation(deps.androidx.recyclerview)
    implementation(deps.androidx.room)
    implementation(deps.androidx.lifecycle_extensions)
    implementation(deps.google.dagger)
    implementation(deps.ktx.core)
    implementation(deps.ktx.fragment)
    implementation(deps.ktx.livedata_core)
    implementation(deps.ktx.livedata)
    implementation(deps.ktx.viewmodel)
    implementation(deps.ktx.navigation_fragment)
    implementation(deps.ktx.navigation_ui)
    kapt(deps.kapt.dagger)

    testImplementation(deps.test.junit)
    androidTestImplementation(deps.uitest.junit)
    androidTestImplementation(deps.uitest.espresso)
}
