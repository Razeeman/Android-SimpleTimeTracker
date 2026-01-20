package com.example.util.simpletimetracker.domain.recordType.interactor

import com.example.util.simpletimetracker.domain.activityFilter.interactor.ActivityFilterInteractor
import com.example.util.simpletimetracker.domain.activitySuggestion.interactor.ActivitySuggestionInteractor
import com.example.util.simpletimetracker.domain.record.repo.RecordRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.category.repo.RecordTypeCategoryRepo
import com.example.util.simpletimetracker.domain.complexRule.interactor.ComplexRuleInteractor
import com.example.util.simpletimetracker.domain.recordType.model.CardOrder
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordTypeToDefaultTagRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordTypeToTagRepo
import com.example.util.simpletimetracker.domain.record.repo.RunningRecordRepo
import com.example.util.simpletimetracker.domain.recordShortcut.repo.RecordShortcutRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordShortcutToRecordTagRepo
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.repo.RecordTypeRepo
import javax.inject.Inject

class RecordTypeInteractor @Inject constructor(
    private val recordTypeRepo: RecordTypeRepo,
    private val recordRepo: RecordRepo,
    private val recordShortcutRepo: RecordShortcutRepo,
    private val runningRecordRepo: RunningRecordRepo,
    private val recordTagRepo: RecordTagRepo,
    private val recordToRecordTagRepo: RecordToRecordTagRepo,
    private val recordShortcutToRecordTagRepo: RecordShortcutToRecordTagRepo,
    private val recordTypeCategoryRepo: RecordTypeCategoryRepo,
    private val recordTypeToTagRepo: RecordTypeToTagRepo,
    private val recordTypeToDefaultTagRepo: RecordTypeToDefaultTagRepo,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val complexRuleInteractor: ComplexRuleInteractor,
    private val activityFilterInteractor: ActivityFilterInteractor,
    private val activitySuggestionInteractor: ActivitySuggestionInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val sortCardsInteractor: SortCardsInteractor,
) {

    suspend fun getAll(cardOrder: CardOrder? = null): List<RecordType> {
        return sortCardsInteractor.sort(
            cardOrder = cardOrder ?: prefsInteractor.getCardOrder(),
            manualOrderProvider = { prefsInteractor.getCardOrderManual() },
            data = recordTypeRepo.getAll().map(::mapForSort),
        ).map { it.data }
    }

    suspend fun get(id: Long): RecordType? {
        return recordTypeRepo.get(id)
    }

    suspend fun get(name: String): List<RecordType> {
        return recordTypeRepo.get(name)
    }

    suspend fun add(recordType: RecordType): Long {
        return recordTypeRepo.add(recordType)
    }

    suspend fun archive(id: Long) {
        recordTypeRepo.archive(id)
    }

    suspend fun restore(id: Long) {
        recordTypeRepo.restore(id)
    }

    suspend fun remove(id: Long) {
        val recordsToRemove = recordRepo.getByType(listOf(id)).map { it.id }
        recordsToRemove.forEach { recordId ->
            recordToRecordTagRepo.removeAllByRecordId(recordId) // TODO do better?
        }
        val shortcutsToRemove = recordShortcutRepo.getByType(listOf(id)).map { it.id }
        shortcutsToRemove.forEach { shortcutId ->
            recordShortcutToRecordTagRepo.removeAllByShortcutId(shortcutId) // TODO do better?
        }
        val tagsToChange = recordTagRepo.getByType(id)
        if (tagsToChange.isNotEmpty()) {
            val type = recordTypeRepo.get(id)
            if (type != null) {
                tagsToChange.forEach { tag ->
                    val updatedTag = tag.copy(
                        color = type.color,
                        icon = type.icon,
                        iconColorSource = 0,
                    )
                    recordTagRepo.add(updatedTag)
                }
            }
        }
        prefsInteractor.getRecordTagSelectionExcludeActivities().toMutableList()
            .apply { remove(id) }
            .let { prefsInteractor.setRecordTagSelectionExcludeActivities(it) }
        prefsInteractor.getCommentInputExcludeActivities().toMutableList()
            .apply { remove(id) }
            .let { prefsInteractor.setCommentInputExcludeActivities(it) }
        prefsInteractor.getFilteredTypes().toMutableList()
            .apply { remove(id) }
            .let { prefsInteractor.setFilteredTypes(it) }
        prefsInteractor.getFilteredTypesOnList().toMutableList()
            .apply { remove(id) }
            .let { prefsInteractor.setFilteredTypesOnList(it) }
        recordRepo.removeByType(id)
        recordShortcutRepo.removeByType(id)
        runningRecordRepo.remove(id)
        recordTypeCategoryRepo.removeAllByType(id)
        recordTypeToTagRepo.removeAllByType(id)
        recordTypeToDefaultTagRepo.removeAllByType(id)
        recordTypeGoalInteractor.removeByType(id)
        complexRuleInteractor.removeTypeId(id)
        activityFilterInteractor.removeTypeId(id)
        activitySuggestionInteractor.removeTypeId(id)
        recordTypeRepo.remove(id)
    }

    fun mapForSort(
        data: RecordType,
    ): SortCardsInteractor.DataHolder<RecordType> {
        return SortCardsInteractor.DataHolder(
            id = data.id,
            name = data.name,
            color = data.color,
            data = data,
        )
    }
}