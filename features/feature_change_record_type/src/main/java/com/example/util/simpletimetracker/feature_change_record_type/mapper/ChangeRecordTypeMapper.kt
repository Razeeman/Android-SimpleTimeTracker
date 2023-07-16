package com.example.util.simpletimetracker.feature_change_record_type.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconEmojiMapper
import com.example.util.simpletimetracker.core.mapper.IconImageMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.IconType
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.emoji.EmojiViewData
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeGoalsState
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeGoalsViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconCategoryInfoViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconCategoryViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconSwitchViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconTypeViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconViewData
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import com.example.util.simpletimetracker.navigation.params.screen.EmojiSelectionDialogParams
import javax.inject.Inject

class ChangeRecordTypeMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val iconImageMapper: IconImageMapper,
    private val iconEmojiMapper: IconEmojiMapper,
    private val colorMapper: ColorMapper,
) {

    private val goalTypeList: List<ChangeRecordTypeGoalsViewData.Type> = listOf(
        ChangeRecordTypeGoalsViewData.Type.Duration,
        ChangeRecordTypeGoalsViewData.Type.Count,
    )

    fun mapGoalsState(
        goalsState: ChangeRecordTypeGoalsState,
    ): ChangeRecordTypeGoalsViewData {
        return ChangeRecordTypeGoalsViewData(
            session = mapGoalViewData(
                title = resourceRepo.getString(R.string.change_record_type_session_goal_time),
                goal = goalsState.session,
            ),
            daily = mapGoalViewData(
                title = resourceRepo.getString(R.string.change_record_type_daily_goal_time),
                goal = goalsState.daily,
            ),
            weekly = mapGoalViewData(
                title = resourceRepo.getString(R.string.change_record_type_weekly_goal_time),
                goal = goalsState.weekly,
            ),
            monthly = mapGoalViewData(
                title = resourceRepo.getString(R.string.change_record_type_monthly_goal_time),
                goal = goalsState.monthly,
            ),
        )
    }

    fun toGoalType(position: Int): RecordTypeGoal.Type {
        return when (goalTypeList.getOrNull(position) ?: goalTypeList.first()) {
            is ChangeRecordTypeGoalsViewData.Type.Duration -> {
                RecordTypeGoal.Type.Duration(0)
            }
            is ChangeRecordTypeGoalsViewData.Type.Count -> {
                RecordTypeGoal.Type.Count(0)
            }
        }
    }

    fun getDefaultGoal(): RecordTypeGoal.Type {
        return RecordTypeGoal.Type.Duration(0)
    }

    fun getDefaultGoalState(): ChangeRecordTypeGoalsState {
        return ChangeRecordTypeGoalsState(
            session = getDefaultGoal(),
            daily = getDefaultGoal(),
            weekly = getDefaultGoal(),
            monthly = getDefaultGoal(),
        )
    }

    fun mapIconImageData(
        newColor: AppColor,
        isDarkTheme: Boolean,
    ): List<ViewHolderType> {
        val iconCategories = iconImageMapper.getAvailableImages()
        return iconCategories.toList().mapIndexed { index, (category, images) ->
            listOf(
                ChangeRecordTypeIconCategoryInfoViewData(
                    type = ChangeRecordTypeIconTypeViewData.Image(category.type, index.toLong()),
                    text = category.name,
                    isLast = index == iconCategories.size - 1,
                ),
            ) + images.map { (iconName, iconResId) ->
                mapImageViewData(iconName, iconResId, newColor, isDarkTheme)
            }
        }.flatten()
    }

    fun mapIconEmojiData(
        newColor: AppColor,
        isDarkTheme: Boolean,
    ): List<ViewHolderType> {
        val iconCategories = iconEmojiMapper.getAvailableEmojis()
        return iconCategories.toList().mapIndexed { index, (category, codes) ->
            listOf(
                ChangeRecordTypeIconCategoryInfoViewData(
                    type = ChangeRecordTypeIconTypeViewData.Emoji(category.type, index.toLong()),
                    text = category.name,
                    isLast = index == iconCategories.size - 1,
                ),
            ) + codes.map { code ->
                mapEmojiViewData(code, newColor, isDarkTheme)
            }
        }.flatten()
    }

    fun mapIconImageCategories(
        selectedIndex: Long,
    ): List<ViewHolderType> {
        return iconImageMapper.getAvailableCategories().mapIndexed { index, iconImageCategory ->
            ChangeRecordTypeIconCategoryViewData(
                type = ChangeRecordTypeIconTypeViewData.Image(iconImageCategory.type, index.toLong()),
                categoryIcon = iconImageCategory.categoryIcon,
                selected = selectedIndex == index.toLong(),
            )
        }
    }

    fun mapIconEmojiCategories(
        selectedIndex: Long,
    ): List<ViewHolderType> {
        return iconEmojiMapper.getAvailableEmojiCategories().mapIndexed { index, iconEmojiCategory ->
            ChangeRecordTypeIconCategoryViewData(
                type = ChangeRecordTypeIconTypeViewData.Emoji(iconEmojiCategory.type, index.toLong()),
                categoryIcon = iconEmojiCategory.categoryIcon,
                selected = selectedIndex == index.toLong(),
            )
        }
    }

    fun mapToIconSwitchViewData(iconType: IconType): List<ViewHolderType> {
        return listOf(
            IconType.IMAGE,
            IconType.TEXT,
            IconType.EMOJI,
        ).map {
            ChangeRecordTypeIconSwitchViewData(
                iconType = it,
                name = mapToFilterTypeName(it),
                isSelected = it == iconType,
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
            emojiCodes = listOf(emojiCodes) + iconEmojiMapper.toSkinToneVariations(emojiCodes),
        )
    }

    private fun mapGoalViewData(
        title: String,
        goal: RecordTypeGoal.Type,
    ): ChangeRecordTypeGoalsViewData.GoalViewData {
        val goalViewData = when (goal) {
            is RecordTypeGoal.Type.Duration -> ChangeRecordTypeGoalsViewData.Type.Duration
            is RecordTypeGoal.Type.Count -> ChangeRecordTypeGoalsViewData.Type.Count
        }
        val position = goalTypeList.indexOf(goalViewData)
            .takeUnless { it == -1 }.orZero()
        val value = when (goal) {
            is RecordTypeGoal.Type.Duration -> toDurationGoalText(goal.value.orZero())
            is RecordTypeGoal.Type.Count -> goal.value.orZero().toString()
        }
        val items = goalTypeList.map {
            when (it) {
                is ChangeRecordTypeGoalsViewData.Type.Duration -> {
                    resourceRepo.getString(R.string.change_record_type_goal_duration)
                }
                is ChangeRecordTypeGoalsViewData.Type.Count -> {
                    resourceRepo.getString(R.string.change_record_type_goal_count)
                }
            }
        }.map(CustomSpinner::CustomSpinnerTextItem)

        return ChangeRecordTypeGoalsViewData.GoalViewData(
            title = title,
            typeItems = items,
            typeSelectedPosition = position,
            type = goalViewData,
            value = value,
        )
    }

    private fun toDurationGoalText(duration: Long): String {
        return if (duration > 0) {
            timeMapper.formatDuration(duration)
        } else {
            resourceRepo.getString(R.string.change_record_type_goal_time_disabled)
        }
    }

    private fun mapToFilterTypeName(iconType: IconType): String {
        return when (iconType) {
            IconType.IMAGE -> R.string.change_record_type_icon_image_hint
            IconType.TEXT -> R.string.change_record_type_icon_text_hint
            IconType.EMOJI -> R.string.change_record_type_icon_emoji_hint
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
                .let { colorMapper.mapToColorInt(it, isDarkTheme) },
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
                .let { colorMapper.mapToColorInt(it, isDarkTheme) },
        )
    }
}