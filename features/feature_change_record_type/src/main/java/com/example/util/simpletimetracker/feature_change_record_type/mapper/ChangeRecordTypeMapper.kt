package com.example.util.simpletimetracker.feature_change_record_type.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.adapter.info.InfoViewData
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconEmojiMapper
import com.example.util.simpletimetracker.core.mapper.IconImageMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.EmojiViewData
import com.example.util.simpletimetracker.domain.model.IconEmojiCategory
import com.example.util.simpletimetracker.domain.model.IconImageCategory
import com.example.util.simpletimetracker.domain.model.IconType
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconCategoryInfoViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconCategoryViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconSwitchViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconTypeViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconViewData
import com.example.util.simpletimetracker.navigation.params.EmojiSelectionDialogParams
import javax.inject.Inject

class ChangeRecordTypeMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val iconImageMapper: IconImageMapper,
    private val iconEmojiMapper: IconEmojiMapper,
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
        return iconImageMapper.getAvailableImages().map { (category, images) ->
            listOf(mapImageCategoryHintViewData(category)) +
                images.map { (iconName, iconResId) ->
                    mapImageViewData(iconName, iconResId, newColorId, isDarkTheme)
                }
        }.flatten()
    }

    fun mapIconEmojiData(
        newColorId: Int,
        isDarkTheme: Boolean
    ): List<ViewHolderType> {
        return iconEmojiMapper.getAvailableEmojis().map { (category, codes) ->
            listOf(mapEmojiCategoryHintViewData(category)) +
                codes.map { code ->
                    mapEmojiViewData(code, newColorId, isDarkTheme)
                }
        }.flatten()
    }

    fun mapIconImageCategories(): List<ViewHolderType> {
        return iconImageMapper.getAvailableCategories().map {
            ChangeRecordTypeIconCategoryViewData(
                type = ChangeRecordTypeIconTypeViewData.Image(it.type),
                categoryIcon = it.categoryIcon
            )
        }
    }

    fun mapIconEmojiCategories(): List<ViewHolderType> {
        return iconEmojiMapper.getAvailableEmojiCategories().map {
            ChangeRecordTypeIconCategoryViewData(
                type = ChangeRecordTypeIconTypeViewData.Emoji(it.type),
                categoryIcon = it.categoryIcon
            )
        }
    }

    fun mapToIconSwitchViewData(iconType: IconType): List<ViewHolderType> {
        return listOf(
            IconType.IMAGE,
            IconType.EMOJI
        ).map {
            ChangeRecordTypeIconSwitchViewData(
                iconType = it,
                name = mapToFilterTypeName(it),
                isSelected = it == iconType
            )
        }
    }

    fun mapEmojiSelectionParams(
        colorId: Int,
        emojiCodes: String
    ): EmojiSelectionDialogParams {
        return EmojiSelectionDialogParams(
            color = colorId,
            emojiCodes = listOf(emojiCodes) + iconEmojiMapper.toSkinToneVariations(emojiCodes)
        )
    }

    private fun mapToFilterTypeName(iconType: IconType): String {
        return when (iconType) {
            IconType.IMAGE -> R.string.change_record_type_icon_hint
            IconType.EMOJI -> R.string.change_record_type_emoji_hint
        }.let(resourceRepo::getString)
    }

    private fun mapImageViewData(
        iconName: String,
        iconResId: Int,
        newColorId: Int,
        isDarkTheme: Boolean
    ): ViewHolderType {
        return ChangeRecordTypeIconViewData(
            iconName = iconName,
            iconResId = iconResId,
            colorInt = newColorId
                .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                .let(resourceRepo::getColor)
        )
    }

    private fun mapEmojiViewData(
        codes: String,
        newColorId: Int,
        isDarkTheme: Boolean
    ): ViewHolderType {
        return EmojiViewData(
            emojiText = iconEmojiMapper.toEmojiString(codes),
            emojiCodes = codes,
            colorInt = newColorId
                .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                .let(resourceRepo::getColor)
        )
    }

    private fun mapImageCategoryHintViewData(
        category: IconImageCategory
    ): ChangeRecordTypeIconCategoryInfoViewData {
        return ChangeRecordTypeIconCategoryInfoViewData(
            type = ChangeRecordTypeIconTypeViewData.Image(category.type),
            text = category.name
        )
    }

    private fun mapEmojiCategoryHintViewData(
        category: IconEmojiCategory
    ): ChangeRecordTypeIconCategoryInfoViewData {
        return ChangeRecordTypeIconCategoryInfoViewData(
            type = ChangeRecordTypeIconTypeViewData.Emoji(category.type),
            text = category.name
        )
    }
}