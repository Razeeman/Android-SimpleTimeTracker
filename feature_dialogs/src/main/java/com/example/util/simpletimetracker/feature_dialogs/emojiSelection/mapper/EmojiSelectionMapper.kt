package com.example.util.simpletimetracker.feature_dialogs.emojiSelection.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconEmojiMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.EmojiViewData
import javax.inject.Inject

class EmojiSelectionMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val colorMapper: ColorMapper,
    private val iconEmojiMapper: IconEmojiMapper
) {

    fun mapIconEmojiData(
        colorId: Int,
        isDarkTheme: Boolean,
        emojiCodes: List<String>
    ): List<ViewHolderType> {
        return emojiCodes.map { codes ->
            EmojiViewData(
                emojiText = iconEmojiMapper.toEmojiString(codes),
                emojiCodes = codes,
                colorInt = colorId
                    .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                    .let(resourceRepo::getColor)
            )
        }
    }
}