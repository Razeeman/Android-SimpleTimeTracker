package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.repo.PrefsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PrefsInteractor @Inject constructor(
    private val prefsRepo: PrefsRepo
) {

    suspend fun getFilteredTypes(): List<Long> = withContext(Dispatchers.IO) {
        prefsRepo.recordTypesFilteredOnChart
            .mapNotNull(String::toLongOrNull)
    }

    suspend fun setFilteredTypes(typeIdsFiltered: List<Long>) = withContext(Dispatchers.IO) {
        prefsRepo.recordTypesFilteredOnChart = typeIdsFiltered
            .map(Long::toString).toSet()
    }

    suspend fun getSortRecordTypesByColor(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.sortRecordTypesByColor
    }

    suspend fun setSortRecordTypesByColor(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.sortRecordTypesByColor = isEnabled
    }
}