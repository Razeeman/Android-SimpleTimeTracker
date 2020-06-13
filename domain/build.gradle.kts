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
}

dependencies {
    api(deps.javax)
    api(deps.coroutines)
    api(deps.timber)

    testImplementation(deps.test.junit)
}
