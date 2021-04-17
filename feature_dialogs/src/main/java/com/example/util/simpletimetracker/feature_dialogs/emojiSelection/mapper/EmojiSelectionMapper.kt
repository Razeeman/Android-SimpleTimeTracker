package com.example.util.simpletimetracker.feature_dialogs.emojiSelection.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.EmojiMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.EmojiViewData
import javax.inject.Inject

class EmojiSelectionMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val colorMapper: ColorMapper,
    private val emojiMapper: EmojiMapper
) {

    fun mapIconEmojiData(
        colorId: Int,
        isDarkTheme: Boolean,
        emojiCodes: List<List<Int>>
    ): List<ViewHolderType> {
        return emojiCodes.map { codes ->
            EmojiViewData(
                emojiText = emojiMapper.toEmojiString(codes),
                emojiCodes = codes,
                colorInt = colorId
                    .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                    .let(resourceRepo::getColor)
            )
        }
    }
}