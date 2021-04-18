package com.example.util.simpletimetracker.feature_change_record_type.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.adapter.info.InfoViewData
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.EmojiMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.EmojiViewData
import com.example.util.simpletimetracker.domain.model.IconType
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconSwitchViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconTypeViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconViewData
import com.example.util.simpletimetracker.navigation.params.EmojiSelectionDialogParams
import javax.inject.Inject

class ChangeRecordTypeMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val iconMapper: IconMapper,
    private val emojiMapper: EmojiMapper,
    private val colorMapper: ColorMapper
) {

    fun mapToEmpty(): List<ViewHolderType> {
        return EmptyViewData(
            message = resourceRepo.getString(R.string.change_record_type_categories_empty)
        ).let(::listOf)
    }

    fun mapSelectedCategoriesHint(isEmpty: Boolean): ViewHolderType {
        return InfoViewData(
            text = if (isEmpty) {
                R.string.change_record_type_selected_categories_empty
            } else {
                R.string.change_record_type_selected_categories_hint
            }.let(resourceRepo::getString)
        )
    }

    fun toGoalTimeViewData(duration: Long): String {
        return if (duration > 0) {
            timeMapper.formatDuration(duration)
        } else {
            resourceRepo.getString(R.string.change_record_type_goal_time_disabled)
        }
    }

    fun mapIconImageData(
        newColorId: Int,
        isDarkTheme: Boolean
    ): List<ViewHolderType> {
        return iconMapper.availableIconsNames
            .map { (iconName, iconResId) ->
                ChangeRecordTypeIconViewData(
                    iconName = iconName,
                    iconResId = iconResId,
                    colorInt = newColorId
                        .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                        .let(resourceRepo::getColor)
                )
            }
    }

    fun mapIconEmojiData(
        newColorId: Int,
        isDarkTheme: Boolean
    ): List<ViewHolderType> {
        return emojiMapper.getAvailableEmojis()
            .map { category ->
                listOf(InfoViewData(category.name)) + category.emojiCodes.map { codes ->
                    mapEmojiViewData(codes, newColorId, isDarkTheme)
                }
            }.flatten()
    }

    fun mapToIconSwitchViewData(iconType: IconType): ViewHolderType {
        return listOf(
            IconType.IMAGE,
            IconType.EMOJI
        ).map {
            ChangeRecordTypeIconTypeViewData(
                iconType = it,
                name = mapToFilterTypeName(it),
                isSelected = it == iconType
            )
        }.let {
            ChangeRecordTypeIconSwitchViewData(it)
        }
    }

    fun mapEmojiSelectionParams(
        colorId: Int,
        emojiCodes: String
    ): EmojiSelectionDialogParams {
        return EmojiSelectionDialogParams(
            color = colorId,
            emojiCodes = listOf(emojiCodes) + emojiMapper.toSkinToneVariations(emojiCodes)
        )
    }

    private fun mapToFilterTypeName(iconType: IconType): String {
        return when (iconType) {
            IconType.IMAGE -> R.string.change_record_type_icon_hint
            IconType.EMOJI -> R.string.change_record_type_emoji_hint
        }.let(resourceRepo::getString)
    }

    private fun mapEmojiViewData(
        codes: String,
        newColorId: Int,
        isDarkTheme: Boolean
    ): ViewHolderType {
        return EmojiViewData(
            emojiText = emojiMapper.toEmojiString(codes),
            emojiCodes = codes,
            colorInt = newColorId
                .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                .let(resourceRepo::getColor)
        )
    }
}