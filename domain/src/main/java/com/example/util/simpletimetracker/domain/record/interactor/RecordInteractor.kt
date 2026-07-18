package com.example.util.simpletimetracker.domain.record.interactor

import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.repo.RecordRepo
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordToRecordTagRepo
import javax.inject.Inject

class RecordInteractor @Inject constructor(
    private val recordRepo: RecordRepo,
    private val recordToRecordTagRepo: RecordToRecordTagRepo,
) {

    suspend fun isEmpty(): Boolean {
        return recordRepo.isEmpty()
    }

    suspend fun getAll(): List<Record> {
        return recordRepo.getAll()
    }

    suspend fun getWithParams(param: GetParam): List<Record> {
        return when (param) {
            is GetParam.Type -> recordRepo.getByType(param.ids)
            is GetParam.TypeWithAnyComment -> recordRepo.getByTypeWithAnyComment(param.ids)
            is GetParam.TypeWithComment -> recordRepo.searchByTypeWithComment(param.ids, param.text)
            is GetParam.Comment -> recordRepo.searchComment(param.text)
            is GetParam.AnyComment -> recordRepo.searchAnyComments()
            is GetParam.Tagged -> recordRepo.getTagged(param.ids)
            is GetParam.Untagged -> recordRepo.getUntagged()
            is GetParam.FromRange -> recordRepo.getFromRange(param.range)
            is GetParam.FromRangeByType -> recordRepo.getFromRangeByType(param.ids, param.range)
        }
    }

    suspend fun get(id: Long): Record? {
        return recordRepo.get(id)
    }

    suspend fun searchSimilarComments(
        text: String,
        limit: Int,
    ): List<String> {
        return recordRepo.searchSimilarComments(text, limit)
    }

    suspend fun getRecentComments(
        typeId: Long,
        limit: Int,
    ): List<String> {
        return recordRepo.getRecentComments(typeId, limit)
    }

    suspend fun getPrev(
        timeStarted: Long,
        ignoreTypeIds: List<Long> = emptyList(),
    ): Record? {
        return recordRepo.getPrev(timeStarted, ignoreTypeIds)
    }

    // Can return several records ended at the same time.
    suspend fun getAllPrev(timeStarted: Long): List<Record> {
        val prev = recordRepo.getPrev(timeStarted) ?: return emptyList()
        return recordRepo.getByTimeEnded(prev.timeEnded)
    }

    suspend fun getNext(timeEnded: Long): Record? {
        return recordRepo.getNext(timeEnded)
    }

    // Can return several records ended at the same time.
    suspend fun getAllNext(timeStarted: Long): List<Record> {
        val prev = recordRepo.getNext(timeStarted) ?: return emptyList()
        return recordRepo.getByTimeStarted(prev.timeStarted)
    }

    suspend fun addFromRunning(
        runningRecord: RunningRecord,
        timeEnded: Long,
    ) {
        Record(
            typeId = runningRecord.id,
            timeStarted = runningRecord.timeStarted,
            timeEnded = timeEnded,
            comment = runningRecord.comment,
            tags = runningRecord.tags,
        ).let {
            add(it)
        }
    }

    suspend fun add(record: Record) {
        val recordId = recordRepo.add(record)
        updateTags(recordId, record.tags)
    }

    suspend fun update(
        recordId: Long,
        typeId: Long,
        comment: String,
        tags: List<RecordBase.Tag>,
    ) {
        recordRepo.update(
            recordId = recordId,
            typeId = typeId,
            comment = comment,
        )
        updateTags(recordId, tags)
    }

    suspend fun updateTimeEnded(recordId: Long, timeEnded: Long) {
        recordRepo.updateTimeEnded(
            recordId = recordId,
            timeEnded = timeEnded,
        )
    }

    suspend fun remove(id: Long) {
        recordToRecordTagRepo.removeAllByRecordId(id)
        recordRepo.remove(id)
    }

    suspend fun removeAll() {
        recordToRecordTagRepo.clear()
        recordRepo.clear()
    }

    private suspend fun updateTags(
        recordId: Long,
        tags: List<RecordBase.Tag>,
    ) {
        recordToRecordTagRepo.removeAllByRecordId(recordId)
        recordToRecordTagRepo.addRecordTags(recordId, tags)
    }

    sealed interface GetParam {
        data class Type(val ids: Set<Long>) : GetParam
        data class TypeWithAnyComment(val ids: Set<Long>) : GetParam
        data class TypeWithComment(val ids: Set<Long>, val text: String) : GetParam
        data class Comment(val text: String) : GetParam
        data object AnyComment : GetParam
        data class Tagged(val ids: Set<Long>) : GetParam
        data object Untagged : GetParam
        data class FromRange(val range: Range) : GetParam
        data class FromRangeByType(val ids: Set<Long>, val range: Range) : GetParam
    }
}
