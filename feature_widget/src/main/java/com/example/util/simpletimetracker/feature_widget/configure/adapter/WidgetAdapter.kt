package com.example.util.simpletimetracker.feature_widget.configure.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_widget.configure.viewData.WidgetRecordTypeViewData

class WidgetAdapter(
    onItemClick: ((WidgetRecordTypeViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.VIEW] = WidgetAdapterDelegate(onItemClick)
        delegates[ViewHolderType.FOOTER] = WidgetEmptyAdapterDelegate()
    }
}