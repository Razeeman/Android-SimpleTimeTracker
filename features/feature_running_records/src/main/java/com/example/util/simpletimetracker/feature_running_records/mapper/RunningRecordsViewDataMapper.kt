package com.example.util.simpletimetracker.feature_running_records.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RunningRecordViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.HintBigViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordWithHint.RecordWithHintViewData
import com.example.util.simpletimetracker.feature_running_records.R
import javax.inject.Inject

class RunningRecordsViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val colorMapper: ColorMapper,
    private val runningRecordViewDataMapper: RunningRecordViewDataMapper,
    private val recordViewDataMapper: RecordViewDataMapper,
) {

    fun mapToTypesEmpty(): ViewHolderType {
        return HintBigViewData(
            text = resourceRepo.getString(
                R.string.running_records_types_empty,
                resourceRepo.getString(R.string.running_records_add_type),
                resourceRepo.getString(R.string.running_records_add_default),
            ),
            infoIconVisible = true,
            closeIconVisible = false,
        )
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.running_records_empty.let(resourceRepo::getString),
            hint = R.string.running_records_empty_hint.let(resourceRepo::getString),
        )
    }

    fun mapToHasRunningRecords(): ViewHolderType {
        return HintViewData(
            text = R.string.running_records_has_timers.let(resourceRepo::getString),
            paddingTop = 0,
            paddingBottom = 0,
        )
    }

    // TODO RETRO move strings to res.
    // TODO RETRO check repeat.
    // TODO RETRO check instant activities.
    // TODO RETRO check record actions.
    // TODO RETRO add scroll to top on first click when there were no records, otherwise hint is not visible.
    // TODO RETRO check pomodoro start on activity click.
    fun mapToRetroActiveMode(
        typesMap: Map<Long, RecordType>,
        recordTags: List<RecordTag>,
        prevRecord: Record?,
        isDarkTheme: Boolean,
        useProportionalMinutes: Boolean,
        useMilitaryTime: Boolean,
        showSeconds: Boolean,
    ): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        if (prevRecord == null) {
            result += EmptyViewData(
                message = "Click on card to select what you have been doing",
                hint = R.string.running_records_empty_hint.let(resourceRepo::getString),
            )
        }

        val type = prevRecord?.typeId?.let(typesMap::get)
        if (prevRecord != null && type != null) {
            result += runningRecordViewDataMapper.map(
                runningRecord = RunningRecord(
                    id = UNTRACKED_ITEM_ID,
                    timeStarted = prevRecord.timeEnded,
                    comment = "",
                ),
                dailyCurrent = null,
                recordType = RecordType(
                    id = 0L,
                    name = resourceRepo.getString(R.string.untracked_time_name),
                    icon = "",
                    color = AppColor(
                        0, colorMapper.toUntrackedColor(isDarkTheme).toString(),
                    ),
                    defaultDuration = 0,
                    note = "",
                ),
                recordTags = emptyList(),
                goals = emptyList(),
                isDarkTheme = isDarkTheme,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
                useProportionalMinutes = useProportionalMinutes,
                nowIconVisible = false,
                goalsVisible = false,
                totalDurationVisible = false,
            )
            result += recordViewDataMapper.map(
                record = prevRecord,
                recordType = type,
                recordTags = recordTags.filter { it.id in prevRecord.tagIds },
                isDarkTheme = isDarkTheme,
                useMilitaryTime = useMilitaryTime,
                useProportionalMinutes = useProportionalMinutes,
                showSeconds = showSeconds,
            ).let {
                RecordWithHintViewData(it)
            }
            result += HintViewData(
                text = "Select what you have been doing",
                paddingTop = 0,
                paddingBottom = 0,
            )
        }

        return result
    }
}