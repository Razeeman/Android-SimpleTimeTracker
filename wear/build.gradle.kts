/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.applyAndroidWearLibrary

plugins {
    alias(libs.plugins.gradleApplication)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

applyAndroidWearLibrary()

android {
    namespace = Base.namespace

    defaultConfig {
        applicationId = Base.applicationId
        versionCode = Base.versionCodeWear
        versionName = Base.versionNameWear

        buildConfigField("String", "WEAR_API_VERSION", "\"${Base.wearApiVersion}\"")
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

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.kotlin.compiler.get()
    }
}

dependencies {
    implementation(project(":wear_api"))
    implementation(project(":resources"))
    implementation(project(":domain:common"))
    implementation(project(":core:common"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.wear.input)
    implementation(libs.google.services)
    implementation(libs.google.gson)
    implementation(libs.google.dagger)
    implementation(libs.wear.complications)
    implementation(libs.wear.wearOngoing)
    implementation(libs.compose.activity)
    implementation(libs.compose.ui)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.compose.materialIcons)
    implementation(libs.compose.wearNavigation)
    implementation(libs.compose.wearMaterial)
    implementation(libs.compose.wearFoundation)
    implementation(libs.compose.wearToolingPreview)
    implementation(libs.compose.horologist)
    implementation(libs.compose.horologistComposables)
    implementation(libs.compose.hilt)
    debugImplementation(libs.compose.uiTooling)
    ksp(libs.kapt.dagger)
    ksp(libs.kapt.metadata)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.mockito)
    testImplementation(libs.test.coroutines)
}