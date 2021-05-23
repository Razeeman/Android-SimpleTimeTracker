package com.example.util.simpletimetracker.core.utils

import androidx.cardview.widget.CardView
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.extension.getThemedAttr

fun setFlipChooserColor(view: CardView, opened: Boolean) {
    val colorAttr = if (opened) {
        R.attr.appInputFieldBorderColor
    } else {
        R.attr.appBackgroundColor
    }
    view.context.getThemedAttr(colorAttr).let(view::setCardBackgroundColor)
}