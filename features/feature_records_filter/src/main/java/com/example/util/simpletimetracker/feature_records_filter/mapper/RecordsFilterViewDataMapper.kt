package com.example.util.simpletimetracker.feature_records_filter.mapper

import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.RecordFilterViewData
import com.example.util.simpletimetracker.feature_records_filter.R
import javax.inject.Inject

class RecordsFilterViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordViewDataMapper: RecordViewDataMapper,
    private val timeMapper: TimeMapper,
) {

    fun map(
        record: Record,
        recordType: RecordType,
        recordTags: List<RecordTag>,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): ViewHolderType {
        val (timeStarted, timeEnded) = record.timeStarted to record.timeEnded

        return recordViewDataMapper.map(
            record = record,
            recordType = recordType,
            recordTags = recordTags,
            timeStarted = timeStarted,
            timeEnded = timeEnded,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        )
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.records_empty.let(resourceRepo::getString)
        )
    }

    fun mapInactiveFilterName(
        filter: RecordFilterViewData.Type,
    ): String {
        return when (filter) {
            RecordFilterViewData.Type.ACTIVITY -> R.string.records_filter_activity
            RecordFilterViewData.Type.COMMENT -> R.string.records_filter_comment
            RecordFilterViewData.Type.DATE -> R.string.records_filter_date
            RecordFilterViewData.Type.SELECTED_TAGS -> R.string.records_filter_select_tags
            RecordFilterViewData.Type.FILTERED_TAGS -> R.string.records_filter_filter_tags
        }.let(resourceRepo::getString)
    }

    fun mapActiveFilterName(filter: RecordsFilter): String {
        val filterName = filter::class.java
            .let(::mapToViewData)
            ?.let(::mapInactiveFilterName)
            .orEmpty() + " "

        val filterValue = when (filter) {
            is RecordsFilter.Activity -> {
                "${filter.typeIds.size}"
            }
            is RecordsFilter.Comment -> {
                filter.comment.let {
                    if (it.length > 10) it.take(10) + "..." else it
                }
            }
            is RecordsFilter.Date -> {
                val startedDate = timeMapper.formatDateYear(filter.range.timeStarted)
                val endedDate = timeMapper.formatDateYear(filter.range.timeEnded)
                "$startedDate - $endedDate"
            }
            is RecordsFilter.SelectedTags -> {
                "${filter.tags.size}"
            }
            is RecordsFilter.FilteredTags -> {
                "${filter.tags.size}"
            }
            else -> ""
        }

        return "$filterName($filterValue)"
    }

    fun mapToClass(type: RecordFilterViewData.Type): Class<out RecordsFilter> {
        return when (type) {
            RecordFilterViewData.Type.ACTIVITY -> RecordsFilter.Activity::class.java
            RecordFilterViewData.Type.COMMENT -> RecordsFilter.Comment::class.java
            RecordFilterViewData.Type.DATE -> RecordsFilter.Date::class.java
            RecordFilterViewData.Type.SELECTED_TAGS -> RecordsFilter.SelectedTags::class.java
            RecordFilterViewData.Type.FILTERED_TAGS -> RecordsFilter.FilteredTags::class.java
        }
    }

    private fun mapToViewData(clazz: Class<out RecordsFilter>): RecordFilterViewData.Type? {
        return when (clazz) {
            RecordsFilter.Activity::class.java -> RecordFilterViewData.Type.ACTIVITY
            RecordsFilter.Comment::class.java -> RecordFilterViewData.Type.COMMENT
            RecordsFilter.Date::class.java -> RecordFilterViewData.Type.DATE
            RecordsFilter.SelectedTags::class.java -> RecordFilterViewData.Type.SELECTED_TAGS
            RecordsFilter.FilteredTags::class.java -> RecordFilterViewData.Type.FILTERED_TAGS
            else -> null
        }
    }
}