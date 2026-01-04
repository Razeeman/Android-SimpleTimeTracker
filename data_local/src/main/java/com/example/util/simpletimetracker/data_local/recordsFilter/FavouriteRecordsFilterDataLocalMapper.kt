package com.example.util.simpletimetracker.data_local.recordsFilter

import com.example.util.simpletimetracker.data_local.daysOfWeek.DaysOfWeekDataLocalMapper
import com.example.util.simpletimetracker.domain.record.model.FavouriteRecordsFilter
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import javax.inject.Inject

class FavouriteRecordsFilterDataLocalMapper @Inject constructor(
    private val daysOfWeekDataLocalMapper: DaysOfWeekDataLocalMapper,
) {

    fun map(dbo: FavouriteRecordsFilterDBO): FavouriteRecordsFilter? {
        return FavouriteRecordsFilter(
            id = dbo.main.id,
            filter = dbo.filters
                .mapNotNull(::map)
                .takeIf { it.isNotEmpty() } ?: return null,
        )
    }

    private fun map(dbo: FavouriteRecordsFilterDBO.FilterDBO): RecordsFilter? {
        val range = dbo.range?.let {
            Range(
                timeStarted = it.rangeTimeStarted,
                timeEnded = it.rangeTimeEnded,
            )
        }
        val rangeLength = dbo.rangeLength?.let { data ->
            when (data.rangeType) {
                0L -> RangeLength.Day
                1L -> RangeLength.Week
                2L -> RangeLength.Month
                3L -> RangeLength.Year
                4L -> RangeLength.All
                5L -> data.customRange?.let {
                    val range = Range(timeStarted = it.rangeTimeStarted, timeEnded = it.rangeTimeEnded)
                    RangeLength.Custom(range)
                }
                6L -> data.lastDays?.let { data ->
                    RangeLength.Last(data.toInt())
                }
                else -> null
            }
        }
        val rangeLengthPosition = dbo.rangeLength?.position?.toInt()
        val daysOfWeek = dbo.daysOfWeek
            ?.let(daysOfWeekDataLocalMapper::mapDaysOfWeek)
            ?.toList()
        val commentItems = dbo.commentItemsText
            ?.let { listOf(RecordsFilter.CommentItem.Comment(it)) }
            ?: dbo.commentItemsIds.orEmpty().split(SEPARATOR).mapNotNull {
                when (it) {
                    COMMENT_ITEM_NO_COMMENT -> RecordsFilter.CommentItem.NoComment
                    COMMENT_ITEM_ANY_COMMENT -> RecordsFilter.CommentItem.AnyComment
                    else -> null
                }
            }.takeIf { it.isNotEmpty() }
        val duplicationItems = dbo.duplicationItemsIds.orEmpty().split(SEPARATOR).mapNotNull {
            when (it) {
                DUPLICATION_ITEM_SAME_ACTIVITY -> RecordsFilter.DuplicationsItem.SameActivity
                DUPLICATION_ITEM_SAME_TIMES -> RecordsFilter.DuplicationsItem.SameTimes
                else -> null
            }
        }.takeIf { it.isNotEmpty() }
        val manuallyFilteredItems = dbo.manuallyFilteredItemsIds.orEmpty().split(GROUP_SEPARATOR).mapNotNull {
            when {
                it.startsWith(MANUALLY_ITEM_TRACKED) -> {
                    val id = it.removePrefix(MANUALLY_ITEM_TRACKED)
                        .toLongOrNull() ?: return@mapNotNull null
                    RecordsFilter.ManuallyFilteredItem.Tracked(id)
                }
                it.startsWith(MANUALLY_ITEM_MULTITASK) -> {
                    val ids = it.removePrefix(MANUALLY_ITEM_MULTITASK)
                        .split(SEPARATOR)
                        .mapNotNull(String::toLongOrNull)
                        .ifEmpty { return@mapNotNull null }
                    RecordsFilter.ManuallyFilteredItem.Multitask(ids)
                }
                it.startsWith(MANUALLY_ITEM_UNTRACKED) -> {
                    val rangeTimes = it.removePrefix(MANUALLY_ITEM_UNTRACKED)
                        .split(SEPARATOR)
                        .mapNotNull(String::toLongOrNull)
                    val range = Range(
                        timeStarted = rangeTimes.getOrNull(0) ?: return@mapNotNull null,
                        timeEnded = rangeTimes.getOrNull(1) ?: return@mapNotNull null,
                    )
                    RecordsFilter.ManuallyFilteredItem.Untracked(range)
                }
                else -> null
            }
        }.takeIf { it.isNotEmpty() }
        val commonItemsParts = dbo.commonItemsIds.orEmpty().split(GROUP_SEPARATOR)
        val commonItems = commonItemsParts.getOrNull(0)
            ?.split(SEPARATOR)?.map { true to it }.orEmpty() +
            commonItemsParts.getOrNull(1)
                ?.split(SEPARATOR)?.map { false to it }.orEmpty()

        val filter = when (dbo.type) {
            0L -> RecordsFilter.Untracked
            1L -> RecordsFilter.Multitask
            2L -> {
                val itemsMapped = commonItems.map { (selected, item) ->
                    selected to item.toLongOrNull()
                }
                val selectedItems = itemsMapped.filter { it.first }.mapNotNull { it.second }
                val filteredItems = itemsMapped.filter { !it.first }.mapNotNull { it.second }

                if (selectedItems.isNotEmpty() || filteredItems.isNotEmpty()) {
                    RecordsFilter.Activity(selected = selectedItems, filtered = filteredItems)
                } else {
                    return null
                }
            }
            3L -> {
                val itemsMapped = commonItems.mapNotNull { (selected, item) ->
                    selected to when (item) {
                        COMMON_ITEM_UNCATEGORIZED -> RecordsFilter.CategoryItem.Uncategorized
                        else -> RecordsFilter.CategoryItem.Categorized(
                            item.toLongOrNull() ?: return@mapNotNull null,
                        )
                    }
                }
                val selectedItems = itemsMapped.filter { it.first }.map { it.second }
                val filteredItems = itemsMapped.filter { !it.first }.map { it.second }

                if (selectedItems.isNotEmpty() || filteredItems.isNotEmpty()) {
                    RecordsFilter.Category(selected = selectedItems, filtered = filteredItems)
                } else {
                    return null
                }
            }
            4L -> {
                val itemsMapped = commonItems.mapNotNull { (selected, item) ->
                    selected to when (item) {
                        COMMON_ITEM_UNTAGGED -> RecordsFilter.TagItem.Untagged
                        else -> RecordsFilter.TagItem.Tagged(
                            item.toLongOrNull() ?: return@mapNotNull null,
                        )
                    }
                }
                val selectedItems = itemsMapped.filter { it.first }.map { it.second }
                val filteredItems = itemsMapped.filter { !it.first }.map { it.second }

                if (selectedItems.isNotEmpty() || filteredItems.isNotEmpty()) {
                    RecordsFilter.Tags(selected = selectedItems, filtered = filteredItems)
                } else {
                    return null
                }
            }
            5L -> RecordsFilter.Comment(commentItems ?: return null)
            6L -> RecordsFilter.Date(rangeLength ?: return null, rangeLengthPosition ?: return null)
            7L -> RecordsFilter.ManuallyFiltered(manuallyFilteredItems ?: return null)
            8L -> RecordsFilter.DaysOfWeek(daysOfWeek ?: return null)
            9L -> RecordsFilter.TimeOfDay(range ?: return null)
            10L -> RecordsFilter.Duration(range ?: return null)
            11L -> RecordsFilter.Duplications(duplicationItems ?: return null)
            else -> return null
        }

        return filter
    }

    fun map(domain: FavouriteRecordsFilter): FavouriteRecordsFilterDBO {
        return FavouriteRecordsFilterDBO(
            main = FavouriteRecordsFilterDBO.MainDBO(
                id = domain.id,
            ),
            filters = domain.filter.map(::map),
        )
    }

    private fun map(domain: RecordsFilter): FavouriteRecordsFilterDBO.FilterDBO {
        val range = when (domain) {
            is RecordsFilter.TimeOfDay -> domain.range
            is RecordsFilter.Duration -> domain.range
            else -> null
        }?.let {
            FavouriteRecordsFilterDBO.RangeDBO(
                rangeTimeStarted = it.timeStarted,
                rangeTimeEnded = it.timeEnded,
            )
        }
        val rangeLength = (domain as? RecordsFilter.Date)?.let { data ->
            FavouriteRecordsFilterDBO.RangeLengthDBO(
                rangeType = when (data.range) {
                    is RangeLength.Day -> 0L
                    is RangeLength.Week -> 1L
                    is RangeLength.Month -> 2L
                    is RangeLength.Year -> 3L
                    is RangeLength.All -> 4L
                    is RangeLength.Custom -> 5L
                    is RangeLength.Last -> 6L
                },
                customRange = (data.range as? RangeLength.Custom)?.range?.let {
                    FavouriteRecordsFilterDBO.RangeDBO(
                        rangeTimeStarted = it.timeStarted,
                        rangeTimeEnded = it.timeEnded,
                    )
                },
                lastDays = (data.range as? RangeLength.Last)?.days?.toLong(),
                position = data.position.toLong(),
            )
        }
        val daysOfWeek = (domain as? RecordsFilter.DaysOfWeek)?.items?.toSet()
        val commonItemsIds = when (domain) {
            is RecordsFilter.Activity -> {
                val items = domain.selected.map { true to it } +
                    domain.filtered.map { false to it }
                val itemsMapped = items.map { (selected, item) ->
                    selected to item.toString()
                }
                String.format(
                    "%s$GROUP_SEPARATOR%s",
                    itemsMapped.filter { it.first }.joinToString(separator = SEPARATOR) { it.second },
                    itemsMapped.filter { !it.first }.joinToString(separator = SEPARATOR) { it.second },
                )
            }
            is RecordsFilter.Category -> {
                val items = domain.selected.map { true to it } +
                    domain.filtered.map { false to it }
                val itemsMapped = items.map { (selected, item) ->
                    selected to when (item) {
                        is RecordsFilter.CategoryItem.Categorized -> item.categoryId.toString()
                        is RecordsFilter.CategoryItem.Uncategorized -> COMMON_ITEM_UNCATEGORIZED
                    }
                }
                String.format(
                    "%s$GROUP_SEPARATOR%s",
                    itemsMapped.filter { it.first }.joinToString(separator = SEPARATOR) { it.second },
                    itemsMapped.filter { !it.first }.joinToString(separator = SEPARATOR) { it.second },
                )
            }
            is RecordsFilter.Tags -> {
                val items = domain.selected.map { true to it } +
                    domain.filtered.map { false to it }
                val itemsMapped = items.map { (selected, item) ->
                    selected to when (item) {
                        is RecordsFilter.TagItem.Tagged -> item.tagId.toString()
                        is RecordsFilter.TagItem.Untagged -> COMMON_ITEM_UNTAGGED
                    }
                }
                String.format(
                    "%s$GROUP_SEPARATOR%s",
                    itemsMapped.filter { it.first }.joinToString(separator = SEPARATOR) { it.second },
                    itemsMapped.filter { !it.first }.joinToString(separator = SEPARATOR) { it.second },
                )
            }
            else -> null
        }
        val commentItemsIds = (domain as? RecordsFilter.Comment)?.items?.mapNotNull {
            when (it) {
                is RecordsFilter.CommentItem.NoComment -> COMMENT_ITEM_NO_COMMENT
                is RecordsFilter.CommentItem.AnyComment -> COMMENT_ITEM_ANY_COMMENT
                is RecordsFilter.CommentItem.Comment -> null // Stored separately.
            }
        }?.joinToString(separator = SEPARATOR)
        // Currently only one comment filter is supported.
        val commentItemsText = (domain as? RecordsFilter.Comment)?.items
            ?.filterIsInstance<RecordsFilter.CommentItem.Comment>()
            ?.firstOrNull()?.text
        val duplicationItemsIds = (domain as? RecordsFilter.Duplications)?.items
            ?.joinToString(separator = SEPARATOR) {
                when (it) {
                    is RecordsFilter.DuplicationsItem.SameActivity -> DUPLICATION_ITEM_SAME_ACTIVITY
                    is RecordsFilter.DuplicationsItem.SameTimes -> DUPLICATION_ITEM_SAME_TIMES
                }
            }
        val manuallyFilteredItemsIds = (domain as? RecordsFilter.ManuallyFiltered)?.items?.mapNotNull { data ->
            when (data) {
                is RecordsFilter.ManuallyFilteredItem.Tracked -> {
                    MANUALLY_ITEM_TRACKED + data.id
                }
                is RecordsFilter.ManuallyFilteredItem.Running -> null
                is RecordsFilter.ManuallyFilteredItem.Multitask -> {
                    MANUALLY_ITEM_MULTITASK + data.ids.joinToString(separator = SEPARATOR)
                }
                is RecordsFilter.ManuallyFilteredItem.Untracked -> {
                    MANUALLY_ITEM_UNTRACKED + data.range.timeStarted + SEPARATOR + data.range.timeEnded
                }
            }
        }?.joinToString(separator = GROUP_SEPARATOR)

        return FavouriteRecordsFilterDBO.FilterDBO(
            id = 0L,
            ownerId = 0L, // Filled later.
            type = when (domain) {
                is RecordsFilter.Untracked -> 0L
                is RecordsFilter.Multitask -> 1L
                is RecordsFilter.Activity -> 2L
                is RecordsFilter.Category -> 3L
                is RecordsFilter.Tags -> 4L
                is RecordsFilter.Comment -> 5L
                is RecordsFilter.Date -> 6L
                is RecordsFilter.ManuallyFiltered -> 7L
                is RecordsFilter.DaysOfWeek -> 8L
                is RecordsFilter.TimeOfDay -> 9L
                is RecordsFilter.Duration -> 10L
                is RecordsFilter.Duplications -> 11L
            },
            commonItemsIds = commonItemsIds,
            commentItemsIds = commentItemsIds,
            commentItemsText = commentItemsText,
            duplicationItemsIds = duplicationItemsIds,
            manuallyFilteredItemsIds = manuallyFilteredItemsIds,
            range = range,
            rangeLength = rangeLength,
            daysOfWeek = daysOfWeek?.let(daysOfWeekDataLocalMapper::mapDaysOfWeek),
        )
    }

    companion object {
        private const val SEPARATOR = ","
        private const val GROUP_SEPARATOR = "|"

        private const val COMMON_ITEM_UNCATEGORIZED = "UNCAT"
        private const val COMMON_ITEM_UNTAGGED = "UNTAG"
        private const val COMMENT_ITEM_NO_COMMENT = "NO"
        private const val COMMENT_ITEM_ANY_COMMENT = "ANY"
        private const val DUPLICATION_ITEM_SAME_ACTIVITY = "ACTIVITY"
        private const val DUPLICATION_ITEM_SAME_TIMES = "TIMES"
        private const val MANUALLY_ITEM_TRACKED = "TRACKED"
        private const val MANUALLY_ITEM_MULTITASK = "MULTITASK"
        private const val MANUALLY_ITEM_UNTRACKED = "UNTRACKED"
    }
}