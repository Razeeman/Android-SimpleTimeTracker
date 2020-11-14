package com.example.util.simpletimetracker.feature_categories.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.LoaderAdapterDelegate
import com.example.util.simpletimetracker.core.viewData.CategoryViewData

class CategoriesAdapter(
    onLongClick: ((CategoryViewData, Map<Any, String>) -> Unit),
    onAddClick: (() -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.CATEGORY] = CategoryAdapterDelegate(onLongClick)
        delegates[ViewHolderType.LOADER] = LoaderAdapterDelegate()
        delegates[ViewHolderType.EMPTY] = EmptyAdapterDelegate()
        delegates[ViewHolderType.FOOTER] = CategoryAddAdapterDelegate(onAddClick)
    }
}