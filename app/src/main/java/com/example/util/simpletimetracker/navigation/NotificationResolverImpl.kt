package com.example.util.simpletimetracker.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import com.example.util.simpletimetracker.core.databinding.ViewPopupLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.setMargins
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.notification.NotificationParams
import com.example.util.simpletimetracker.navigation.params.notification.PopupParams
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.notification.ToastParams
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.Integer.max
import javax.inject.Inject

class NotificationResolverImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationResolver {

    override fun show(
        activity: Activity?,
        dialog: Dialog?,
        data: NotificationParams,
        anchor: Any?,
    ) {
        when (data) {
            is ToastParams -> showSystemMessage(data)
            is SnackBarParams -> showSnackBar(activity, dialog, data, anchor)
            is PopupParams -> showPopup(activity, data)
        }
    }

    private fun showSystemMessage(data: ToastParams) {
        Toast.makeText(context, data.message, Toast.LENGTH_LONG).show()
    }

    @SuppressLint("WrongConstant")
    private fun showSnackBar(
        activity: Activity?,
        dialog: Dialog?,
        data: SnackBarParams,
        anchor: Any?,
    ) {
        val view = if (data.inDialog) {
            dialog?.window?.decorView
        } else {
            activity?.findViewById(android.R.id.content)
        } ?: return

        val snackBar = Snackbar.make(
            view,
            data.message,
            when (data.duration) {
                is SnackBarParams.Duration.ExtraShort -> SNACK_BAR_EXTRA_SHORT_DURATION_MS
                is SnackBarParams.Duration.Short -> SNACK_BAR_SHORT_DURATION_MS
                is SnackBarParams.Duration.Normal -> SNACK_BAR_DURATION_MS
                is SnackBarParams.Duration.Long -> SNACK_BAR_LONG_DURATION_MS
                is SnackBarParams.Duration.Indefinite -> LENGTH_INDEFINITE
            },
        )

        val textViewId = com.google.android.material.R.id.snackbar_text
        snackBar.view.findViewById<TextView>(textViewId)?.apply {
            setTextColor(Color.WHITE)
            maxLines = 5
        }

        if (data.marginBottomDp != null) {
            snackBar.view.apply { post { setMargins(bottom = data.marginBottomDp) } }
        }

        if (anchor is View) {
            snackBar.anchorView = anchor
        }

        snackBar.addCallback(
            object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event != DISMISS_EVENT_ACTION) {
                        data.dismissedListener?.invoke(data.tag)
                    }
                }
            },
        )

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

    @SuppressLint("ClickableViewAccessibility")
    private fun showPopup(activity: Activity?, data: PopupParams) {
        if (activity == null) return

        val parent = activity.window.decorView

        val view = ViewPopupLayoutBinding.inflate(activity.layoutInflater)
        view.tvPopupText.text = data.message
        val popupView = PopupWindow(
            view.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        popupView.apply {
            isFocusable = false
            isTouchable = true
            isOutsideTouchable = true
        }
        view.root.setOnTouchListener { _, _ ->
            popupView.dismiss()
            true
        }

        view.root.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
        )
        val viewWidth = view.root.measuredWidth
        val viewHeight = view.root.measuredHeight
        val availableSpace = Rect()
        parent.getWindowVisibleDisplayFrame(availableSpace)

        val canShowAtBottom = viewHeight <= availableSpace.bottom - data.anchorCoordinates.bottom
        val offsetX = data.anchorCoordinates.right - data.anchorCoordinates.width / 2 - viewWidth / 2
        val offsetY = if (canShowAtBottom) {
            data.anchorCoordinates.bottom
        } else {
            data.anchorCoordinates.top - viewHeight
        }

        popupView.showAsDropDown(
            parent,
            offsetX.coerceIn(
                availableSpace.left,
                max(availableSpace.left, availableSpace.right - viewWidth),
            ),
            offsetY.coerceIn(
                availableSpace.top,
                max(availableSpace.top, availableSpace.bottom - viewHeight),
            ),
        )
    }

    companion object {
        private const val SNACK_BAR_EXTRA_SHORT_DURATION_MS = 1000
        private const val SNACK_BAR_SHORT_DURATION_MS = 2000
        private const val SNACK_BAR_DURATION_MS = 5000
        private const val SNACK_BAR_LONG_DURATION_MS = 10000
    }
}