package com.example.util.simpletimetracker.core.view.buttonsRowView

import android.content.res.ColorStateList
import android.graphics.Color
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import kotlinx.android.synthetic.main.buttons_row_item_layout.view.btnButtonsRowView

fun createButtonsRowViewAdapterDelegate(
    selectedColor: Int,
    onItemClick: ((ButtonsRowViewData) -> Unit)
) = createRecyclerAdapterDelegate<ButtonsRowViewData>(
    R.layout.buttons_row_item_layout
) { itemView, item, _ ->

    with(itemView) {
        item as ButtonsRowViewData

        val color = if (item.isSelected) selectedColor else Color.TRANSPARENT

        btnButtonsRowView.text = item.name
        btnButtonsRowView.backgroundTintList = ColorStateList.valueOf(color)
        btnButtonsRowView.setOnClickWith(item, onItemClick)
    }
}