package com.example.util.simpletimetracker.feature_dialogs.chartFilter.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.recordType.RecordTypeAdapterDelegate
import com.example.util.simpletimetracker.core.viewData.CategoryViewData
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData

class ChartFilterAdapter(
    onRecordTypeClick: ((RecordTypeViewData) -> Unit),
    onCategoryClick: ((CategoryViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.RECORD_TYPE] = RecordTypeAdapterDelegate(onRecordTypeClick)
        delegates[ViewHolderType.CATEGORY] = ChartFilterCategoryAdapterDelegate(onCategoryClick)
        delegates[ViewHolderType.LOADER] = LoaderAdapterDelegate()
    }
}