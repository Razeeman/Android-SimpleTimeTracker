package com.example.util.simpletimetracker.navigation

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.util.simpletimetracker.navigation.params.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.ToastParams
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class NotificationResolverImpl @Inject constructor() : NotificationResolver {

    override fun show(activity: Activity?, notification: Notification, data: Any?, anchor: Any?) {
        when (notification) {
            Notification.TOAST -> showSystemMessage(activity, data)
            Notification.SNACK_BAR -> showSnackBar(activity, data, anchor)
        }
    }

    private fun showSystemMessage(activity: Activity?, data: Any?) {
        if (data !is ToastParams) return

        Toast.makeText(activity?.applicationContext, data.message, Toast.LENGTH_LONG).show()
    }

    private fun showSnackBar(activity: Activity?, data: Any?, anchor: Any?) {
        if (data !is SnackBarParams || activity == null) return

        val snackBar = Snackbar.make(
            activity.findViewById(android.R.id.content),
            data.message,
            5000
        )

        val textViewId = com.google.android.material.R.id.snackbar_text
        snackBar.view.findViewById<TextView>(textViewId)?.setTextColor(Color.WHITE)

        if (anchor is View) {
            snackBar.anchorView = anchor
        }

        snackBar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                if (event != DISMISS_EVENT_ACTION) {
                    data.dismissedListener?.invoke(data.tag)
                }
            }
        })

        if (data.actionText.isNotEmpty()) {
            data.actionListener?.let { listener ->
                snackBar.setAction(data.actionText) { listener(data.tag) }
            }
        }

        snackBar.show()
    }
}