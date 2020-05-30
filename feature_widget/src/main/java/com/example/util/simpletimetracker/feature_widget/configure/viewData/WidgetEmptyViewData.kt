package com.example.util.simpletimetracker.feature_widget.configure.viewData

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class WidgetEmptyViewData(
    var message: String
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.FOOTER

    override fun getUniqueId(): Long? = 1L
}