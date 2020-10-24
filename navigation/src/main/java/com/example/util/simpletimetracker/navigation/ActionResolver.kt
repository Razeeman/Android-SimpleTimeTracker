package com.example.util.simpletimetracker.navigation

import android.app.Activity

interface ActionResolver {

    fun execute(activity: Activity?, action: Action, data: Any?)
}