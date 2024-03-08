/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.Deps
import com.example.util.simpletimetracker.Versions
import com.example.util.simpletimetracker.applyAndroidWearLibrary

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

applyAndroidWearLibrary()

android {
    namespace = Base.namespace

    defaultConfig {
        applicationId = Base.applicationId
        versionCode = Base.versionCodeWear
        versionName = Base.versionNameWear
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-debug-rules.pro",
            )
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.compose_kotlin_compiler
    }
}

dependencies {
    implementation(project(":wear_api"))
    implementation(project(":resources"))

    implementation(Deps.Androidx.appcompat)
    implementation(Deps.Google.services)
    implementation(Deps.Google.gson)
    implementation(Deps.Google.dagger)
    coreLibraryDesugaring(Deps.Google.desugaring)
    implementation(Deps.Compose.activity)
    implementation(Deps.Compose.ui)
    implementation(Deps.Compose.uiToolingPreview)
    implementation(Deps.Compose.materialIcons)
    implementation(Deps.Compose.wearNavigation)
    implementation(Deps.Compose.wearMaterial)
    implementation(Deps.Compose.wearFoundation)
    implementation(Deps.Compose.wearToolingPreview)
    implementation(Deps.Compose.horologist)
    implementation(Deps.Compose.hilt)
    debugImplementation(Deps.Compose.uiTooling)
    kapt(Deps.Kapt.dagger)

    testImplementation(Deps.Test.junit)
    testImplementation(Deps.Test.coroutines)

}