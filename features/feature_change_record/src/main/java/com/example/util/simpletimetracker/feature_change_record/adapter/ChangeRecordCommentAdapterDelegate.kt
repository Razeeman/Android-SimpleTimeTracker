package com.example.util.simpletimetracker.feature_change_record.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordCommentItemBinding as Binding
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordCommentViewData as ViewData

fun createChangeRecordCommentAdapterDelegate(
    onItemClick: ((ViewData) -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvChangeRecordItemComment.text = item.text

        root.setOnClickWith(item, onItemClick)
    }
}