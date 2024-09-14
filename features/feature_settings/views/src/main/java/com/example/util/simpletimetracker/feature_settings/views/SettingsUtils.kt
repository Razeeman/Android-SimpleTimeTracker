package com.example.util.simpletimetracker.feature_settings.views

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.feature_base_adapter.RecyclerAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_views.extension.getThemedAttr

@ColorInt
fun SettingsTextColor.getColor(context: Context): Int {
    fun getColor(@ColorRes resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }
    return when (this) {
        is SettingsTextColor.Default -> getColor(R.color.textSecondary)
        is SettingsTextColor.Attention -> getColor(R.color.colorSecondary)
        is SettingsTextColor.Success -> context.getThemedAttr(R.attr.appPositiveColor)
    }
}

fun getSettingsAdapterDelegates(
    onBlockClicked: (SettingsBlock) -> Unit,
    onBlockClickedThrottled: (SettingsBlock) -> Unit = onBlockClicked,
    onSpinnerPositionSelected: (SettingsBlock, Int) -> Unit,
): List<RecyclerAdapterDelegate> {
    return listOf(
        createSettingsTopAdapterDelegate(),
        createSettingsBottomAdapterDelegate(),
        createSettingsTranslatorAdapterDelegate(),
        createSettingsHintAdapterDelegate(),
        createSettingsTextAdapterDelegate(onBlockClickedThrottled),
        createSettingsTextWithButtonAdapterDelegate(onBlockClicked),
        createSettingsTextWithIconAdapterDelegate(onBlockClicked),
        createSettingsCheckboxAdapterDelegate(onBlockClicked),
        createSettingsCheckboxWithButtonAdapterDelegate(onBlockClicked),
        createSettingsCheckboxWithRangeAdapterDelegate(onBlockClicked),
        createSettingsCollapseAdapterDelegate(onBlockClicked),
        createSettingsSelectorAdapterDelegate(onBlockClicked),
        createSettingsSelectorWithButtonAdapterDelegate(onBlockClicked),
        createSettingsRangeAdapterDelegate(onBlockClicked),
        createSettingsSpinnerAdapterDelegate(onSpinnerPositionSelected),
        createSettingsSpinnerEvenAdapterDelegate(onSpinnerPositionSelected),
        createSettingsSpinnerNotCheckableAdapterDelegate(onSpinnerPositionSelected),
        createSettingsSpinnerWithButtonAdapterDelegate(
            onPositionSelected = onSpinnerPositionSelected,
            onButtonClicked = onBlockClicked,
        ),
    )
}