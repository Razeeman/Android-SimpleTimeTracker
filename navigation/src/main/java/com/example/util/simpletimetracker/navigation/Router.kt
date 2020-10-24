package com.example.util.simpletimetracker.navigation

import android.app.Activity
import android.content.Intent

interface Router {

    fun bind(activity: Activity)

    fun navigate(
        screen: Screen,
        data: Any? = null,
        sharedElements: Map<Any, String>? = null
    )

    fun execute(
        action: Action,
        data: Any? = null
    )

    fun show(
        notification: Notification,
        data: Any? = null,
        anchor: Any? = null // should be a view
    )

    fun back()

    fun getMainStartIntent(): Intent
}