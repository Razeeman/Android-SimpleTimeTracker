package com.example.util.simpletimetracker.feature_change_record_type.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyAdapterDelegate
import com.example.util.simpletimetracker.core.viewData.CategoryViewData

class ChangeRecordTypeCategoriesAdapter(
    onItemClick: ((CategoryViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.CATEGORY] = ChangeRecordTypeCategoryAdapterDelegate(onItemClick)
        delegates[ViewHolderType.EMPTY] = EmptyAdapterDelegate()
    }
}