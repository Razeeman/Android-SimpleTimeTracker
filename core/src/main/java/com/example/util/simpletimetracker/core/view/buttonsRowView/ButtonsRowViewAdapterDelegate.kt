package com.example.util.simpletimetracker.core.view.buttonsRowView

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import kotlinx.android.synthetic.main.buttons_row_item_layout.view.btnButtonsRowView

class ButtonsRowViewAdapterDelegate(
    @ColorInt private val selectedColor: Int,
    private val onItemClick: ((ButtonsRowViewData) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        ButtonsRowViewHolder(parent)

    inner class ButtonsRowViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.buttons_row_item_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView) {
            item as ButtonsRowViewData

            val color = if (item.isSelected) selectedColor else Color.TRANSPARENT

            btnButtonsRowView.text = item.name
            btnButtonsRowView.backgroundTintList = ColorStateList.valueOf(color)
            btnButtonsRowView.setOnClickWith(item, onItemClick)
        }
    }
}