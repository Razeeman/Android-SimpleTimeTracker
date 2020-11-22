package com.example.util.simpletimetracker.feature_change_category.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.color.ColorAdapterDelegate
import com.example.util.simpletimetracker.core.viewData.ColorViewData

class ChangeCategoryColorAdapter(
    onColorItemClick: ((ColorViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.VIEW] = ColorAdapterDelegate(onColorItemClick)
    }
}