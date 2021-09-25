package com.example.util.simpletimetracker.navigation

import android.app.Activity
import androidx.activity.ComponentActivity
import com.example.util.simpletimetracker.navigation.params.action.ActionParams

interface ActionResolver {

    fun registerResultListeners(activity: ComponentActivity)

    fun execute(activity: Activity?, data: ActionParams)
}