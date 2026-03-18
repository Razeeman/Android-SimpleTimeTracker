package com.example.util.simpletimetracker.domain.backup.model

import com.example.util.simpletimetracker.domain.activityFilter.model.ActivityFilter
import com.example.util.simpletimetracker.domain.activitySuggestion.model.ActivitySuggestion
import com.example.util.simpletimetracker.domain.category.model.Category
import com.example.util.simpletimetracker.domain.category.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.complexRule.model.ComplexRule
import com.example.util.simpletimetracker.domain.favourite.model.FavouriteColor
import com.example.util.simpletimetracker.domain.favourite.model.FavouriteComment
import com.example.util.simpletimetracker.domain.favourite.model.FavouriteIcon
import com.example.util.simpletimetracker.domain.favourite.model.RecordTypeToFavouriteComment
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import com.example.util.simpletimetracker.domain.recordTag.model.RecordShortcutToRecordTag
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordTag.model.RecordToRecordTag
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTypeToDefaultTag
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTypeToTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal

// TODO switch to LongObjectMap from androidx.collections
data class PartialBackupRestoreData(
    val types: Map<Long, Holder<RecordType>>,
    val records: Map<Long, Holder<Record>>,
    val recordShortcuts: Map<Long, Holder<RecordShortcut>>,
    val categories: Map<Long, Holder<Category>>,
    val typeToCategory: List<Holder<RecordTypeCategory>>,
    val tags: Map<Long, Holder<RecordTag>>,
    val recordToTag: List<Holder<RecordToRecordTag>>,
    val recordShortcutToTag: List<Holder<RecordShortcutToRecordTag>>,
    val typeToTag: List<Holder<RecordTypeToTag>>,
    val typeToDefaultTag: List<Holder<RecordTypeToDefaultTag>>,
    val activityFilters: Map<Long, Holder<ActivityFilter>>,
    val favouriteComments: Map<Long, Holder<FavouriteComment>>,
    val typeToFavouriteComment: List<Holder<RecordTypeToFavouriteComment>>,
    val favouriteColors: Map<Long, Holder<FavouriteColor>>,
    val favouriteIcon: Map<Long, Holder<FavouriteIcon>>,
    val goals: Map<Long, Holder<RecordTypeGoal>>,
    val rules: Map<Long, Holder<ComplexRule>>,
    val activitySuggestions: Map<Long, Holder<ActivitySuggestion>>,
) {

    data class Holder<T>(
        val exist: Boolean,
        val data: T,
    )
}

fun <T> Collection<PartialBackupRestoreData.Holder<T>>.getNotExistingValues(): List<T> {
    return this.mapNotNull { if (!it.exist) it.data else null }
}

fun <T> Collection<PartialBackupRestoreData.Holder<T>>.getExistingValues(): List<T> {
    return this.mapNotNull { if (it.exist) it.data else null }
}