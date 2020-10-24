package com.example.util.simpletimetracker.navigation

import androidx.navigation.NavController

interface ScreenResolver {

    fun navigate(navController: NavController?, screen: Screen, data: Any?, sharedElements: Map<Any, String>?)
}