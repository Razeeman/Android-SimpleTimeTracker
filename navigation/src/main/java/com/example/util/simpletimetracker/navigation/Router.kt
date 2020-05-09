package com.example.util.simpletimetracker.navigation

import android.app.Activity

abstract class Router {

    abstract fun navigate(screen: Screen, data: Any? = null)

    abstract fun bind(activity: Activity)
}