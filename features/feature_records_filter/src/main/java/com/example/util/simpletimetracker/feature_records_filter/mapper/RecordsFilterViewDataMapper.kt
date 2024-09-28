package com.example.util.simpletimetracker.feature_records_filter.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.getCommentItems
import com.example.util.simpletimetracker.domain.extension.getComments
import com.example.util.simpletimetracker.domain.extension.hasAnyComment
import com.example.util.simpletimetracker.domain.extension.hasNoComment
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.EmptySpaceViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData
import com.example.util.simpletimetracker.feature_base_adapter.selectionButton.SelectionButtonViewData
import com.example.util.simpletimetracker.feature_records_filter.R
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterCommentType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterType
import com.example.util.simpletimetracker.feature_records_filter.viewData.RecordsFilterSelectionButtonType
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import javax.inject.Inject

class RecordsFilterViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val colorMapper: ColorMapper,
) {

    fun mapInitialFilter(
        extra: RecordsFilterParams,
        filters: List<RecordsFilter>,
    ): RecordFilterType? {
        return filters
            .firstOrNull {
                when (it) {
                    is RecordsFilter.Date -> extra.dateSelectionAvailable
                    is RecordsFilter.Untracked -> extra.untrackedSelectionAvailable
                    is RecordsFilter.Multitask -> extra.multitaskSelectionAvailable
                    else -> true
                }
            }
            ?.let { mapToViewData(it::class.java) }
    }

    fun mapRecordsCount(
        extra: RecordsFilterParams,
        count: Int,
        filterSelected: Boolean,
    ): String {
        if (!filterSelected) return extra.title

        val selected = resourceRepo.getString(R.string.something_selected)
        val recordsString: String = resourceRepo.getQuantityString(
            R.plurals.statistics_detail_times_tracked,
            count,
        ).lowercase()

        return "$selected $count $recordsString"
    }

    fun mapInactiveFilterName(
        filter: RecordFilterType,
    ): String {
        return when (filter) {
            RecordFilterType.Untracked -> R.string.untracked_time_name
            RecordFilterType.Multitask -> R.string.multitask_time_name
            RecordFilterType.Activity -> R.string.activity_hint
            RecordFilterType.Category -> R.string.category_hint
            RecordFilterType.Comment -> R.string.change_record_comment_field
            RecordFilterType.Date -> R.string.date_time_dialog_date
            RecordFilterType.SelectedTags -> R.string.records_filter_select_tags
            RecordFilterType.FilteredTags -> R.string.records_filter_filter_tags
            RecordFilterType.ManuallyFiltered -> R.string.records_filter_manually_filtered
            RecordFilterType.DaysOfWeek -> R.string.range_day
            RecordFilterType.TimeOfDay -> R.string.date_time_dialog_time
            RecordFilterType.Duration -> R.string.records_all_sort_duration
        }.let(resourceRepo::getString)
    }

    fun mapActiveFilterName(
        filter: RecordsFilter,
        useMilitaryTime: Boolean,
    ): String {
        val filterName = filter::class.java
            .let(::mapToViewData)
            ?.let(::mapInactiveFilterName)
            .orEmpty()

        val filterValue = when (filter) {
            is RecordsFilter.Untracked -> {
                ""
            }
            is RecordsFilter.Multitask -> {
                ""
            }
            is RecordsFilter.Activity -> {
                "${filter.typeIds.size}"
            }
            is RecordsFilter.Category -> {
                "${filter.items.size}"
            }
            is RecordsFilter.Comment -> {
                val items = filter.items
                when {
                    items.hasNoComment() -> {
                        resourceRepo.getString(R.string.records_filter_no_comment)
                    }
                    items.hasAnyComment() -> {
                        resourceRepo.getString(R.string.records_filter_any_comment)
                    }
                    else -> {
                        items.getComments()
                            .firstOrNull()
                            .orEmpty()
                            .replace("\n", " ")
                            .let {
                                if (it.length > 10) it.take(10) + "..." else it
                            }
                    }
                }
            }
            is RecordsFilter.Date -> {
                val startedDate = timeMapper.formatDateTime(
                    time = filter.range.timeStarted,
                    useMilitaryTime = useMilitaryTime,
                    showSeconds = false,
                )
                val endedDate = timeMapper.formatDateTime(
                    time = filter.range.timeEnded,
                    useMilitaryTime = useMilitaryTime,
                    showSeconds = false,
                )
                "$startedDate - $endedDate"
            }
            is RecordsFilter.SelectedTags -> {
                "${filter.items.size}"
            }
            is RecordsFilter.FilteredTags -> {
                "${filter.items.size}"
            }
            is RecordsFilter.ManuallyFiltered -> {
                "${filter.recordIds.size}"
            }
            is RecordsFilter.DaysOfWeek -> {
                "${filter.items.size}"
            }
            is RecordsFilter.TimeOfDay -> {
                // TODO add to mapper
                val startOfDay = timeMapper.getStartOfDayTimeStamp()
                val start = timeMapper.formatTime(
                    time = filter.range.timeStarted + startOfDay,
                    useMilitaryTime = useMilitaryTime,
                    showSeconds = false,
                )
                val end = timeMapper.formatTime(
                    time = filter.range.timeEnded + startOfDay,
                    useMilitaryTime = useMilitaryTime,
                    showSeconds = false,
                )
                "$start - $end"
            }
            is RecordsFilter.Duration -> {
                val start = timeMapper.formatDuration(filter.range.timeStarted / 1000)
                val end = timeMapper.formatDuration(interval = filter.range.timeEnded / 1000)
                "$start - $end"
            }
        }

        return if (filterValue.isNotEmpty()) "$filterName ($filterValue)" else filterName
    }

    fun mapCommentFilter(
        type: RecordFilterCommentType,
        filters: List<RecordsFilter>,
        isDarkTheme: Boolean,
    ): ViewHolderType {
        val name: String
        val enabled: Boolean

        when (type) {
            RecordFilterCommentType.NoComment -> {
                enabled = filters.getCommentItems().hasNoComment()
                name = resourceRepo.getString(R.string.records_filter_no_comment)
            }
            RecordFilterCommentType.AnyComment -> {
                enabled = filters.getCommentItems().hasAnyComment()
                name = resourceRepo.getString(R.string.records_filter_any_comment)
            }
        }

        return FilterViewData(
            id = type.hashCode().toLong(),
            type = RecordFilterType.Comment,
            name = name,
            color = if (enabled) {
                colorMapper.toActiveColor(isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            },
            selected = enabled,
            removeBtnVisible = false,
        )
    }

    fun mapToClass(type: RecordFilterType): Class<out RecordsFilter> {
        return when (type) {
            RecordFilterType.Untracked -> RecordsFilter.Untracked::class.java
            RecordFilterType.Multitask -> RecordsFilter.Multitask::class.java
            RecordFilterType.Activity -> RecordsFilter.Activity::class.java
            RecordFilterType.Category -> RecordsFilter.Category::class.java
            RecordFilterType.Comment -> RecordsFilter.Comment::class.java
            RecordFilterType.Date -> RecordsFilter.Date::class.java
            RecordFilterType.SelectedTags -> RecordsFilter.SelectedTags::class.java
            RecordFilterType.FilteredTags -> RecordsFilter.FilteredTags::class.java
            RecordFilterType.ManuallyFiltered -> RecordsFilter.ManuallyFiltered::class.java
            RecordFilterType.DaysOfWeek -> RecordsFilter.DaysOfWeek::class.java
            RecordFilterType.TimeOfDay -> RecordsFilter.TimeOfDay::class.java
            RecordFilterType.Duration -> RecordsFilter.Duration::class.java
        }
    }

    fun mapToSelectionButtons(
        type: RecordsFilterSelectionButtonType.Type,
        isDarkTheme: Boolean,
    ): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        result += SelectionButtonViewData(
            type = RecordsFilterSelectionButtonType(
                type = type,
                subtype = RecordsFilterSelectionButtonType.Subtype.SelectAll,
            ),
            name = resourceRepo.getString(R.string.select_all),
            color = colorMapper.toInactiveColor(isDarkTheme),
        )
        result += SelectionButtonViewData(
            type = RecordsFilterSelectionButtonType(
                type = type,
                subtype = RecordsFilterSelectionButtonType.Subtype.SelectNone,
            ),
            name = resourceRepo.getString(R.string.select_nothing),
            color = colorMapper.toInactiveColor(isDarkTheme),
        )
        result += EmptySpaceViewData(
            id = 0,
            wrapBefore = true,
        )

        return result
    }

    private fun mapToViewData(clazz: Class<out RecordsFilter>): RecordFilterType? {
        return when (clazz) {
            RecordsFilter.Untracked::class.java -> RecordFilterType.Untracked
            RecordsFilter.Multitask::class.java -> RecordFilterType.Multitask
            RecordsFilter.Activity::class.java -> RecordFilterType.Activity
            RecordsFilter.Category::class.java -> RecordFilterType.Category
            RecordsFilter.Comment::class.java -> RecordFilterType.Comment
            RecordsFilter.Date::class.java -> RecordFilterType.Date
            RecordsFilter.SelectedTags::class.java -> RecordFilterType.SelectedTags
            RecordsFilter.FilteredTags::class.java -> RecordFilterType.FilteredTags
            RecordsFilter.ManuallyFiltered::class.java -> RecordFilterType.ManuallyFiltered
            RecordsFilter.DaysOfWeek::class.java -> RecordFilterType.DaysOfWeek
            RecordsFilter.TimeOfDay::class.java -> RecordFilterType.TimeOfDay
            RecordsFilter.Duration::class.java -> RecordFilterType.Duration
            else -> null
        }
    }
}