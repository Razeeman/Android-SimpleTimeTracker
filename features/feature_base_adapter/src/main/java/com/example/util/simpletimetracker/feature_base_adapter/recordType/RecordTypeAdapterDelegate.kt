package com.example.util.simpletimetracker.feature_base_adapter.recordType

import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemRecordTypeLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData as ViewData

fun createRecordTypeAdapterDelegate(
    onItemClick: ((ViewData) -> Unit)? = null,
    onItemLongClick: ((ViewData, Map<Any, String>) -> Unit)? = null,
    withTransition: Boolean = false,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.viewRecordTypeItem) {
        item as ViewData
        val transitionName = TransitionNames.RECORD_TYPE + item.id

        layoutParams = layoutParams.also { params ->
            item.width?.dpToPx()?.let { params.width = it }
            item.height?.dpToPx()?.let { params.height = it }
        }

        itemIsRow = item.asRow
        itemColor = item.color
        itemIcon = item.iconId
        itemIconColor = item.iconColor
        itemIconAlpha = item.iconAlpha
        itemName = item.name
        itemWithCheck = item.isChecked != null
        itemIsChecked = item.isChecked.orFalse()
        itemCompleteIsAnimated = true
        itemIsComplete = item.isComplete
        getCheckmarkOutline().itemIsFiltered = item.itemIsFiltered
        onItemClick?.let { setOnClickWith(item, it) }
        onItemLongClick?.let { setOnLongClick { it(item, mapOf(this to transitionName)) } }
        if (withTransition) ViewCompat.setTransitionName(this, transitionName)
    }
}