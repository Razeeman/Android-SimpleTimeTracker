import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    id("com.android.library")
    id("kotlin-android")
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.resources"
}
