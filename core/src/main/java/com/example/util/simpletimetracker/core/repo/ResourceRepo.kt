package com.example.util.simpletimetracker.core.repo

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.feature_views.extension.pxToDp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ResourceRepo @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getColor(@ColorRes colorResId: Int): Int {
        return ContextCompat.getColor(context, colorResId)
    }

    fun getString(@StringRes stringResId: Int): String {
        return context.getString(stringResId)
    }

    fun getString(@StringRes stringResId: Int, vararg args: Any): String {
        return context.getString(stringResId, *args)
    }

    fun getQuantityString(@PluralsRes stringResId: Int, quantity: Int): String {
        return context.resources.getQuantityString(stringResId, quantity)
    }

    fun getDimenInDp(@DimenRes dimenResId: Int): Int {
        return context.resources.getDimension(dimenResId).pxToDp()
    }
}