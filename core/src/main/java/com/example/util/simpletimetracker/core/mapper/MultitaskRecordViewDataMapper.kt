package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.domain.model.MultitaskRecord
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.multitaskRecord.customView.MultitaskRecordView
import com.example.util.simpletimetracker.feature_base_adapter.multitaskRecord.MultitaskRecordViewData
import javax.inject.Inject

class MultitaskRecordViewDataMapper @Inject constructor(
    private val recordViewDataMapper: RecordViewDataMapper,
) {

    fun map(
        multitaskRecord: MultitaskRecord,
        recordTypes: Map<Long, RecordType>,
        recordTags: List<RecordTag>,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): MultitaskRecordViewData {
        val ids = multitaskRecord.records.map(Record::id)
        val records = multitaskRecord.records.mapNotNull { record ->
            recordViewDataMapper.map(
                record = record,
                recordType = recordTypes[record.typeId] ?: return@mapNotNull null,
                recordTags = recordTags.filter { it.id in record.tagIds },
                timeStarted = record.timeStarted,
                timeEnded = record.timeEnded,
                isDarkTheme = isDarkTheme,
                useMilitaryTime = useMilitaryTime,
                useProportionalMinutes = useProportionalMinutes,
                showSeconds = showSeconds,
            )
        }

        return MultitaskRecordViewData(
            id = ids.hashCode().toLong(),
            data = MultitaskRecordView.ViewData(
                timeStarted = records.firstOrNull()?.timeStarted.orEmpty(),
                timeFinished = records.firstOrNull()?.timeFinished.orEmpty(),
                duration = records.firstOrNull()?.duration.orEmpty(),
                items = records.map {
                    MultitaskRecordView.ItemViewData(
                        name = it.name,
                        tagName = it.tagName,
                        iconId = it.iconId,
                        color = it.color,
                        comment = it.comment,
                    )
                },
            )
        )
    }
}