package com.example.util.simpletimetracker.feature_settings.partialRestore.utils

import com.example.util.simpletimetracker.domain.model.PartialBackupRestoreData
import com.example.util.simpletimetracker.feature_settings.partialRestore.model.PartialRestoreFilterType

fun PartialBackupRestoreData.getIds(
    filter: PartialRestoreFilterType,
): Set<Long> {
    return when (filter) {
        is PartialRestoreFilterType.Activities -> types
        is PartialRestoreFilterType.Categories -> categories
        is PartialRestoreFilterType.Tags -> tags
        is PartialRestoreFilterType.Records -> records
        is PartialRestoreFilterType.ActivityFilters -> activityFilters
        is PartialRestoreFilterType.FavouriteComments -> favouriteComments
        is PartialRestoreFilterType.FavouriteColors -> favouriteColors
        is PartialRestoreFilterType.FavouriteIcons -> favouriteIcon
        is PartialRestoreFilterType.ComplexRules -> rules
    }.keys
}