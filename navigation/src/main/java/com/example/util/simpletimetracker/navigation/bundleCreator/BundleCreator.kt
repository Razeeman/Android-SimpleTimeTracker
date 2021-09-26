package com.example.util.simpletimetracker.navigation.bundleCreator

import android.os.Bundle

abstract class BundleCreator {

    abstract fun createBundle(data: Any): Bundle?

    companion object {

        fun empty() = object : BundleCreator() {
            override fun createBundle(data: Any): Bundle? = null
        }
    }
}