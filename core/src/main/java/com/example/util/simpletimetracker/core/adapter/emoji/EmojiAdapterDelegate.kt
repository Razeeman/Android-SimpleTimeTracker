package com.example.util.simpletimetracker.core.adapter.emoji

import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.core.databinding.ItemEmojiLayoutBinding as Binding
import com.example.util.simpletimetracker.core.viewData.EmojiViewData as ViewData

fun createEmojiAdapterDelegate(
    onIconItemClick: ((ViewData) -> Unit)
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        layoutEmojiItem.setCardBackgroundColor(item.colorInt)
        tvEmojiItem.text = item.emojiText
        layoutEmojiItem.setOnClickWith(item, onIconItemClick)
    }
}