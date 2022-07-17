package com.example.util.simpletimetracker.feature_change_record_type.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconEmojiMapper
import com.example.util.simpletimetracker.core.mapper.IconImageMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.IconEmojiCategory
import com.example.util.simpletimetracker.domain.model.IconImageCategory
import com.example.util.simpletimetracker.domain.model.IconType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.emoji.EmojiViewData
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconCategoryInfoViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconCategoryViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconSwitchViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconTypeViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconViewData
import com.example.util.simpletimetracker.navigation.params.screen.EmojiSelectionDialogParams
import javax.inject.Inject

class ChangeRecordTypeMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val iconImageMapper: IconImageMapper,
    private val iconEmojiMapper: IconEmojiMapper,
    private val colorMapper: ColorMapper,
) {

    fun toGoalTimeViewData(duration: Long): String {
        return if (duration > 0) {
            timeMapper.formatDuration(duration)
        } else {
            resourceRepo.getString(R.string.change_record_type_goal_time_disabled)
        }
    }

    fun mapIconImageData(
        newColor: AppColor,
        isDarkTheme: Boolean,
    ): List<ViewHolderType> {
        return iconImageMapper.getAvailableImages().map { (category, images) ->
            listOf(mapImageCategoryHintViewData(category)) +
                images.map { (iconName, iconResId) ->
                    mapImageViewData(iconName, iconResId, newColor, isDarkTheme)
                }
        }.flatten()
    }

    fun mapIconEmojiData(
        newColor: AppColor,
        isDarkTheme: Boolean,
    ): List<ViewHolderType> {
        return iconEmojiMapper.getAvailableEmojis().map { (category, codes) ->
            listOf(mapEmojiCategoryHintViewData(category)) +
                codes.map { code ->
                    mapEmojiViewData(code, newColor, isDarkTheme)
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
        color: AppColor,
        emojiCodes: String,
    ): EmojiSelectionDialogParams {
        return EmojiSelectionDialogParams(
            color = EmojiSelectionDialogParams.Color(
                colorId = color.colorId,
                colorInt = color.colorInt,
            ),
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
        newColor: AppColor,
        isDarkTheme: Boolean,
    ): ViewHolderType {
        return ChangeRecordTypeIconViewData(
            iconName = iconName,
            iconResId = iconResId,
            colorInt = newColor
                .let { colorMapper.mapToColorInt(it, isDarkTheme) }
        )
    }

    private fun mapEmojiViewData(
        codes: String,
        newColor: AppColor,
        isDarkTheme: Boolean,
    ): ViewHolderType {
        return EmojiViewData(
            emojiText = iconEmojiMapper.toEmojiString(codes),
            emojiCodes = codes,
            colorInt = newColor
                .let { colorMapper.mapToColorInt(it, isDarkTheme) }
        )
    }

    private fun mapImageCategoryHintViewData(
        category: IconImageCategory,
    ): ChangeRecordTypeIconCategoryInfoViewData {
        return ChangeRecordTypeIconCategoryInfoViewData(
            type = ChangeRecordTypeIconTypeViewData.Image(category.type),
            text = category.name
        )
    }

    private fun mapEmojiCategoryHintViewData(
        category: IconEmojiCategory,
    ): ChangeRecordTypeIconCategoryInfoViewData {
        return ChangeRecordTypeIconCategoryInfoViewData(
            type = ChangeRecordTypeIconTypeViewData.Emoji(category.type),
            text = category.name
        )
    }
}