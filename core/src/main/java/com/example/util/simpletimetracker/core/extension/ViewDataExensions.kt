package com.example.util.simpletimetracker.core.extension

import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.GoalTimeViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordTypeIconParams

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
