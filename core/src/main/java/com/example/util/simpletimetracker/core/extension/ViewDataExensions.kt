package com.example.util.simpletimetracker.core.extension

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.viewData.ChangeRecordDateTimeState
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.GoalTimeViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordDateTimeStateParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.RangeLengthParams
import com.example.util.simpletimetracker.navigation.params.screen.RangeParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordTypeIconParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParam

fun RecordTypeIconParams.toViewData(): RecordTypeIcon {
    return when (this) {
        is RecordTypeIconParams.Image -> RecordTypeIcon.Image(this.iconId)
        is RecordTypeIconParams.Text -> RecordTypeIcon.Text(this.text)
    }
}

fun RecordTypeIcon.toParams(): RecordTypeIconParams {
    return when (this) {
        is RecordTypeIcon.Image -> RecordTypeIconParams.Image(this.iconId)
        is RecordTypeIcon.Text -> RecordTypeIconParams.Text(this.text)
    }
}

fun RangeParams.toModel(): Range {
    return Range(
        timeStarted = timeStarted,
        timeEnded = timeEnded,
    )
}

fun Range.toParams(): RangeParams {
    return RangeParams(
        timeStarted = timeStarted,
        timeEnded = timeEnded,
    )
}

fun ChangeRunningRecordParams.Preview.GoalTimeParams.toViewData(): GoalTimeViewData {
    return GoalTimeViewData(
        text = this.text,
        complete = this.complete,
    )
}

fun GoalTimeViewData.toParams(): ChangeRunningRecordParams.Preview.GoalTimeParams {
    return ChangeRunningRecordParams.Preview.GoalTimeParams(
        text = this.text,
        complete = this.complete,
    )
}

fun ChangeRecordDateTimeStateParams.toViewData(): ChangeRecordDateTimeState {
    val state = when (val state = this.state) {
        is ChangeRecordDateTimeStateParams.State.DateTime -> {
            val dateTime = TimeMapper.DateTime(
                date = state.date,
                time = state.time,
            )
            ChangeRecordDateTimeState.State.DateTime(dateTime)
        }
        is ChangeRecordDateTimeStateParams.State.Duration -> {
            ChangeRecordDateTimeState.State.Duration(state.data)
        }
    }
    return ChangeRecordDateTimeState(
        hint = hint,
        state = state,
    )
}

fun ChangeRecordDateTimeState.toRecordParams(): ChangeRecordDateTimeStateParams {
    val state = when (val state = this.state) {
        is ChangeRecordDateTimeState.State.DateTime -> {
            ChangeRecordDateTimeStateParams.State.DateTime(
                date = state.data.date,
                time = state.data.time,
            )
        }
        is ChangeRecordDateTimeState.State.Duration -> {
            ChangeRecordDateTimeStateParams.State.Duration(state.data)
        }
    }
    return ChangeRecordDateTimeStateParams(
        hint = hint,
        state = state,
    )
}

fun RecordsFilterParam.toModel(): RecordsFilter {
    return when (this) {
        is RecordsFilterParam.Untracked -> RecordsFilter.Untracked
        is RecordsFilterParam.Multitask -> RecordsFilter.Multitask
        is RecordsFilterParam.Activity -> RecordsFilter.Activity(typeIds)
        is RecordsFilterParam.Category -> RecordsFilter.Category(items.map { it.toModel() })
        is RecordsFilterParam.Comment -> RecordsFilter.Comment(items.map { it.toModel() })
        is RecordsFilterParam.Date -> RecordsFilter.Date(range.toModel(), position)
        is RecordsFilterParam.SelectedTags -> RecordsFilter.SelectedTags(items.map { it.toModel() })
        is RecordsFilterParam.FilteredTags -> RecordsFilter.FilteredTags(items.map { it.toModel() })
        is RecordsFilterParam.ManuallyFiltered -> RecordsFilter.ManuallyFiltered(recordIds)
        is RecordsFilterParam.DaysOfWeek -> RecordsFilter.DaysOfWeek(items)
        is RecordsFilterParam.TimeOfDay -> RecordsFilter.TimeOfDay(range.toModel())
        is RecordsFilterParam.Duration -> RecordsFilter.Duration(range.toModel())
    }
}

