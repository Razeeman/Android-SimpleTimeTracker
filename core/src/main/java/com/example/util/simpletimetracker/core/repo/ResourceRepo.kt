package com.example.util.simpletimetracker.core.repo

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.provider.ContextProvider
import com.example.util.simpletimetracker.feature_views.extension.pxToDp
import javax.inject.Inject

class ResourceRepo @Inject constructor(
    private val contextProvider: ContextProvider,
) {

    private val context: Context get() = contextProvider.get()

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

    fun getQuantityString(@PluralsRes stringResId: Int, quantity: Int, vararg args: Any): String {
        return context.resources.getQuantityString(stringResId, quantity, *args)
    }

    fun getDimenInDp(@DimenRes dimenResId: Int): Int {
        return context.resources.getDimension(dimenResId).pxToDp()
    }

    fun getThemedAttr(attrId: Int, themeId: Int): Int {
        return TypedValue().apply {
            ContextThemeWrapper(context, themeId)
                .theme
                .resolveAttribute(attrId, this, true)
        }.data
    }

    fun getThemedAttr(attrId: Int, isDarkTheme: Boolean): Int {
        val theme = if (isDarkTheme) {
            R.style.AppThemeDark
        } else {
            R.style.AppTheme
        }
        return getThemedAttr(attrId = attrId, themeId = theme)
    }

    fun getDrawable(@DrawableRes drawableResId: Int): Drawable? {
        return ResourcesCompat.getDrawable(context.resources, drawableResId, context.theme)
    }

    fun getStringArray(@ArrayRes arrayResId: Int): List<String> {
        return context.resources.getStringArray(arrayResId).toList()
    }
}