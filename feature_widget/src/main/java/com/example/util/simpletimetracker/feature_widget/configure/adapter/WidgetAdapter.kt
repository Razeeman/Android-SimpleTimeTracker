package com.example.util.simpletimetracker.feature_widget.configure.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.LoaderAdapterDelegate
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData

class WidgetAdapter(
    onItemClick: ((RecordTypeViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.VIEW] = WidgetAdapterDelegate(onItemClick)
        delegates[ViewHolderType.EMPTY] = EmptyAdapterDelegate()
        delegates[ViewHolderType.LOADER] = LoaderAdapterDelegate()
    }
}