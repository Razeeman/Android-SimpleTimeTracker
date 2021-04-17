package com.example.util.simpletimetracker.core.adapter.emoji

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.core.viewData.EmojiViewData
import kotlinx.android.synthetic.main.item_emoji_layout.view.layoutEmojiItem
import kotlinx.android.synthetic.main.item_emoji_layout.view.tvEmojiItem

fun createEmojiAdapterDelegate(
    onIconItemClick: ((EmojiViewData) -> Unit)
) = createRecyclerAdapterDelegate<EmojiViewData>(
    R.layout.item_emoji_layout
) { itemView, item, _ ->

    with(itemView) {
        item as EmojiViewData

        layoutEmojiItem.setCardBackgroundColor(item.colorInt)
        tvEmojiItem.text = item.emojiText
        layoutEmojiItem.setOnClickWith(item, onIconItemClick)
    }
}