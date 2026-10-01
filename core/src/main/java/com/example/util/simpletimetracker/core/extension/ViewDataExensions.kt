package com.example.util.simpletimetracker.core.extension

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.viewData.ChangeRecordDateTimeState
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordDataSelectionDialogResult
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.recordShortcut.RecordShortcutViewData
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.GoalTimeViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordDateTimeStateParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeShortcutParams
import com.example.util.simpletimetracker.navigation.params.screen.RangeLengthParams
import com.example.util.simpletimetracker.navigation.params.screen.RangeParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagParam
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
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
        state = this.state?.toViewData(),
    )
}

fun GoalTimeViewData.toParams(): ChangeRunningRecordParams.Preview.GoalTimeParams {
    return ChangeRunningRecordParams.Preview.GoalTimeParams(
        text = this.text,
        state = this.state?.toParams(),
    )
}

fun ChangeRunningRecordParams.Preview.GoalSubtypeParams.toViewData(): RecordTypeGoal.Subtype {
    return when (this) {
        is ChangeRunningRecordParams.Preview.GoalSubtypeParams.Goal -> RecordTypeGoal.Subtype.Goal
        is ChangeRunningRecordParams.Preview.GoalSubtypeParams.Limit -> RecordTypeGoal.Subtype.Limit
    }
}

fun RecordTypeGoal.Subtype.toParams(): ChangeRunningRecordParams.Preview.GoalSubtypeParams {
    return when (this) {
        is RecordTypeGoal.Subtype.Goal -> ChangeRunningRecordParams.Preview.GoalSubtypeParams.Goal
        is RecordTypeGoal.Subtype.Limit -> ChangeRunningRecordParams.Preview.GoalSubtypeParams.Limit
    }
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
        is RecordsFilterParam.Activity -> RecordsFilter.Activity(
            selected = selected,
            filtered = filtered,
        )
        is RecordsFilterParam.Category -> RecordsFilter.Category(
            selected = selected.map { it.toModel() },
            filtered = filtered.map { it.toModel() },
        )
        is RecordsFilterParam.Comment -> RecordsFilter.Comment(items.map { it.toModel() })
        is RecordsFilterParam.Date -> RecordsFilter.Date(range.toModel(), position)
        is RecordsFilterParam.Tags -> RecordsFilter.Tags(
            selected = selected.map { it.toModel() },
            filtered = filtered.map { it.toModel() },
        )
        is RecordsFilterParam.ManuallyFiltered -> RecordsFilter.ManuallyFiltered(items.map { it.toModel() })
        is RecordsFilterParam.DaysOfWeek -> RecordsFilter.DaysOfWeek(items)
        is RecordsFilterParam.TimeOfDay -> RecordsFilter.TimeOfDay(range.toModel())
        is RecordsFilterParam.Duration -> RecordsFilter.Duration(range.toModel())
        is RecordsFilterParam.Duplications -> RecordsFilter.Duplications(items.map { it.toModel() })
    }
}

