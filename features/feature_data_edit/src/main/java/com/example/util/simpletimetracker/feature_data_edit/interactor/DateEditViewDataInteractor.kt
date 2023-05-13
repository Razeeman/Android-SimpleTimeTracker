package com.example.util.simpletimetracker.feature_data_edit.interactor

import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_data_edit.R
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeActivityState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeButtonState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeCommentState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditRecordsCountState
import javax.inject.Inject

class DateEditViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordFilterInteractor: RecordFilterInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun getSelectedRecordsCount(
        filters: List<RecordsFilter>,
    ): DataEditRecordsCountState {
        val records = recordFilterInteractor.getByFilter(filters)
        val selectedRecordsCount = records.size
        val recordsString = resourceRepo.getQuantityString(
            R.plurals.statistics_detail_times_tracked,
            selectedRecordsCount
        ).lowercase()

        return DataEditRecordsCountState(
            count = selectedRecordsCount,
            countText = "$selectedRecordsCount $recordsString"
        )
    }

    suspend fun getChangeActivityState(
        newTypeId: Long,
    ): DataEditChangeActivityState {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val type = newTypeId.let { recordTypeInteractor.get(it) }

        return if (type == null) {
            DataEditChangeActivityState.Disabled
        } else {
            DataEditChangeActivityState.Enabled(
                recordTypeViewDataMapper.map(
                    recordType = type,
                    isDarkTheme = isDarkTheme,
                )
            )
        }
    }

    fun getChangeCommentState(
        newComment: String,
    ): DataEditChangeCommentState {
        return DataEditChangeCommentState.Enabled(newComment)
    }

    suspend fun getTagState(
        tagIds: List<Long>,
    ): List<CategoryViewData.Record> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val types = recordTypeInteractor.getAll().associateBy { it.id }

        return recordTagInteractor.getAll()
            .filter { it.id in tagIds }
            .map {
                categoryViewDataMapper.mapRecordTag(
                    tag = it,
                    type = types[it.typeId],
                    isDarkTheme = isDarkTheme,
                )
            }
    }

    suspend fun getChangeButtonState(
        enabled: Boolean,
    ): DataEditChangeButtonState {
        val theme = if (prefsInteractor.getDarkMode()) {
            R.style.AppThemeDark
        } else {
            R.style.AppTheme
        }

        return DataEditChangeButtonState(
            enabled = enabled,
            backgroundTint = (if (enabled) R.attr.appActiveColor else R.attr.appInactiveColor)
                .let { resourceRepo.getThemedAttr(it, theme) },
        )
    }

    fun filterTags(
        typeForTagSelection: Long?,
        tags: List<CategoryViewData.Record>,
        allTags: Map<Long, RecordTag>,
    ): List<CategoryViewData.Record> {
        // If there is a specific type selected by filter or change activity state,
        val typeId = typeForTagSelection.orZero()
        // Filter tags selected to add to have typed tags only for this selected activity.
        val newTags = tags.filter {
            val tag = allTags[it.id] ?: return@filter false
            tag.typeId == 0L || tag.typeId == typeId
        }
        return newTags
    }
}