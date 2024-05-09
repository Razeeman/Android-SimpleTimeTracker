package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.extension.toRange
import com.example.util.simpletimetracker.domain.mapper.OverlappingRangesMapper
import com.example.util.simpletimetracker.domain.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.model.MultitaskRecord
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RunningRecord
import javax.inject.Inject

class GetMultitaskRecordsInteractor @Inject constructor(
    private val overlappingRangesMapper: OverlappingRangesMapper,
    private val rangeMapper: RangeMapper,
) {

    private sealed interface Id : OverlappingRangesMapper.Id {
        data class RecordId(val id: Long) : Id
        data class RunningRecordId(val id: Long) : Id
    }

    fun get(
        records: List<RecordBase>,
    ): List<MultitaskRecord> {
        val recordsMap: Map<Id, RecordBase> = records.mapNotNull {
            val id = when (it) {
                is Record -> Id.RecordId(it.id)
                is RunningRecord -> Id.RunningRecordId(it.id)
                is MultitaskRecord -> return@mapNotNull null
            }
            id to it
        }.toMap()
        val segments = recordsMap.map { (id, record) ->
            id to record.toRange()
        }
        val overlappedSegments = overlappingRangesMapper.map(segments)

        return overlappedSegments.mapNotNull { (ids, range) ->
            ids
                .mapNotNull { id -> recordsMap[id] }
                .map {
                    if (it is RunningRecord) {
                        rangeMapper.mapRunningRecordToRecord(it)
                    } else {
                        it
                    }
                }
                .map { rangeMapper.clampRecordToRange(it, range) }
                .filterIsInstance<Record>()
                .takeUnless(List<RecordBase>::isEmpty)
                ?.let(::MultitaskRecord)
        }
    }
}