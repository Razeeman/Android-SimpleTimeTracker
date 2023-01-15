package com.example.util.simpletimetracker.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.util.simpletimetracker.feature_views.extension.setMargins
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.notification.NotificationParams
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.notification.ToastParams
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
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
            when (data.duration) {
                is SnackBarParams.Duration.Short -> SNACK_BAR_SHORT_DURATION_MS
                is SnackBarParams.Duration.Normal -> SNACK_BAR_DURATION_MS
                is SnackBarParams.Duration.Long -> SNACK_BAR_LONG_DURATION_MS
                is SnackBarParams.Duration.Indefinite -> LENGTH_INDEFINITE
            }
        )

        val textViewId = com.google.android.material.R.id.snackbar_text
        snackBar.view.findViewById<TextView>(textViewId)?.apply {
            setTextColor(Color.WHITE)
            maxLines = 5
        }

        snackBar.view.apply {
            post {
                setMargins(
                    top = data.margins.top,
                    bottom = data.margins.bottom,
                    start = data.margins.left,
                    end = data.margins.right,
                )
            }
        }

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

        snackBar.view.setOnClick {
            snackBar.dismiss()
        }

        if (data.actionText.isNotEmpty()) {
            data.actionListener?.let { listener ->
                snackBar.setAction(data.actionText) { listener(data.tag) }
            }
        }

        snackBar.show()
    }

    companion object {
        private const val SNACK_BAR_SHORT_DURATION_MS = 2000
        private const val SNACK_BAR_DURATION_MS = 5000
        private const val SNACK_BAR_LONG_DURATION_MS = 10000
    }
}