package com.example.util.simpletimetracker.navigation.params

import android.os.Bundle

data class NavigationData(
    val navId: Int,
    val bundleProvider: ((Any?) -> Bundle)?,
)