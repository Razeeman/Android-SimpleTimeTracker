package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.domain.record.extension.getCategoryIds
import com.example.util.simpletimetracker.domain.record.extension.getTagIds
import com.example.util.simpletimetracker.domain.record.extension.getTypeIds
import com.example.util.simpletimetracker.domain.record.extension.hasSelectedActivityFilter
import com.example.util.simpletimetracker.domain.record.extension.hasSelectedCategoryFilter
import com.example.util.simpletimetracker.domain.record.extension.hasSelectedTagsFilter
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import javax.inject.Inject

class StatisticsDetailGetGoalFromFilterInteractor @Inject constructor(
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
) {

    suspend fun execute(
        filter: List<RecordsFilter>,
    ): List<RecordTypeGoal> {
        return when {
            filter.hasSelectedActivityFilter() -> {
                // Show goal only if one activity is selected.
                val typeIds = filter.getTypeIds()
                if (typeIds.size != 1) return emptyList()
                val typeId = typeIds.firstOrNull() ?: return emptyList()
                recordTypeGoalInteractor.getByType(typeId)
            }
            filter.hasSelectedCategoryFilter() -> {
                // Show goal only if one category is selected.
                val categoryIds = filter.getCategoryIds()
                if (categoryIds.size != 1) return emptyList()
                val categoryId = categoryIds.firstOrNull() ?: return emptyList()
                recordTypeGoalInteractor.getByCategory(categoryId)
            }
            filter.hasSelectedTagsFilter() -> {
                // Show goal only if one tag is selected.
                val tagIds = filter.getTagIds()
                if (tagIds.size != 1) return emptyList()
                val tagId = tagIds.firstOrNull() ?: return emptyList()
                recordTypeGoalInteractor.getByTag(tagId)
            }
            else -> emptyList()
        }
    }
}