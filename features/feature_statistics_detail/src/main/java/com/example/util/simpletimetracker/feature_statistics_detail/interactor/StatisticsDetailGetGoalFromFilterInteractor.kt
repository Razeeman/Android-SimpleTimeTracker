package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.domain.extension.getCategoryIds
import com.example.util.simpletimetracker.domain.extension.getTypeIds
import com.example.util.simpletimetracker.domain.extension.hasActivityFilter
import com.example.util.simpletimetracker.domain.extension.hasCategoryFilter
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import javax.inject.Inject

class StatisticsDetailGetGoalFromFilterInteractor @Inject constructor(
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
) {

    suspend fun execute(
        filter: List<RecordsFilter>,
    ): List<RecordTypeGoal> {
        return when {
            filter.hasActivityFilter() -> {
                // Show goal only if one activity is selected.
                val typeIds = filter.getTypeIds()
                if (typeIds.size != 1) return emptyList()
                val typeId = typeIds.firstOrNull() ?: return emptyList()
                recordTypeGoalInteractor.getByType(typeId)
            }
            filter.hasCategoryFilter() -> {
                // Show goal only if one category is selected.
                val categoryIds = filter.getCategoryIds()
                if (categoryIds.size != 1) return emptyList()
                val categoryId = categoryIds.firstOrNull() ?: return emptyList()
                recordTypeGoalInteractor.getByCategory(categoryId)
            }
            else -> emptyList()
        }
    }
}