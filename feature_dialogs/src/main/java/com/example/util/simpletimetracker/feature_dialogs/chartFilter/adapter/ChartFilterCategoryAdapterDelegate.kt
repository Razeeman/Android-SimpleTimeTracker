package com.example.util.simpletimetracker.feature_dialogs.chartFilter.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.core.viewData.CategoryViewData
import com.example.util.simpletimetracker.feature_dialogs.R
import kotlinx.android.synthetic.main.chart_filter_item_category_layout.view.viewChartFilterCategoryItem

fun createChartFilterCategoryAdapterDelegate(
    onItemClick: ((CategoryViewData) -> Unit)
) = createRecyclerAdapterDelegate<CategoryViewData>(
    R.layout.chart_filter_item_category_layout
) { itemView, item, _ ->

    with(itemView.viewChartFilterCategoryItem) {
        item as CategoryViewData

        itemColor = item.color
        itemName = item.name
        itemTextColor = item.textColor
        setOnClickWith(item, onItemClick)
    }
}