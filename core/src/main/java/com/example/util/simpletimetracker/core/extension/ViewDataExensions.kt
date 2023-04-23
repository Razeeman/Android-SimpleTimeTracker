package com.example.util.simpletimetracker.core.extension

import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.GoalTimeViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
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

fun RecordsFilterParam.toModel(): RecordsFilter {
    return when (this) {
        is RecordsFilterParam.Activity -> RecordsFilter.Activity(typeIds)
        is RecordsFilterParam.Category -> RecordsFilter.Category(categoryIds)
        is RecordsFilterParam.Comment -> RecordsFilter.Comment(comment)
        is RecordsFilterParam.Date -> RecordsFilter.Date(Range(rangeStart, rangeEnd))
        is RecordsFilterParam.SelectedTags -> RecordsFilter.SelectedTags(tags.map { it.toModel() })
        is RecordsFilterParam.FilteredTags -> RecordsFilter.FilteredTags(tags.map { it.toModel() })
        is RecordsFilterParam.ManuallyFiltered -> RecordsFilter.ManuallyFiltered(recordIds)
    }
}

fun RecordsFilter.toParams(): RecordsFilterParam {
    return when (this) {
        is RecordsFilter.Activity -> RecordsFilterParam.Activity(typeIds)
        is RecordsFilter.Category -> RecordsFilterParam.Category(categoryIds)
        is RecordsFilter.Comment -> RecordsFilterParam.Comment(comment)
        is RecordsFilter.Date -> RecordsFilterParam.Date(range.timeStarted, range.timeEnded)
        is RecordsFilter.SelectedTags -> RecordsFilterParam.SelectedTags(tags.map { it.toParams() })
        is RecordsFilter.FilteredTags -> RecordsFilterParam.FilteredTags(tags.map { it.toParams() })
        is RecordsFilter.ManuallyFiltered -> RecordsFilterParam.ManuallyFiltered(recordIds)
    }
}

fun RecordsFilterParam.Tag.toModel(): RecordsFilter.Tag {
    return when (this) {
        is RecordsFilterParam.Tag.Tagged -> RecordsFilter.Tag.Tagged(tagId)
        is RecordsFilterParam.Tag.Untagged -> RecordsFilter.Tag.Untagged(typeId)
    }
}

fun RecordsFilter.Tag.toParams(): RecordsFilterParam.Tag {
    return when (this) {
        is RecordsFilter.Tag.Tagged -> RecordsFilterParam.Tag.Tagged(tagId)
        is RecordsFilter.Tag.Untagged -> RecordsFilterParam.Tag.Untagged(typeId)
    }
}
