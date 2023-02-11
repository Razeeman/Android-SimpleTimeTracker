package com.example.util.simpletimetracker.feature_records.customView

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class RecordsCalendarViewData(
    val currentTime: Long?,
    val startOfDayShift: Long,
    val points: List<Point>,
    val reverseOrder: Boolean,
) {

    data class Point(
        val start: Long,
        val end: Long,
        val data: Data,
    ) {

        sealed interface Data {
            val value: ViewHolderType
            val color: Int
            val duration: String
            val name: String
            val tagName: String
            val iconId: RecordTypeIcon
            val comment: String

            data class RecordData(override val value: RecordViewData) : Data {
                override val color = value.color
                override val duration = value.duration
                override val name = value.name
                override val tagName = value.tagName
                override val iconId = value.iconId
                override val comment = value.comment
            }

            data class RunningRecordData(override val value: RunningRecordViewData) : Data {
                override val color = value.color
                override val duration = value.timer
                override val name = value.name
                override val tagName = value.tagName
                override val iconId = value.iconId
                override val comment = value.comment
            }
        }
    }
}