package com.example.util.simpletimetracker.feature_change_record.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.timeAdjustment.TimeAdjustmentView
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordCommentViewData
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import javax.inject.Inject

class ChangeRecordViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val changeRecordViewDataMapper: ChangeRecordViewDataMapper,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
) {

    suspend fun getPreviewViewData(
        record: Record,
    ): ChangeRecordViewData {
        val type = recordTypeInteractor.get(record.typeId)
        val tags = recordTagInteractor.getAll().filter { it.id in record.tagIds }
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showSeconds = prefsInteractor.getShowSeconds()

        return changeRecordViewDataMapper.map(
            record = record,
            recordType = type,
            recordTags = tags,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        )
    }

    suspend fun getLastCommentsViewData(
        typeId: Long,
    ): List<ViewHolderType> {
        data class Data(val timeStarted: Long, val comment: String)

        val records = recordInteractor.getByTypeWithComment(listOf(typeId))
            .map { Data(it.timeStarted, it.comment) }
        val runningRecords = runningRecordInteractor.getAll()
            .filter { it.id == typeId && it.comment.isNotEmpty() }
            .map { Data(it.timeStarted, it.comment) }

        return (records + runningRecords)
            .asSequence()
            .sortedByDescending { it.timeStarted }
            .map { it.comment }
            .toSet()
            .take(LAST_COMMENTS_TO_SHOW)
            .map { ChangeRecordCommentViewData(it) }
            .takeUnless { it.isEmpty() }
            ?.let {
                HintViewData(
                    text = resourceRepo.getString(R.string.change_record_last_comments_hint)
                ).let(::listOf) + it
            }.orEmpty()
    }

    fun getTimeAdjustmentItems(): List<ViewHolderType> {
        return listOf(
            TimeAdjustmentView.ViewData.Adjust(text = "-30", value = -30),
            TimeAdjustmentView.ViewData.Adjust(text = "-5", value = -5),
            TimeAdjustmentView.ViewData.Adjust(text = "-1", value = -1),
            TimeAdjustmentView.ViewData.Adjust(text = "+1", value = +1),
            TimeAdjustmentView.ViewData.Adjust(text = "+5", value = +5),
            TimeAdjustmentView.ViewData.Adjust(text = "+30", value = +30),
            TimeAdjustmentView.ViewData.Now(text = resourceRepo.getString(R.string.time_now)),
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

    companion object {
        private const val LAST_COMMENTS_TO_SHOW = 20
    }
}
