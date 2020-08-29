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

    suspend fun getShowUntrackedInRecords(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.showUntrackedInRecords
    }

    suspend fun setShowUntrackedInRecords(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.showUntrackedInRecords = isEnabled
    }

    suspend fun getAllowMultitasking(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.allowMultitasking
    }

    suspend fun setAllowMultitasking(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.allowMultitasking = isEnabled
    }

    suspend fun getNumberOfCards(): Int = withContext(Dispatchers.IO) {
        prefsRepo.numberOfCards
    }

    suspend fun setNumberOfCards(cardSize: Int) = withContext(Dispatchers.IO) {
        prefsRepo.numberOfCards = cardSize
    }

    suspend fun setWidget(widgetId: Int, recordType: Long) = withContext(Dispatchers.IO) {
        prefsRepo.setWidget(widgetId, recordType)
    }

    suspend fun getWidget(widgetId: Int): Long = withContext(Dispatchers.IO) {
        prefsRepo.getWidget(widgetId)
    }

    suspend fun removeWidget(widgetId: Int) = withContext(Dispatchers.IO) {
        prefsRepo.removeWidget(widgetId)
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        prefsRepo.clear()
    }
}