package com.example.util.simpletimetracker.feature_change_record.interactor

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.timeAdjustment.TimeAdjustmentView
import com.example.util.simpletimetracker.domain.interactor.FavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordCommentFieldViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordCommentViewData
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordDateTimeFieldsState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordFavCommentState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordSearchCommentState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import java.util.Locale
import javax.inject.Inject

class ChangeRecordViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val favouriteCommentInteractor: FavouriteCommentInteractor,
    private val changeRecordViewDataMapper: ChangeRecordViewDataMapper,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val colorMapper: ColorMapper,
) {

    suspend fun getPreviewViewData(
        record: Record,
        dateTimeFieldState: ChangeRecordDateTimeFieldsState,
    ): ChangeRecordViewData {
        // TODO pass cached data?
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
            dateTimeFieldState = dateTimeFieldState,
        )
    }

    suspend fun getLastCommentsViewData(
        typeId: Long,
    ): List<ViewHolderType> {
        data class Data(val timeStarted: Long, val comment: String)

        val favouriteComments = favouriteCommentInteractor.getAll()
            .map { ChangeRecordCommentViewData.Favourite(it.comment) }
            .takeUnless { it.isEmpty() }
            ?.let {
                HintViewData(
                    text = resourceRepo.getString(R.string.change_record_favourite_comments_hint),
                ).let(::listOf) + it
            }.orEmpty()

        val records = recordInteractor.getByTypeWithAnyComment(listOf(typeId))
            .map { Data(it.timeStarted, it.comment) }
        val runningRecords = runningRecordInteractor.getAll()
            .filter { it.id == typeId && it.comment.isNotEmpty() }
            .map { Data(it.timeStarted, it.comment) }

        val lastComments = (records + runningRecords)
            .asSequence()
            .sortedByDescending { it.timeStarted }
            .map { it.comment }
            .toSet()
            .take(LAST_COMMENTS_TO_SHOW)
            .map { ChangeRecordCommentViewData.Last(it) }
            .takeUnless { it.isEmpty() }
            ?.let {
                HintViewData(
                    text = resourceRepo.getString(R.string.change_record_last_comments_hint),
                ).let(::listOf) + it
            }.orEmpty()

        return favouriteComments + lastComments
    }

    suspend fun getFavCommentViewData(
        comment: String,
    ): ChangeRecordFavCommentState {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val isFavourite = favouriteCommentInteractor.get(comment) != null

        return ChangeRecordFavCommentState(
            iconColor = if (isFavourite) {
                resourceRepo.getColor(R.color.colorSecondary)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            },
            isVisible = comment.isNotEmpty(),
        )
    }

    suspend fun getSearchCommentViewData(
        isEnabled: Boolean,
        isLoading: Boolean,
        search: String,
    ): ChangeRecordSearchCommentState {
        val items = mutableListOf<ViewHolderType>()

        ChangeRecordCommentFieldViewData(
            id = 1L, // Only one at the time.
            text = search,
        ).let(items::add)

        when {
            isLoading -> LoaderViewData().let(::listOf)
            search.isEmpty() -> emptyList()
            else -> {
                recordInteractor.searchComment(search)
                    .sortedByDescending { it.timeStarted }
                    .map { ChangeRecordCommentViewData.Last(it.comment) }
            }
        }.let(items::addAll)

        return ChangeRecordSearchCommentState(
            enabled = isEnabled,
            items = items,
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

    companion object {
        private const val LAST_COMMENTS_TO_SHOW = 20
    }
}
