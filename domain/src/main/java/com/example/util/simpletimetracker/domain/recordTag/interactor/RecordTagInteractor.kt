package com.example.util.simpletimetracker.domain.recordTag.interactor

import com.example.util.simpletimetracker.domain.complexRule.interactor.ComplexRuleInteractor
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.CardTagOrder
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTypeToTag
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordShortcutToRecordTagRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordTypeToDefaultTagRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordTypeToTagRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RunningRecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.SortCardsInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import javax.inject.Inject

class RecordTagInteractor @Inject constructor(
    private val repo: RecordTagRepo,
    private val recordToRecordTagRepo: RecordToRecordTagRepo,
    private val runningRecordToRecordTagRepo: RunningRecordToRecordTagRepo,
    private val recordShortcutToRecordTagRepo: RecordShortcutToRecordTagRepo,
    private val recordTypeToTagRepo: RecordTypeToTagRepo,
    private val recordTypeToDefaultTagRepo: RecordTypeToDefaultTagRepo,
    private val complexRuleInteractor: ComplexRuleInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val sortCardsInteractor: SortCardsInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
) {

    suspend fun isEmpty(): Boolean {
        return repo.isEmpty()
    }

    suspend fun getAll(cardOrder: CardTagOrder? = null): List<RecordTag> {
        val tags = repo.getAll()
        val types = recordTypeInteractor.getAll()
        val typesMap = types.associateBy { it.id }
        val getActivityOrderProvider: suspend () -> Map<Long, Long> = {
            getActivityOrderProvider(
                tags = tags,
                typesMap = typesMap,
                typesToTags = recordTypeToTagRepo.getAll(),
            )
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

    suspend fun get(name: String): List<RecordTag> {
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
        prefsInteractor.getFilteredTags().toMutableList()
            .apply { remove(id) }
            .let { prefsInteractor.setFilteredTags(it) }
        prefsInteractor.getFilteredTagsOnList().toMutableList()
            .apply { remove(id) }
            .let { prefsInteractor.setFilteredTagsOnList(it) }
        repo.remove(id)
        recordToRecordTagRepo.removeAllByTagId(id)
        runningRecordToRecordTagRepo.removeAllByTagId(id)
        recordShortcutToRecordTagRepo.removeAllByTagId(id)
        recordTypeToTagRepo.removeAll(id)
        recordTypeToDefaultTagRepo.removeAll(id)
        recordTypeGoalInteractor.removeByTag(id)
        complexRuleInteractor.removeTagId(id)
    }

    fun getActivityOrderProvider(
        tags: List<RecordTag>,
        typesMap: Map<Long, RecordType>,
        typesToTags: List<RecordTypeToTag>,
    ): Map<Long, Long> {
        val types = typesMap.values
        val tagsToAssignedTypes = typesToTags
            .groupBy { it.tagId }
            .mapValues { (_, typeToTag) ->
                typeToTag
                    .map { it.recordTypeId }
                    .sortedBy { typeId -> types.indexOfFirst { it.id == typeId } }
            }
        return tags.associate { tag ->
            val mainTypeId = tagsToAssignedTypes[tag.id]?.firstOrNull().orZero()
            val type = typesMap[mainTypeId]
            val index = types.indexOf(type).toLong()
                // Put general tags at the end.
                .takeUnless { it == -1L }
                ?: Long.MAX_VALUE
            tag.id to index
        }
    }

    fun mapForSort(
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