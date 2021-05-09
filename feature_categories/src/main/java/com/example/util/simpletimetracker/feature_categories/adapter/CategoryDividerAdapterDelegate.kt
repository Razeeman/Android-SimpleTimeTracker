package com.example.util.simpletimetracker.feature_categories.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.feature_categories.R
import com.example.util.simpletimetracker.feature_categories.viewData.CategoryDividerViewData

fun createCategoryDividerAdapterDelegate() = createRecyclerAdapterDelegate<CategoryDividerViewData>(
    R.layout.item_category_divider_layout
) { _, _, _ ->

    // Nothing to bind
}