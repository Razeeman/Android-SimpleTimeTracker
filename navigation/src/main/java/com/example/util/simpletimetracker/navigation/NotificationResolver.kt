package com.example.util.simpletimetracker.navigation

import android.app.Activity
import android.app.Dialog
import com.example.util.simpletimetracker.navigation.params.notification.NotificationParams

interface NotificationResolver {

    fun show(
        activity: Activity?,
        dialog: Dialog?,
        data: NotificationParams,
        anchor: Any?,
    )
}