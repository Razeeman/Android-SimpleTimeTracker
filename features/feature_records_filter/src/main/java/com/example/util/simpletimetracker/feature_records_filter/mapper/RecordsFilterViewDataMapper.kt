package com.example.util.simpletimetracker.feature_records_filter.mapper

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.CommonViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RangeTitleMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.record.extension.getCategoryItems
import com.example.util.simpletimetracker.domain.record.extension.getCommentItems
import com.example.util.simpletimetracker.domain.record.extension.getComments
import com.example.util.simpletimetracker.domain.record.extension.getDuplicationItems
import com.example.util.simpletimetracker.domain.record.extension.getFilteredCategoryItems
import com.example.util.simpletimetracker.domain.record.extension.getFilteredTags
import com.example.util.simpletimetracker.domain.record.extension.getFilteredTypeIds
import com.example.util.simpletimetracker.domain.record.extension.getSelectedTags
import com.example.util.simpletimetracker.domain.record.extension.getTypeIds
import com.example.util.simpletimetracker.domain.record.extension.hasAnyComment
import com.example.util.simpletimetracker.domain.record.extension.hasNoComment
import com.example.util.simpletimetracker.domain.record.extension.hasSameActivity
import com.example.util.simpletimetracker.domain.record.extension.hasSameTimes
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.buttonDouble.DoubleButtonsViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData
import com.example.util.simpletimetracker.feature_records_filter.R
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterActivitiesType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterCommentType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterDateType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterDuplicationsType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterSelectionType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterType
import com.example.util.simpletimetracker.feature_records_filter.viewData.RecordsFilterSelectionButtonType
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import javax.inject.Inject

class RecordsFilterViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val colorMapper: ColorMapper,
    private val rangeTitleMapper: RangeTitleMapper,
    private val commonViewDataMapper: CommonViewDataMapper,
) {

    fun mapInitialFilter(
        filters: List<RecordsFilter>,
    ): RecordFilterType? {
        // Date is selected last.
        return filters.minByOrNull { it is RecordsFilter.Date }
            ?.let(::mapToViewData)
    }

    fun mapRecordsCount(
        extra: RecordsFilterParams,
        count: Int,
        filterSelected: Boolean,
    ): String {
        return if (!filterSelected) {
            extra.title
        } else {
            commonViewDataMapper.mapRecordsCountHint(count)
        }
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
            RecordFilterType.Tags -> R.string.record_tag_hint
            RecordFilterType.ManuallyFiltered -> R.string.records_filter_manually_filtered
            RecordFilterType.DaysOfWeek -> R.string.range_day
            RecordFilterType.TimeOfDay -> R.string.date_time_dialog_time
            RecordFilterType.Duration -> R.string.records_all_sort_duration
            RecordFilterType.Duplications -> R.string.records_filter_duplications
            RecordFilterType.Favourite -> R.string.change_record_favourite_comments_hint
        }.let(resourceRepo::getString)
    }

    fun mapActiveFilterName(
        filter: RecordsFilter,
        useMilitaryTime: Boolean,
        startOfDayShift: Long,
        firstDayOfWeek: DayOfWeek,
    ): String {
        val filterName = filter
            .let(::mapToViewData)
            .let(::mapInactiveFilterName)

        val filterValue = mapFilterValue(
            filter = filter,
            useMilitaryTime = useMilitaryTime,
            startOfDayShift = startOfDayShift,
            firstDayOfWeek = firstDayOfWeek,
        )

        return mapFilterName(filterName = filterName, filterValue = filterValue)
    }

    fun mapFilterValue(
        filter: RecordsFilter,
        useMilitaryTime: Boolean,
        startOfDayShift: Long,
        firstDayOfWeek: DayOfWeek,
    ): String {
        return when (filter) {
            is RecordsFilter.Untracked,
            is RecordsFilter.Multitask,
            is RecordsFilter.Duplications,
            -> {
                ""
            }
            is RecordsFilter.Activity -> {
                "${filter.selected.size + filter.filtered.size}"
            }
            is RecordsFilter.Category -> {
                "${filter.selected.size + filter.filtered.size}"
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
                rangeTitleMapper.mapToTitle(
                    rangeLength = filter.range,
                    position = filter.position,
                    startOfDayShift = startOfDayShift,
                    firstDayOfWeek = firstDayOfWeek,
                )
            }
            is RecordsFilter.Tags -> {
                "${filter.selected.size + filter.filtered.size}"
            }
            is RecordsFilter.ManuallyFiltered -> {
                "${filter.items.size}"
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
            type = type,
            name = name,
            color = if (enabled) {
                colorMapper.toActiveColor(isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            },
            selected = enabled,
            isBtnVisible = false,
        )
    }

    fun mapDuplicationsFilter(
        type: RecordFilterDuplicationsType,
        filters: List<RecordsFilter>,
        isDarkTheme: Boolean,
    ): ViewHolderType {
        val name: String
        val enabled: Boolean

        when (type) {
            RecordFilterDuplicationsType.SameActivity -> {
                enabled = filters.getDuplicationItems().hasSameActivity()
                name = resourceRepo.getString(R.string.records_filter_duplications_same_activity)
            }
            RecordFilterDuplicationsType.SameTimes -> {
                enabled = filters.getDuplicationItems().hasSameTimes()
                name = resourceRepo.getString(R.string.records_filter_duplications_same_times)
            }
        }

        return FilterViewData(
            id = type.hashCode().toLong(),
            type = type,
            name = name,
            color = if (enabled) {
                colorMapper.toActiveColor(isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            },
            selected = enabled,
            isBtnVisible = false,
        )
    }

    fun mapActivitiesSelectionTypeFilter(
        type: RecordFilterActivitiesType,
        filters: List<RecordsFilter>,
        currentType: RecordFilterActivitiesType,
        isDarkTheme: Boolean,
    ): ViewHolderType {
        val enabled = type == currentType
        val name: String
        val tagCount: Int

        when (type) {
            RecordFilterActivitiesType.Activities -> {
                name = resourceRepo.getString(R.string.activity_hint)
                tagCount = filters.getTypeIds().size + filters.getFilteredTypeIds().size
            }
            RecordFilterActivitiesType.Categories -> {
                name = resourceRepo.getString(R.string.category_hint)
                tagCount = filters.getCategoryItems().size + filters.getFilteredCategoryItems().size
            }
        }

        return FilterViewData(
            id = type.hashCode().toLong(),
            type = type,
            name = mapFilterName(
                filterName = name,
                filterValue = tagCount.takeIf { it > 0 }?.toString().orEmpty(),
            ),
            color = if (enabled) {
                colorMapper.toActiveColor(isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            },
            selected = enabled,
            isBtnVisible = false,
        )
    }

    fun mapTagSelectionTypeFilter(
        type: RecordFilterSelectionType,
        filters: List<RecordsFilter>,
        currentType: RecordFilterSelectionType,
        isDarkTheme: Boolean,
    ): ViewHolderType {
        val enabled = type == currentType
        val name: String
        val tagCount: Int

        when (type) {
            RecordFilterSelectionType.Select -> {
                name = resourceRepo.getString(R.string.records_filter_select)
                tagCount = filters.getSelectedTags().size
            }
            RecordFilterSelectionType.Filter -> {
                name = resourceRepo.getString(R.string.records_filter_exclude)
                tagCount = filters.getFilteredTags().size
            }
        }

        return FilterViewData(
            id = type.hashCode().toLong(),
            type = type,
            name = mapFilterName(
                filterName = name,
                filterValue = tagCount.takeIf { it > 0 }?.toString().orEmpty(),
            ),
            color = if (enabled) {
                colorMapper.toActiveColor(isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            },
            selected = enabled,
            isBtnVisible = false,
        )
    }

    fun mapDateRangeFilter(
        rangeLength: RangeLength,
        filter: RecordsFilter.Date?,
        isDarkTheme: Boolean,
        startOfDayShift: Long,
        firstDayOfWeek: DayOfWeek,
        index: Int,
    ): ViewHolderType {
        val selected = filter?.range == rangeLength &&
            filter.position == 0

        return FilterViewData(
            id = index.toLong(),
            type = RecordFilterDateType(rangeLength),
            name = rangeTitleMapper.mapToTitle(
                rangeLength = rangeLength,
                position = 0,
                startOfDayShift = startOfDayShift,
                firstDayOfWeek = firstDayOfWeek,
                useShortCustomRange = true,
            ),
            color = if (selected) {
                colorMapper.toActiveColor(isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            },
            selected = selected,
            isBtnVisible = false,
        )
    }

    fun mapToClass(type: RecordFilterType): Class<out RecordsFilter>? {
        return when (type) {
            RecordFilterType.Untracked -> RecordsFilter.Untracked::class.java
            RecordFilterType.Multitask -> RecordsFilter.Multitask::class.java
            RecordFilterType.Activity -> RecordsFilter.Activity::class.java
            RecordFilterType.Category -> RecordsFilter.Category::class.java
            RecordFilterType.Comment -> RecordsFilter.Comment::class.java
            RecordFilterType.Date -> RecordsFilter.Date::class.java
            RecordFilterType.Tags -> RecordsFilter.Tags::class.java
            RecordFilterType.ManuallyFiltered -> RecordsFilter.ManuallyFiltered::class.java
            RecordFilterType.DaysOfWeek -> RecordsFilter.DaysOfWeek::class.java
            RecordFilterType.TimeOfDay -> RecordsFilter.TimeOfDay::class.java
            RecordFilterType.Duration -> RecordsFilter.Duration::class.java
            RecordFilterType.Duplications -> RecordsFilter.Duplications::class.java
            RecordFilterType.Favourite -> null
        }
    }

    fun mapToSelectionButtons(
        type: RecordsFilterSelectionButtonType.Type,
    ): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        result += DoubleButtonsViewData(
            DoubleButtonsViewData.Button(
                type = RecordsFilterSelectionButtonType(
                    type = type,
                    subtype = RecordsFilterSelectionButtonType.Subtype.SelectAll,
                ),
                name = resourceRepo.getString(R.string.select_all),
            ),
            DoubleButtonsViewData.Button(
                type = RecordsFilterSelectionButtonType(
                    type = type,
                    subtype = RecordsFilterSelectionButtonType.Subtype.SelectNone,
                ),
                name = resourceRepo.getString(R.string.select_nothing),
            ),
        )

        return result
    }

    @ColorInt
    fun mapTextFieldColor(
        isSelected: Boolean,
        isDarkTheme: Boolean,
    ): Int {
        return if (isSelected) {
            R.attr.appTextPrimaryColor
        } else {
            R.attr.appTextHintColor
        }.let {
            resourceRepo.getThemedAttr(it, isDarkTheme)
        }
    }

    fun mapSortOrder(type: RecordFilterType): Int {
        return listOf(
            RecordFilterType.Activity,
            RecordFilterType.Category,
            RecordFilterType.Tags,
            RecordFilterType.Untracked,
            RecordFilterType.Comment,
            RecordFilterType.Date,
            RecordFilterType.DaysOfWeek,
            RecordFilterType.TimeOfDay,
            RecordFilterType.Duration,
            RecordFilterType.Multitask,
            RecordFilterType.Duplications,
            RecordFilterType.ManuallyFiltered,
            RecordFilterType.Favourite,
        ).indexOf(type)
    }

    private fun mapFilterName(
        filterName: String,
        filterValue: String,
    ): String {
        return if (filterValue.isNotEmpty()) "$filterName ($filterValue)" else filterName
    }

    fun mapToViewData(filter: RecordsFilter): RecordFilterType {
        return when (filter) {
            is RecordsFilter.Untracked -> RecordFilterType.Untracked
            is RecordsFilter.Multitask -> RecordFilterType.Multitask
            is RecordsFilter.Activity -> RecordFilterType.Activity
            is RecordsFilter.Category -> RecordFilterType.Category
            is RecordsFilter.Comment -> RecordFilterType.Comment
            is RecordsFilter.Date -> RecordFilterType.Date
            is RecordsFilter.Tags -> RecordFilterType.Tags
            is RecordsFilter.ManuallyFiltered -> RecordFilterType.ManuallyFiltered
            is RecordsFilter.DaysOfWeek -> RecordFilterType.DaysOfWeek
            is RecordsFilter.TimeOfDay -> RecordFilterType.TimeOfDay
            is RecordsFilter.Duration -> RecordFilterType.Duration
            is RecordsFilter.Duplications -> RecordFilterType.Duplications
        }
    }
}