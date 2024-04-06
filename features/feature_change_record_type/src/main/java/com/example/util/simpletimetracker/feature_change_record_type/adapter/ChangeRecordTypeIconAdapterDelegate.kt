package com.example.util.simpletimetracker.feature_change_record_type.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_change_record_type.databinding.ChangeRecordTypeItemIconLayoutBinding as Binding
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.ChangeRecordTypeIconViewData as ViewData

fun createChangeRecordTypeIconAdapterDelegate(
    onIconItemClick: ((ViewData) -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        layoutChangeRecordTypeIconItem.setCardBackgroundColor(item.colorInt)
        ivChangeRecordTypeIconItem.setBackgroundResource(item.iconResId)
        ivChangeRecordTypeIconItem.tag = item.iconResId
        layoutChangeRecordTypeIconItem.setOnClickWith(item, onIconItemClick)
    }
}