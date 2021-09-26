package com.example.util.simpletimetracker.navigation.bundleCreator

import android.os.Bundle
import com.example.util.simpletimetracker.navigation.params.screen.ScreenParams

inline fun <T : ScreenParams> bundleCreatorDelegate(
    crossinline bundleCreator: ((T) -> Bundle?),
) = object : BundleCreator() {

    @Suppress("UNCHECKED_CAST")
    override fun createBundle(data: Any): Bundle? {
        return bundleCreator(data as T)
    }
}