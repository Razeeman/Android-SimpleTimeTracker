package com.example.util.simpletimetracker.core.view.buttonsRowView

import android.graphics.Color
import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.core.databinding.ButtonsRowItemLayoutBinding as Binding
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData as ViewData

fun createButtonsRowViewAdapterDelegate(
    selectedColor: Int,
    onItemClick: ((ViewData) -> Unit)
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        val color = if (item.isSelected) selectedColor else Color.TRANSPARENT

        btnButtonsRowView.text = item.name
        btnButtonsRowView.setBackgroundColor(color)
        btnButtonsRowView.setOnClickWith(item, onItemClick)
    }
}