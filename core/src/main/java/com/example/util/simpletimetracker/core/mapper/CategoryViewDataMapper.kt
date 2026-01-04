package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.ARCHIVED_BUTTON_ITEM_ID
import com.example.util.simpletimetracker.domain.base.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.category.model.Category
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryAddViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryShowAllViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.TagType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.HintBigViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class CategoryViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val iconMapper: IconMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTagViewDataMapper: RecordTagViewDataMapper,
    private val recordTagValueMapper: RecordTagValueMapper,
) {

    fun mapCategory(
        category: Category,
        isDarkTheme: Boolean,
        isFiltered: Boolean = false,
    ): CategoryViewData.Category {
        return CategoryViewData.Category(
            id = category.id,
            name = category.name,
            iconColor = colorMapper.toIconColor(
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            ),
            color = colorMapper.toFilteredColor(
                color = category.color,
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            ),
        )
    }

    fun mapToUncategorizedItem(
        isFiltered: Boolean,
        isDarkTheme: Boolean,
    ): CategoryViewData.Category {
        return CategoryViewData.Category(
            id = UNCATEGORIZED_ITEM_ID,
            name = R.string.uncategorized_time_name
                .let(resourceRepo::getString),
            iconColor = colorMapper.toIconColor(
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            ),
            color = colorMapper.toFilteredUntrackedColor(
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            ),
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
            iconColor = colorMapper.toIconColor(
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            ),
            color = colorMapper.toFilteredUntrackedColor(
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            ),
        )
    }

    fun mapRecordTag(
        tag: RecordTag,
        types: Map<Long, RecordType>,
        isDarkTheme: Boolean,
        isFiltered: Boolean = false,
    ): CategoryViewData.Record.Tagged {
        val icon = recordTagViewDataMapper.mapIcon(tag, types)
            ?.let(iconMapper::mapIcon)
        val color = recordTagViewDataMapper.mapColor(tag, types)

        return CategoryViewData.Record.Tagged(
            id = tag.id,
            name = tag.name,
            iconColor = colorMapper.toIconColor(
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            ),
            iconAlpha = colorMapper.toIconAlpha(icon, isFiltered),
            color = colorMapper.toFilteredColor(
                color = color,
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            ),
            icon = icon,
        )
    }

    fun mapRecordTagWithValue(
        tag: RecordTag,
        tagData: RecordBase.Tag?,
        types: Map<Long, RecordType>,
        isDarkTheme: Boolean,
        isFiltered: Boolean = false,
    ): CategoryViewData.Record {
        val viewData = mapRecordTag(
            tag = tag,
            types = types,
            isDarkTheme = isDarkTheme,
            isFiltered = isFiltered,
        )
        val value = tagData?.numericValue
        return if (value != null) {
            val newName = recordTagValueMapper.getNameWithValue(
                name = viewData.name,
                value = value,
                valueSuffix = tag.valueSuffix,
            )
            return viewData.copy(name = newName)
        } else {
            viewData
        }
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
            iconColor = colorMapper.toIconColor(
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            ),
            color = colorMapper.toFilteredUntrackedColor(
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            ),
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
            iconColor = colorMapper.toIconColor(
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            ),
            color = colorMapper.toFilteredUntrackedColor(
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            ),
        )
    }

    fun mapToTagArchiveItem(
        isEnabled: Boolean,
        isDarkTheme: Boolean,
    ): CategoryViewData {
        return CategoryViewData.Record.Tagged(
            id = ARCHIVED_BUTTON_ITEM_ID,
            name = R.string.settings_archive
                .let(resourceRepo::getString),
            icon = RecordTypeIcon.Image(R.drawable.archive),
            iconColor = colorMapper.toIconColor(
                isDarkTheme = isDarkTheme,
                isFiltered = false,
            ),
            color = if (isEnabled) {
                colorMapper.toActiveColor(isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            },
        )
    }

    fun mapToTypeTagAddItem(isDarkTheme: Boolean): CategoryAddViewData {
        return map(type = TagType.RECORD_TYPE, isDarkTheme = isDarkTheme)
    }

    fun mapToRecordTagAddItem(isDarkTheme: Boolean): CategoryAddViewData {
        return map(type = TagType.RECORD, isDarkTheme = isDarkTheme)
    }

    fun mapToRecordTagShowAllItem(isDarkTheme: Boolean): CategoryShowAllViewData {
        return CategoryShowAllViewData(
            name = resourceRepo.getString(R.string.types_filter_show_all),
            color = colorMapper.toInactiveColor(isDarkTheme),
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