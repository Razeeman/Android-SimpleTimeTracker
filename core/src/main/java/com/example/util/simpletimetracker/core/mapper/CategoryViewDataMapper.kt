package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.MULTITASK_ITEM_ID
import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryAddViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.TagType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.HintBigViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import java.util.SortedMap
import javax.inject.Inject

class CategoryViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val iconMapper: IconMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTagViewDataMapper: RecordTagViewDataMapper,
) {

    fun mapCategory(
        category: Category,
        isDarkTheme: Boolean,
        isFiltered: Boolean = false,
    ): CategoryViewData.Category {
        return CategoryViewData.Category(
            id = category.id,
            name = category.name,
            iconColor = getTextColor(isDarkTheme, isFiltered),
            color = getColor(category.color, isDarkTheme, isFiltered),
        )
    }

    fun mapToUncategorizedItem(
        isFiltered: Boolean,
        isDarkTheme: Boolean,
    ): CategoryViewData {
        return CategoryViewData.Category(
            id = UNCATEGORIZED_ITEM_ID,
            name = R.string.uncategorized_time_name
                .let(resourceRepo::getString),
            iconColor = getTextColor(
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            ),
            color = if (isFiltered) {
                colorMapper.toFilteredColor(isDarkTheme)
            } else {
                colorMapper.toUntrackedColor(isDarkTheme)
            },
        )
    }

    fun mapToCategoryUntrackedItem(
        isFiltered: Boolean,
        isDarkTheme: Boolean,
    ): CategoryViewData {
        return CategoryViewData.Category(
            id = UNTRACKED_ITEM_ID,
            name = R.string.untracked_time_name
                .let(resourceRepo::getString),
            iconColor = getTextColor(
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            ),
            color = if (isFiltered) {
                colorMapper.toFilteredColor(isDarkTheme)
            } else {
                colorMapper.toUntrackedColor(isDarkTheme)
            },
        )
    }

    fun mapRecordTag(
        tag: RecordTag,
        type: RecordType?,
        isDarkTheme: Boolean,
        isFiltered: Boolean = false,
    ): CategoryViewData.Record {
        val icon = recordTagViewDataMapper.mapIcon(tag, type)
            ?.let(iconMapper::mapIcon)
        val color = recordTagViewDataMapper.mapColor(tag, type)

        return CategoryViewData.Record.Tagged(
            id = tag.id,
            name = tag.name,
            iconColor = getTextColor(isDarkTheme, isFiltered),
            iconAlpha = colorMapper.toIconAlpha(icon, isFiltered),
            color = getColor(color, isDarkTheme, isFiltered),
            icon = icon,
        )
    }

    fun groupToTagGroups(
        tags: List<RecordTag>,
    ): Map<String, List<RecordTag>> {
        // Sorted by group name.
        return tags.groupBy { mapRecordTagToTagGroupName(it) }.toSortedMap()
    }

    fun mapToUntaggedItem(
        isDarkTheme: Boolean,
        isFiltered: Boolean,
    ): CategoryViewData.Record.Untagged {
        return CategoryViewData.Record.Untagged(
            id = UNCATEGORIZED_ITEM_ID,
            name = R.string.change_record_untagged
                .let(resourceRepo::getString),
            iconColor = getTextColor(isDarkTheme, isFiltered),
            color = if (isFiltered) {
                colorMapper.toFilteredColor(isDarkTheme)
            } else {
                colorMapper.toUntrackedColor(isDarkTheme)
            },
            icon = RecordTypeIcon.Image(R.drawable.untagged),
        )
    }

    fun mapToTagUntrackedItem(
        isFiltered: Boolean,
        isDarkTheme: Boolean,
    ): CategoryViewData {
        return CategoryViewData.Record.Tagged(
            id = UNTRACKED_ITEM_ID,
            name = R.string.untracked_time_name
                .let(resourceRepo::getString),
            icon = RecordTypeIcon.Image(R.drawable.unknown),
            iconColor = getTextColor(
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            ),
            color = if (isFiltered) {
                colorMapper.toFilteredColor(isDarkTheme)
            } else {
                colorMapper.toUntrackedColor(isDarkTheme)
            },
        )
    }

    fun mapToTypeTagAddItem(isDarkTheme: Boolean): CategoryAddViewData {
        return map(type = TagType.RECORD_TYPE, isDarkTheme = isDarkTheme)
    }

    fun mapToRecordTagAddItem(isDarkTheme: Boolean): CategoryAddViewData {
        return map(type = TagType.RECORD, isDarkTheme = isDarkTheme)
    }

    fun mapToMultitaskItem(
        isFiltered: Boolean,
        isDarkTheme: Boolean,
    ): CategoryViewData {
        return CategoryViewData.Record.Tagged(
            id = MULTITASK_ITEM_ID,
            name = R.string.multitask_time_name
                .let(resourceRepo::getString),
            icon = RecordTypeIcon.Image(R.drawable.multitask),
            iconColor = getTextColor(
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            ),
            color = if (isFiltered) {
                colorMapper.toFilteredColor(isDarkTheme)
            } else {
                colorMapper.toUntrackedColor(isDarkTheme)
            },
        )
    }

    fun mapToRecordTagsEmpty(): ViewHolderType {
        return EmptyViewData(
            message = resourceRepo.getString(R.string.change_record_categories_empty),
        )
    }

    fun mapToCategoriesEmpty(): ViewHolderType {
        return EmptyViewData(
            message = resourceRepo.getString(R.string.change_record_type_categories_empty),
        )
    }

    fun mapToCategoryHint(): ViewHolderType = HintViewData(
        text = R.string.categories_record_type_hint
            .let(resourceRepo::getString),
    )

    fun mapToRecordTagHint(): ViewHolderType = HintViewData(
        text = R.string.categories_record_hint
            .let(resourceRepo::getString),
    )

    fun mapToCategoriesFirstHint(): ViewHolderType {
        return HintBigViewData(
            text = resourceRepo.getString(R.string.categories_record_type_hint),
            infoIconVisible = true,
            closeIconVisible = false,
        )
    }

    fun mapToTagsFirstHint(): ViewHolderType {
        return HintBigViewData(
            text = resourceRepo.getString(R.string.categories_record_hint),
            infoIconVisible = true,
            closeIconVisible = false,
        )
    }

    fun getTextColor(
        isDarkTheme: Boolean,
        isFiltered: Boolean,
    ): Int {
        return if (isFiltered) {
            colorMapper.toFilteredIconColor(isDarkTheme)
        } else {
            colorMapper.toIconColor(isDarkTheme)
        }
    }

    private fun getColor(
        color: AppColor,
        isDarkTheme: Boolean,
        isFiltered: Boolean,
    ): Int {
        return if (isFiltered) {
            colorMapper.toFilteredColor(isDarkTheme)
        } else {
            color.let { colorMapper.mapToColorInt(it, isDarkTheme) }
        }
    }

    private fun map(type: TagType, isDarkTheme: Boolean): CategoryAddViewData {
        val name = when (type) {
            TagType.RECORD_TYPE -> R.string.categories_add_category
            TagType.RECORD -> R.string.categories_add_record_tag
        }.let(resourceRepo::getString)

        return CategoryAddViewData(
            type = type,
            name = name,
            color = colorMapper.toInactiveColor(isDarkTheme),
        )
    }

    private fun mapRecordTagToTagGroupName(
        tag: RecordTag,
    ): String {
        return tag.name.substringBefore("::", "")
    }
}