fun RecordsFilter.toParams(): RecordsFilterParam {
    return when (this) {
        is RecordsFilter.Untracked -> RecordsFilterParam.Untracked
        is RecordsFilter.Multitask -> RecordsFilterParam.Multitask
        is RecordsFilter.Activity -> RecordsFilterParam.Activity(typeIds)
        is RecordsFilter.Category -> RecordsFilterParam.Category(items.map { it.toParams() })
        is RecordsFilter.Comment -> RecordsFilterParam.Comment(items.map { it.toParams() })
        is RecordsFilter.Date -> RecordsFilterParam.Date(range.toParams(), position)
        is RecordsFilter.SelectedTags -> RecordsFilterParam.SelectedTags(items.map { it.toParams() })
        is RecordsFilter.FilteredTags -> RecordsFilterParam.FilteredTags(items.map { it.toParams() })
        is RecordsFilter.ManuallyFiltered -> RecordsFilterParam.ManuallyFiltered(recordIds)
        is RecordsFilter.DaysOfWeek -> RecordsFilterParam.DaysOfWeek(items)
        is RecordsFilter.TimeOfDay -> RecordsFilterParam.TimeOfDay(range.toParams())
        is RecordsFilter.Duration -> RecordsFilterParam.Duration(range.toParams())
    }
}

fun RecordsFilterParam.CommentItem.toModel(): RecordsFilter.CommentItem {
    return when (this) {
        is RecordsFilterParam.CommentItem.NoComment -> RecordsFilter.CommentItem.NoComment
        is RecordsFilterParam.CommentItem.AnyComment -> RecordsFilter.CommentItem.AnyComment
        is RecordsFilterParam.CommentItem.Comment -> RecordsFilter.CommentItem.Comment(text)
    }
}

fun RecordsFilter.CommentItem.toParams(): RecordsFilterParam.CommentItem {
    return when (this) {
        is RecordsFilter.CommentItem.NoComment -> RecordsFilterParam.CommentItem.NoComment
        is RecordsFilter.CommentItem.AnyComment -> RecordsFilterParam.CommentItem.AnyComment
        is RecordsFilter.CommentItem.Comment -> RecordsFilterParam.CommentItem.Comment(text)
    }
}

fun RecordsFilterParam.CategoryItem.toModel(): RecordsFilter.CategoryItem {
    return when (this) {
        is RecordsFilterParam.CategoryItem.Categorized -> RecordsFilter.CategoryItem.Categorized(categoryId)
        is RecordsFilterParam.CategoryItem.Uncategorized -> RecordsFilter.CategoryItem.Uncategorized
    }
}

fun RecordsFilter.CategoryItem.toParams(): RecordsFilterParam.CategoryItem {
    return when (this) {
        is RecordsFilter.CategoryItem.Categorized -> RecordsFilterParam.CategoryItem.Categorized(categoryId)
        is RecordsFilter.CategoryItem.Uncategorized -> RecordsFilterParam.CategoryItem.Uncategorized
    }
}

fun RecordsFilterParam.TagItem.toModel(): RecordsFilter.TagItem {
    return when (this) {
        is RecordsFilterParam.TagItem.Tagged -> RecordsFilter.TagItem.Tagged(tagId)
        is RecordsFilterParam.TagItem.Untagged -> RecordsFilter.TagItem.Untagged
    }
}

fun RecordsFilter.TagItem.toParams(): RecordsFilterParam.TagItem {
    return when (this) {
        is RecordsFilter.TagItem.Tagged -> RecordsFilterParam.TagItem.Tagged(tagId)
        is RecordsFilter.TagItem.Untagged -> RecordsFilterParam.TagItem.Untagged
    }
}

fun RangeLengthParams.toModel(): RangeLength {
    return when (this) {
        is RangeLengthParams.Day -> RangeLength.Day
        is RangeLengthParams.Week -> RangeLength.Week
        is RangeLengthParams.Month -> RangeLength.Month
        is RangeLengthParams.Year -> RangeLength.Year
        is RangeLengthParams.All -> RangeLength.All
        is RangeLengthParams.Custom -> Range(
            timeStarted = start, timeEnded = end,
        ).let(RangeLength::Custom)
        is RangeLengthParams.Last -> RangeLength.Last(
            days = days,
        )
    }
}

fun RangeLength.toParams(): RangeLengthParams {
    return when (this) {
        is RangeLength.Day -> RangeLengthParams.Day
        is RangeLength.Week -> RangeLengthParams.Week
        is RangeLength.Month -> RangeLengthParams.Month
        is RangeLength.Year -> RangeLengthParams.Year
        is RangeLength.All -> RangeLengthParams.All
        is RangeLength.Custom -> RangeLengthParams.Custom(
            start = range.timeStarted,
            end = range.timeEnded,
        )
        is RangeLength.Last -> RangeLengthParams.Last(
            days = days,
        )
    }
}
