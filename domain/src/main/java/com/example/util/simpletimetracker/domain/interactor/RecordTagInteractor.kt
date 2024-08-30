package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.CardTagOrder
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeToDefaultTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeToTagRepo
import com.example.util.simpletimetracker.domain.repo.RunningRecordToRecordTagRepo
import javax.inject.Inject

class RecordTagInteractor @Inject constructor(
    private val repo: RecordTagRepo,
    private val recordToRecordTagRepo: RecordToRecordTagRepo,
    private val runningRecordToRecordTagRepo: RunningRecordToRecordTagRepo,
    private val recordTypeToTagRepo: RecordTypeToTagRepo,
    private val recordTypeToDefaultTagRepo: RecordTypeToDefaultTagRepo,
    private val complexRuleInteractor: ComplexRuleInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val sortCardsInteractor: SortCardsInteractor,
) {

    suspend fun isEmpty(): Boolean {
        return repo.isEmpty()
    }

    suspend fun getAll(cardOrder: CardTagOrder? = null): List<RecordTag> {
        val tags = repo.getAll()
        val types = recordTypeInteractor.getAll()
        val typesMap = types.associateBy { it.id }

        val getActivityOrderProvider: suspend () -> Map<Long, Long> = {
            val typesToTags = recordTypeToTagRepo.getAll()
            val tagsToAssignedTypes = typesToTags
                .groupBy { it.tagId }
                .mapValues { (_, typeToTag) ->
                    typeToTag
                        .map { it.recordTypeId }
                        .sortedBy { typeId -> types.indexOfFirst { it.id == typeId } }
                }
            tags.associate { tag ->
                val mainTypeId = tagsToAssignedTypes[tag.id]?.firstOrNull().orZero()
                val type = typesMap[mainTypeId]
                val index = types.indexOf(type).toLong()
                    // Put general tags at the end.
                    .takeUnless { it == -1L }
                    ?: Long.MAX_VALUE
                tag.id to index
            }
        }

        return sortCardsInteractor.sortTags(
            cardTagOrder = cardOrder ?: prefsInteractor.getTagOrder(),
            manualOrderProvider = { prefsInteractor.getTagOrderManual() },
            activityOrderProvider = { getActivityOrderProvider() },
            data = tags.map {
                mapForSort(
                    data = it,
                    colorSource = typesMap[it.iconColorSource],
                )
            },
        ).map { it.data }
    }

    suspend fun get(id: Long): RecordTag? {
        return repo.get(id)
    }

    suspend fun get(name: String): RecordTag? {
        return repo.get(name)
    }

    suspend fun add(tag: RecordTag): Long {
        return repo.add(tag)
    }

    suspend fun archive(id: Long) {
        repo.archive(id)
    }

    suspend fun restore(id: Long) {
        repo.restore(id)
    }

    suspend fun remove(id: Long) {
        repo.remove(id)
        recordToRecordTagRepo.removeAllByTagId(id)
        runningRecordToRecordTagRepo.removeAllByTagId(id)
        recordTypeToTagRepo.removeAll(id)
        recordTypeToDefaultTagRepo.removeAll(id)
        complexRuleInteractor.removeTagId(id)
    }

    private fun mapForSort(
        data: RecordTag,
        colorSource: RecordType?,
    ): SortCardsInteractor.DataHolder<RecordTag> {
        return SortCardsInteractor.DataHolder(
            id = data.id,
            name = data.name,
            color = colorSource?.color ?: data.color,
            data = data,
        )
    }
}