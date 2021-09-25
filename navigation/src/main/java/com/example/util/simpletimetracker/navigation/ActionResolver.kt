package com.example.util.simpletimetracker.navigation

import android.app.Activity
import androidx.activity.ComponentActivity

interface ActionResolver {

    fun registerResultListeners(activity: ComponentActivity)

    fun execute(activity: Activity?, action: Action, data: Any?)
}