package com.example.util.simpletimetracker.navigation

import android.app.Activity

interface NotificationResolver {

    fun show(activity: Activity?, data: NotificationParams, anchor: Any?)
}