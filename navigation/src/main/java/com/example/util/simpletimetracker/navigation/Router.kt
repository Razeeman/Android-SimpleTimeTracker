package com.example.util.simpletimetracker.navigation

import android.app.Activity

abstract class Router {

    abstract fun navigate(screen: Screen)

    abstract fun bind(activity: Activity)
}