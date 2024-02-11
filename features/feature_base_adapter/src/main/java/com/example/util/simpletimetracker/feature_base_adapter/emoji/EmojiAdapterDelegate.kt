package com.example.util.simpletimetracker.feature_base_adapter.emoji

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemEmojiLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.emoji.EmojiViewData as ViewData

fun createEmojiAdapterDelegate(
    onIconItemClick: ((ViewData) -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        layoutEmojiItem.setCardBackgroundColor(item.colorInt)
        tvEmojiItem.text = item.emojiText
        layoutEmojiItem.setOnClickWith(item, onIconItemClick)
    }
}