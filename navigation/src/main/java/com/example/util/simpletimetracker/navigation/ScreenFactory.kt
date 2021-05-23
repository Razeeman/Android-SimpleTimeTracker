package com.example.util.simpletimetracker.navigation

import androidx.fragment.app.Fragment

interface ScreenFactory {

    fun getFragment(screen: Screen, data: Any?): Fragment?
}