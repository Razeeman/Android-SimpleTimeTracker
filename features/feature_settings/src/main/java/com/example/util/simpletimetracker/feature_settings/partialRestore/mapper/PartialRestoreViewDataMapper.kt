package com.example.util.simpletimetracker.feature_settings.partialRestore.mapper

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.PartialBackupRestoreData
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.partialRestore.model.PartialRestoreFilterType
import javax.inject.Inject

class PartialRestoreViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun mapFilterName(
        filter: PartialRestoreFilterType,
        selectedIds: List<Long>,
    ): String {
        val nameText = when (filter) {
            PartialRestoreFilterType.Activities -> R.string.activity_hint
            PartialRestoreFilterType.Categories -> R.string.category_hint
            PartialRestoreFilterType.Tags -> R.string.record_tag_hint_short
            PartialRestoreFilterType.Records -> R.string.shortcut_navigation_records
            PartialRestoreFilterType.ActivityFilters -> R.string.change_activity_filters_hint
            PartialRestoreFilterType.FavouriteComments -> R.string.change_record_favourite_comments_hint_long
            PartialRestoreFilterType.FavouriteColors -> R.string.change_record_favourite_colors_hint
            PartialRestoreFilterType.FavouriteIcons -> R.string.change_record_favourite_icons_hint
            PartialRestoreFilterType.ComplexRules -> R.string.settings_complex_rules
        }.let(resourceRepo::getString)
        val countText = selectedIds.size
            .takeUnless { it == 0 }
            ?.let { "($it)" }
            .orEmpty()

        return nameText + countText
    }

    fun mapFilteredData(
        filters: Map<PartialRestoreFilterType, Set<Long>>,
        data: PartialBackupRestoreData,
    ): PartialBackupRestoreData {
        return data.copy(
            types = data.types
                .filter { it.key !in filters[PartialRestoreFilterType.Activities].orEmpty() },
            records = data.records
                .filter { it.key !in filters[PartialRestoreFilterType.Records].orEmpty() },
            categories = data.categories
                .filter { it.key !in filters[PartialRestoreFilterType.Categories].orEmpty() },
            typeToCategory = data.typeToCategory,
            tags = data.tags
                .filter { it.key !in filters[PartialRestoreFilterType.Tags].orEmpty() },
            recordToTag = data.recordToTag,
            typeToTag = data.typeToTag,
            typeToDefaultTag = data.typeToDefaultTag,
            activityFilters = data.activityFilters
                .filter { it.key !in filters[PartialRestoreFilterType.ActivityFilters].orEmpty() },
            favouriteComments = data.favouriteComments
                .filter { it.key !in filters[PartialRestoreFilterType.FavouriteComments].orEmpty() },
            favouriteColors = data.favouriteColors
                .filter { it.key !in filters[PartialRestoreFilterType.FavouriteColors].orEmpty() },
            favouriteIcon = data.favouriteIcon
                .filter { it.key !in filters[PartialRestoreFilterType.FavouriteIcons].orEmpty() },
            goals = data.goals,
            rules = data.rules
                .filter { it.key !in filters[PartialRestoreFilterType.ComplexRules].orEmpty() },
        )
    }
}