package com.example.util.simpletimetracker.feature_change_record.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.timeAdjustment.TimeAdjustmentView
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.api.model.ChangeRecordDateTimeFieldsState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import javax.inject.Inject

class ChangeRecordViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val changeRecordViewDataMapper: ChangeRecordViewDataMapper,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
) {

    suspend fun getPreviewViewData(
        record: Record,
        dateTimeFieldState: ChangeRecordDateTimeFieldsState,
    ): ChangeRecordViewData {
        // TODO pass cached data?
        val type = recordTypeInteractor.get(record.typeId)
        val tags = recordTagInteractor.getAll()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val durationFormat = prefsInteractor.getDurationFormat()
        val showSeconds = prefsInteractor.getShowSeconds()

        return changeRecordViewDataMapper.map(
            record = record,
            recordType = type,
            recordTags = tags,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
            dateTimeFieldState = dateTimeFieldState,
        )
    }

    fun getTimeAdjustmentItems(
        dateTimeFieldState: ChangeRecordDateTimeFieldsState.State,
    ): List<ViewHolderType> {
        val additionalButton = when (dateTimeFieldState) {
            is ChangeRecordDateTimeFieldsState.State.DateTime -> {
                TimeAdjustmentView.ViewData.Now(text = resourceRepo.getString(R.string.time_now))
            }
            is ChangeRecordDateTimeFieldsState.State.Duration -> {
                TimeAdjustmentView.ViewData.Zero("0")
            }
        }
        return listOf(
            TimeAdjustmentView.ViewData.Adjust(text = "-30", value = -30),
            TimeAdjustmentView.ViewData.Adjust(text = "-5", value = -5),
            TimeAdjustmentView.ViewData.Adjust(text = "-1", value = -1),
            TimeAdjustmentView.ViewData.Adjust(text = "+1", value = +1),
            TimeAdjustmentView.ViewData.Adjust(text = "+5", value = +5),
            TimeAdjustmentView.ViewData.Adjust(text = "+30", value = +30),
            additionalButton,
        )
    }

    suspend fun mapTime(time: Long): String {
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()

        return timeMapper.formatDateTime(
            time = time,
            useMilitaryTime = useMilitaryTime,
            showSeconds = showSeconds,
        )
    }
}
