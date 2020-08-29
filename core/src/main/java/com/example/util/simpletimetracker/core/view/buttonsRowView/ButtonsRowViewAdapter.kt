package com.example.util.simpletimetracker.core.view.buttonsRowView

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

class ButtonsRowViewAdapter(
    @ColorInt selectedColor: Int,
    onItemClick: ((ButtonsRowViewData) -> Unit)
) : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.VIEW] = ButtonsRowViewAdapterDelegate(selectedColor, onItemClick)
    }
}