package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.viewData.RecordTypeSuggestionType
import com.example.util.simpletimetracker.domain.activitySuggestion.interactor.GetCurrentActivitySuggestionsInteractor
import com.example.util.simpletimetracker.domain.extension.search
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSuggestion.RecordTypeSuggestionViewData
import javax.inject.Inject

class ActivitySuggestionViewDataInteractor @Inject constructor(
    private val getCurrentActivitySuggestionsInteractor: GetCurrentActivitySuggestionsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
) {

    suspend fun getSuggestionsViewData(
        recordTypesMap: Map<Long, RecordType>,
        goals: Map<Long, List<RecordTypeGoal>>,
        runningRecords: List<RunningRecord>,
        allDailyCurrents: Map<Long, GetCurrentRecordsDurationInteractor.Result>,
        completeTypeIds: Set<Long>,
        searchText: String,
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): List<RecordTypeSuggestionViewData> {
        val suggestionTypes = getCurrentActivitySuggestionsInteractor.execute(
            recordTypesMap = recordTypesMap,
            runningRecords = runningRecords,
        )

        return suggestionTypes.map { recordType ->
            recordTypeViewDataMapper.map(
                recordType = recordType,
                numberOfCards = numberOfCards,
                isDarkTheme = isDarkTheme,
                checkStates = recordTypeViewDataMapper.mapGoalCheckmarks(
                    type = recordType,
                    goals = goals,
                    allDailyCurrents = allDailyCurrents,
                ),
                isComplete = recordType.id in completeTypeIds,
            ).let {
                RecordTypeSuggestionViewData(
                    data = it,
                    type = RecordTypeSuggestionType,
                )
            }
        }.search(
            text = searchText,
            searchableContent = { data.name },
        )
    }
}