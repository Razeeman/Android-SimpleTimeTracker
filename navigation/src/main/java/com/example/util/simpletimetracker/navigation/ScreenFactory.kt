package com.example.util.simpletimetracker.navigation

import androidx.fragment.app.Fragment
import com.example.util.simpletimetracker.navigation.params.screen.ScreenParams

interface ScreenFactory {

    fun getFragment(data: ScreenParams): Fragment?
}