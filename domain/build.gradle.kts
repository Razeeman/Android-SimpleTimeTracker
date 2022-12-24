import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.Deps

plugins {
    id("com.android.library") // TODO java library
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    compileSdkVersion(Base.currentSDK)

    defaultConfig {
        minSdkVersion(Base.minSDK)
        targetSdkVersion(Base.currentSDK)
    }
}

dependencies {
    api(Deps.javax)
    api(Deps.coroutines)
    api(Deps.timber)
    api(Deps.kotlin)

    testImplementation(Deps.Test.junit)
}
