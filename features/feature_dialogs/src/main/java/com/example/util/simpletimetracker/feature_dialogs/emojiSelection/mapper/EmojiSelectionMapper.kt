package com.example.util.simpletimetracker.feature_dialogs.emojiSelection.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconEmojiMapper
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.emoji.EmojiViewData
import javax.inject.Inject

class EmojiSelectionMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val iconEmojiMapper: IconEmojiMapper,
) {

    fun mapIconEmojiData(
        color: AppColor,
        isDarkTheme: Boolean,
        emojiCodes: List<String>,
    ): List<ViewHolderType> {
        return emojiCodes.map { codes ->
            EmojiViewData(
                emojiText = iconEmojiMapper.toEmojiString(codes),
                emojiCodes = codes,
                colorInt = colorMapper.mapToColorInt(color, isDarkTheme)
            )
        }
    }
}