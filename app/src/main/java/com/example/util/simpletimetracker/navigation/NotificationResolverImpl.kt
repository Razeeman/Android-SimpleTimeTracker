package com.example.util.simpletimetracker.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.util.simpletimetracker.navigation.params.notification.NotificationParams
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.notification.ToastParams
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class NotificationResolverImpl @Inject constructor() : NotificationResolver {

    override fun show(activity: Activity?, data: NotificationParams, anchor: Any?) {
        when (data) {
            is ToastParams -> showSystemMessage(activity, data)
            is SnackBarParams -> showSnackBar(activity, data, anchor)
        }
    }

    private fun showSystemMessage(activity: Activity?, data: ToastParams) {
        Toast.makeText(activity?.applicationContext, data.message, Toast.LENGTH_LONG).show()
    }

    @SuppressLint("WrongConstant")
    private fun showSnackBar(activity: Activity?, data: SnackBarParams, anchor: Any?) {
        if (activity == null) return

        val snackBar = Snackbar.make(
            activity.findViewById(android.R.id.content),
            data.message,
            SNACK_BAR_DURATION_MS
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

    companion object {
        private const val SNACK_BAR_DURATION_MS = 5000
    }
}