package com.example.util.simpletimetracker.feature_change_record_type.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeEmojiViewData
import kotlinx.android.synthetic.main.change_record_type_item_emoji_layout.view.layoutChangeRecordTypeEmojiItem
import kotlinx.android.synthetic.main.change_record_type_item_emoji_layout.view.tvChangeRecordTypeEmojiItem

fun createChangeRecordTypeEmojiAdapterDelegate(
    onIconItemClick: ((ChangeRecordTypeEmojiViewData) -> Unit)
) = createRecyclerAdapterDelegate<ChangeRecordTypeEmojiViewData>(
    R.layout.change_record_type_item_emoji_layout
) { itemView, item, _ ->

    with(itemView) {
        item as ChangeRecordTypeEmojiViewData

        layoutChangeRecordTypeEmojiItem.setCardBackgroundColor(item.colorInt)
        tvChangeRecordTypeEmojiItem.text = item.emojiText
        layoutChangeRecordTypeEmojiItem.setOnClickWith(item, onIconItemClick)
    }
}