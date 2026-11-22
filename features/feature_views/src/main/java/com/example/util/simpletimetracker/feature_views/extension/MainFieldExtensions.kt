package com.example.util.simpletimetracker.feature_views.extension

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.example.util.simpletimetracker.feature_views.R

fun ConstraintLayout.updateMainFieldLayout(
    label: View,
    preview: View,
    arrow: View,
    opened: Boolean,
) {
    val container = this
    ConstraintSet().apply {
        clone(container)
        val previewSize = resources.getDimensionPixelSize(
            if (opened) R.dimen.input_field_preview_size_compact else R.dimen.input_field_preview_size,
        )
        constrainWidth(preview.id, previewSize)
        constrainHeight(preview.id, previewSize)
        clear(preview.id, ConstraintSet.START)
        clear(preview.id, ConstraintSet.END)
        clear(label.id, ConstraintSet.BOTTOM)
        if (opened) {
            connect(preview.id, ConstraintSet.END, arrow.id, ConstraintSet.START, 8.dpToPx())
            connect(preview.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
            connect(preview.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
            connect(label.id, ConstraintSet.END, preview.id, ConstraintSet.START, 8.dpToPx())
            connect(label.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
            connect(label.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
        } else {
            clear(preview.id, ConstraintSet.TOP)
            connect(preview.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 12.dpToPx())
            connect(preview.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 12.dpToPx())
            connect(label.id, ConstraintSet.END, arrow.id, ConstraintSet.START, 12.dpToPx())
            connect(label.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 12.dpToPx())
        }
        applyTo(container)
    }
}
