package com.example.util.simpletimetracker.navigation

import android.app.Activity

interface NotificationResolver {

    fun show(activity: Activity?, notification: Notification, data: Any?, anchor: Any?)
}