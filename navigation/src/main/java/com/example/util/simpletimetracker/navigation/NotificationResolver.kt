package com.example.util.simpletimetracker.navigation

import android.app.Activity
import com.example.util.simpletimetracker.navigation.params.notification.NotificationParams

interface NotificationResolver {

    fun show(activity: Activity?, data: NotificationParams, anchor: Any?)
}