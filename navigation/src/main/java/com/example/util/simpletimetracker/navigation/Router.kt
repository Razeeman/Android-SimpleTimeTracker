package com.example.util.simpletimetracker.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity

interface Router {

    fun onCreate(activity: ComponentActivity)

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
        data: NotificationParams,
        anchor: Any? = null // should be a view
    )

    fun setResultListener(key: String, listener: ResultListener)

    fun sendResult(key: String, data: Any?)

    fun back()

    fun getMainStartIntent(): Intent
}