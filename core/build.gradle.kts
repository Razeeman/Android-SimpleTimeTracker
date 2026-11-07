import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    alias(libs.plugins.gradleLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinParcelize)
    alias(libs.plugins.ksp)
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.core"

    testOptions {
        unitTests.all {
            // Allow Robolectric to access JDK internals through reflection on Java 17+.
            it.jvmArgs(
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.util=ALL-UNNAMED",
                "--add-opens=java.base/java.io=ALL-UNNAMED",
                "--add-opens=java.base/java.net=ALL-UNNAMED",
                "--add-opens=java.base/java.security=ALL-UNNAMED",
                "--add-opens=java.base/java.text=ALL-UNNAMED",
                "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
                "--add-opens=java.desktop/java.awt.font=ALL-UNNAMED",
                "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
            )
        }
    }
}

// TODO remove api
dependencies {
    api(project(":domain"))
    api(project(":navigation"))
    api(project(":resources"))
    api(project(":feature_base_adapter"))
    api(project(":feature_views"))
    api(project(":core:common"))
    implementation(project(":wear_api"))

    api(libs.androidx.appcompat)
    api(libs.androidx.recyclerView)
    api(libs.androidx.constraintLayout)
    api(libs.androidx.cardView)
    api(libs.androidx.material)
    api(libs.androidx.viewpager2)
    api(libs.emoji.emojiBundled)
    api(libs.google.flexBox)
    api(libs.google.dagger)
    api(libs.ktx.core)
    api(libs.ktx.fragment)
    api(libs.ktx.liveDataCore)
    api(libs.ktx.liveData)
    api(libs.ktx.viewModel)
    api(libs.ktx.activity)
    api(libs.uitest.espressoIdling)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.mockito)
    testImplementation(libs.test.robolectric)
}
