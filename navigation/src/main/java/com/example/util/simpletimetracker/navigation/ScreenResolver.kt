package com.example.util.simpletimetracker.navigation

import androidx.navigation.NavController
import com.example.util.simpletimetracker.navigation.params.screen.ScreenParams

interface ScreenResolver {

    fun navigate(
        navController: NavController?,
        data: ScreenParams,
        sharedElements: Map<Any, String>?,
    )

    companion object {
        var disableAnimationsForTest: Boolean = false
    }
}