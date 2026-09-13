package com.example.util.simpletimetracker.feature_base_adapter.button

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setMargins
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_base_adapter.button.ButtonViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemButtonLayoutBinding as Binding

// TODO remove ripple from icon background if background is transparent.
// TODO SUG add backup tests
// TODO GOAL add backup tests, raise test file version
fun createButtonAdapterDelegate(
    onClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        root.tag = item.id
        root.setMargins(
            start = item.marginHorizontalDp,
            end = item.marginHorizontalDp,
        )
        itemButton.buttonIconText = item.text
        when (item.icon) {
            is ViewData.Icon.Hidden -> {
                itemButton.buttonIconVisible = false
            }
            is ViewData.Icon.Present -> {
                itemButton.buttonIconVisible = true
                itemButton.buttonIconRes = item.icon.icon
                itemButton.buttonIconBackgroundColor = item.icon.iconBackgroundColor
            }
        }
        itemButton.isEnabled = item.isEnabled
        itemButton.setOnClickWith(item, onClick)
    }
}

data class ButtonViewData(
    val id: Id,
    val text: String,
    val icon: Icon,
    @ColorInt val backgroundColor: Int,
    val isEnabled: Boolean,
    val marginHorizontalDp: Int,
) : ViewHolderType {

    override fun getUniqueId(): Long = id.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ViewData

    interface Id

    sealed interface Icon {
        data object Hidden : Icon
        data class Present(
            @DrawableRes val icon: Int,
            @ColorInt val iconColor: Int,
            @ColorInt val iconBackgroundColor: Int,
        ) : Icon
    }
}