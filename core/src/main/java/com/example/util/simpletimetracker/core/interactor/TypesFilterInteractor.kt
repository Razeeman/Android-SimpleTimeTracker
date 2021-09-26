package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.navigation.params.screen.TypesFilterParams
import javax.inject.Inject

class TypesFilterInteractor @Inject constructor(
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor
) {

    suspend fun getTypeIds(filter: TypesFilterParams): List<Long> {
        return when (filter.filterType) {
            ChartFilterType.ACTIVITY -> {
                filter.selectedIds
            }
            ChartFilterType.CATEGORY -> {
                recordTypeCategoryInteractor.getAll()
                    .filter { it.categoryId in filter.selectedIds }
                    .map { it.recordTypeId }
                    .distinct()
            }
        }
    }
}