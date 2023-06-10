package com.example.util.simpletimetracker.core.utils

import androidx.cardview.widget.CardView
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.feature_base_adapter.extensions.getThemedAttr

/**
 * Sets card background depending if it was clicked before (eg. opening a choose by clicking on card).
 */
fun CardView.setChooserColor(opened: Boolean) {
    val colorAttr = if (opened) {
        R.attr.appInputFieldBorderColor
    } else {
        R.attr.appBackgroundColor
    }
    context.getThemedAttr(colorAttr).let(::setCardBackgroundColor)
}