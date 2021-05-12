package com.example.util.simpletimetracker.feature_change_running_record.interactor

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_change_running_record.mapper.ChangeRunningRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_running_record.viewData.ChangeRunningRecordViewData
import javax.inject.Inject

class ChangeRunningRecordViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val changeRunningRecordViewDataMapper: ChangeRunningRecordViewDataMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper
) {

    suspend fun getPreviewViewData(record: RunningRecord): ChangeRunningRecordViewData {
        val type = recordTypeInteractor.get(record.id)
        val tag = recordTagInteractor.get(record.tagId)
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()

        return changeRunningRecordViewDataMapper.map(
            runningRecord = record,
            recordType = type,
            recordTag = tag,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime
        )
    }

    suspend fun getTypesViewData(): List<ViewHolderType> {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()

        return recordTypeInteractor.getAll()
            .filter { !it.hidden }
            .takeUnless { it.isEmpty() }
            ?.map { recordTypeViewDataMapper.map(it, numberOfCards, isDarkTheme) }
            ?: recordTypeViewDataMapper.mapToEmpty()
    }

    suspend fun getCategoriesViewData(newTypeId: Long): List<ViewHolderType> {
        if (newTypeId == 0L) {
            return categoryViewDataMapper.mapToTypeNotSelected()
        }

        val isDarkTheme = prefsInteractor.getDarkMode()
        val type = recordTypeInteractor.get(newTypeId)

        return recordTagInteractor.getByType(newTypeId)
            .filterNot { it.archived }
            .takeUnless { it.isEmpty() }
            ?.map { categoryViewDataMapper.map(it, type, isDarkTheme) }
            ?.plus(categoryViewDataMapper.mapUntagged(isDarkTheme))
            ?: categoryViewDataMapper.mapToCategoriesEmpty()
    }
}