package com.example.util.simpletimetracker.feature_base_adapter.recordComment

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ChangeRecordCommentItemBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.recordComment.RecordCommentViewData as ViewData

fun createRecordCommentAdapterDelegate(
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

sealed class RecordCommentViewData : ViewHolderType {

    abstract val text: String

    override fun getUniqueId(): Long = text.hashCode().toLong()

    data class Similar(
        override val text: String,
    ) : ViewData() {

        override fun isValidType(other: ViewHolderType): Boolean = other is Similar
    }

    data class Last(
        override val text: String,
    ) : ViewData() {

        override fun isValidType(other: ViewHolderType): Boolean = other is Last
    }

    data class Favourite(
        override val text: String,
    ) : ViewData() {

        override fun isValidType(other: ViewHolderType): Boolean = other is Favourite
    }
}