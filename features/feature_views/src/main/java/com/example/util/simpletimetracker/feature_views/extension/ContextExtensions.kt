package com.example.util.simpletimetracker.feature_views.extension

import android.content.Context
import android.util.TypedValue

fun Context.getThemedAttr(attrId: Int): Int {
    return getAttr(attrId).data
}

fun Context.getThemedAttrRes(attrId: Int): Int {
    return getAttr(attrId).resourceId
}

private fun Context.getAttr(attrId: Int): TypedValue {
    return TypedValue().apply {
        theme?.resolveAttribute(attrId, this, true)
    }
}