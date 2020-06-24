package com.example.util.simpletimetracker.navigation

import android.app.Activity
import android.view.View
import com.example.util.simpletimetracker.core.model.SnackBarMessage

abstract class Router {

    abstract fun bind(activity: Activity)

    abstract fun navigate(
        screen: Screen,
        data: Any? = null,
        sharedElements: Map<Any, String>? = null
    )

    abstract fun back()

    abstract fun showSystemMessage(message: String)

    abstract fun showSnackBar(
        view: View,
        snackBarMessage: SnackBarMessage
    )
}