fun RecordsFilter.toParams(): RecordsFilterParam {
    return when (this) {
        is RecordsFilter.Untracked -> RecordsFilterParam.Untracked
        is RecordsFilter.Multitask -> RecordsFilterParam.Multitask
        is RecordsFilter.Activity -> RecordsFilterParam.Activity(
            selected = selected,
            filtered = filtered,
        )
        is RecordsFilter.Category -> RecordsFilterParam.Category(
            selected = selected.map { it.toParams() },
            filtered = filtered.map { it.toParams() },
        )
        is RecordsFilter.Comment -> RecordsFilterParam.Comment(items.map { it.toParams() })
        is RecordsFilter.Date -> RecordsFilterParam.Date(range.toParams(), position)
        is RecordsFilter.Tags -> RecordsFilterParam.Tags(
            selected = selected.map { it.toParams() },
            filtered = filtered.map { it.toParams() },
        )
        is RecordsFilter.ManuallyFiltered -> RecordsFilterParam.ManuallyFiltered(items.map { it.toParams() })
        is RecordsFilter.DaysOfWeek -> RecordsFilterParam.DaysOfWeek(items)
        is RecordsFilter.TimeOfDay -> RecordsFilterParam.TimeOfDay(range.toParams())
        is RecordsFilter.Duration -> RecordsFilterParam.Duration(range.toParams())
        is RecordsFilter.Duplications -> RecordsFilterParam.Duplications(items.map { it.toParams() })
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

fun RecordsFilterParam.ManuallyFilteredItem.toModel(): RecordsFilter.ManuallyFilteredItem {
    return when (this) {
        is RecordsFilterParam.ManuallyFilteredItem.Tracked ->
            RecordsFilter.ManuallyFilteredItem.Tracked(id)
        is RecordsFilterParam.ManuallyFilteredItem.Running ->
            RecordsFilter.ManuallyFilteredItem.Running(id)
        is RecordsFilterParam.ManuallyFilteredItem.Untracked ->
            RecordsFilter.ManuallyFilteredItem.Untracked(range.toModel())
        is RecordsFilterParam.ManuallyFilteredItem.Multitask ->
            RecordsFilter.ManuallyFilteredItem.Multitask(ids)
    }
}

fun RecordsFilter.ManuallyFilteredItem.toParams(): RecordsFilterParam.ManuallyFilteredItem {
    return when (this) {
        is RecordsFilter.ManuallyFilteredItem.Tracked ->
            RecordsFilterParam.ManuallyFilteredItem.Tracked(id)
        is RecordsFilter.ManuallyFilteredItem.Running ->
            RecordsFilterParam.ManuallyFilteredItem.Running(id)
        is RecordsFilter.ManuallyFilteredItem.Untracked ->
            RecordsFilterParam.ManuallyFilteredItem.Untracked(range.toParams())
        is RecordsFilter.ManuallyFilteredItem.Multitask ->
            RecordsFilterParam.ManuallyFilteredItem.Multitask(ids)
    }
}

fun RecordsFilterParam.DuplicationsItem.toModel(): RecordsFilter.DuplicationsItem {
    return when (this) {
        is RecordsFilterParam.DuplicationsItem.SameActivity -> RecordsFilter.DuplicationsItem.SameActivity
        is RecordsFilterParam.DuplicationsItem.SameTimes -> RecordsFilter.DuplicationsItem.SameTimes
    }
}

fun RecordsFilter.DuplicationsItem.toParams(): RecordsFilterParam.DuplicationsItem {
    return when (this) {
        is RecordsFilter.DuplicationsItem.SameActivity -> RecordsFilterParam.DuplicationsItem.SameActivity
        is RecordsFilter.DuplicationsItem.SameTimes -> RecordsFilterParam.DuplicationsItem.SameTimes
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

fun List<RecordDataSelectionDialogResult.Field>.toParams(): List<RecordTagSelectionParams.FieldParam> {
    return this.map {
        when (it) {
            is RecordDataSelectionDialogResult.Field.Tags -> RecordTagSelectionParams.FieldParam.Tags
            is RecordDataSelectionDialogResult.Field.Comment -> RecordTagSelectionParams.FieldParam.Comment
        }
    }
}

fun RecordTagParam.toModel(): RecordBase.Tag {
    return RecordBase.Tag(
        tagId = tagId,
        numericValue = numericValue,
    )
}

fun RecordBase.Tag.toParams(): RecordTagParam {
    return RecordTagParam(
        tagId = tagId,
        numericValue = numericValue,
    )
}

fun RecordShortcutViewData.toPreview(): ChangeShortcutParams.Preview {
    return ChangeShortcutParams.Preview(
        name = data.name,
        color = data.color,
        icon = data.icon?.toParams(),
        iconColor = data.iconColor,
        iconAlpha = data.iconAlpha,
    )
}