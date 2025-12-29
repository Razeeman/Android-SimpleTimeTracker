package com.example.util.simpletimetracker.feature_base_adapter.recordType

import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemRecordTypeLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData as ViewData

fun createRecordTypeAdapterDelegate(
    onItemClick: ((ViewData) -> Unit)? = null,
    onItemClickWithData: ((ViewData, Pair<Any, String>) -> Unit)? = null,
    onItemLongClick: ((ViewData, Pair<Any, String>) -> Unit)? = null,
    withTransition: Boolean = false,
    transitionNamePrefix: String = TransitionNames.RECORD_TYPE,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.viewRecordTypeItem) {
        item as ViewData
        val transitionName = transitionNamePrefix + item.id

        layoutParams = layoutParams.also { params ->
            item.width.dpToPx().let { params.width = it }
            item.height.dpToPx().let { params.height = it }
        }

        itemIsRow = item.asRow
        itemColor = item.color
        itemIcon = item.iconId
        itemIconColor = item.iconColor
        itemIconAlpha = item.iconAlpha
        itemName = item.name
        itemCheckState = item.checkState
        itemCompleteIsAnimated = true
        itemIsComplete = item.isComplete
        getCheckmarkOutline().itemIsFiltered = item.itemIsFiltered
        onItemClick?.let { setOnClickWith(item, it) }
        onItemClickWithData?.let { setOnClick { it(item, this to transitionName) } }
        onItemLongClick?.let { setOnLongClick { it(item, this to transitionName) } }
        if (withTransition) ViewCompat.setTransitionName(this, transitionName)
    }
}