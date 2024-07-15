package com.example.util.simpletimetracker

object Base {
    const val applicationId = "com.razeeman.util.simpletimetracker"
    const val namespace = "com.example.util.simpletimetracker"

    // Raise by 2 to account for wear version code.
    const val versionCode = 55
    const val versionName = "1.43"
    const val minSDK = 21
    const val currentSDK = 34

    const val versionCodeWear = versionCode + 1
    const val versionNameWear = versionName + "w"
    const val minSDKWear = 26
    const val currentSDKWear = currentSDK
    const val targetSDKWear = 33
}
