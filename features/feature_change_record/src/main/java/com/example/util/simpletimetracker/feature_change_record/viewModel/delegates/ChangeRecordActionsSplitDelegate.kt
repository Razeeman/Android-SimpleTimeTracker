package com.example.util.simpletimetracker.feature_change_record.viewModel.delegates

import com.example.util.simpletimetracker.core.mapper.RecordQuickActionMapper
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.recordAction.model.RecordQuickAction
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordChangePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordSliderViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimeAdjustmentViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordActionsBlock
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordDateTimeFieldsState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordPreview
import com.example.util.simpletimetracker.feature_change_record.viewModel.base.ChangeRecordDelegateBridge
import com.example.util.simpletimetracker.feature_change_record.viewModel.base.ChangeRecordActionsSubDelegate
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.ensureActive

class ChangeRecordActionsSplitDelegate @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val changeRecordViewDataMapper: ChangeRecordViewDataMapper,
    private val recordQuickActionMapper: RecordQuickActionMapper,
) : ChangeRecordActionsSubDelegate {

    private var bridge: ChangeRecordDelegateBridge? = null
    private var viewData: List<ViewHolderType> = emptyList()

    override fun attach(bridge: ChangeRecordDelegateBridge) {
        this.bridge = bridge
    }

    override fun getViewData(): List<ViewHolderType> {
        return viewData
    }

    override suspend fun updateViewData() {
        coroutineScope {
            viewData = loadViewData()
            ensureActive()
            bridge?.send(ChangeRecordDelegateBridge.Action.UpdateViewData)
        }
    }

    suspend fun onSplitClickDelegate() {
        val params = bridge?.getParams() ?: return
        val firstTypeId = params.splitParams.newBeforeTypeId
        val newBeforeTags = when (firstTypeId) {
            // Override type not selected - use same tags.
            // Override same as new type - use same tags.
            params.baseParams.newTypeId -> params.baseParams.newTags
            // Override same as original - use original tags.
            params.splitParams.originalTypeId -> params.splitParams.originalTags
            // Override to new type - use empty tags.
            else -> emptyList()
        }

        Record(
            id = 0L, // Zero id creates new record
            typeId = firstTypeId,
            timeStarted = params.baseParams.newTimeStarted,
            timeEnded = params.splitParams.newTimeSplit,
            comment = params.baseParams.newComment,
            tags = newBeforeTags,
        ).let {
            addRecordMediator.add(it)
        }
        bridge?.send(ChangeRecordDelegateBridge.Action.OnSplitComplete)
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        val params = bridge?.getParams()
            ?: return emptyList()
        val newTimeSplit = params.splitParams.newTimeSplit
        val firstTypeId = params.splitParams.newBeforeTypeId
        val secondTypeId = params.baseParams.newTypeId
        val newTimeStarted = params.baseParams.newTimeStarted
        val newTimeEnded = params.splitParams.splitPreviewTimeEnded
        val showTimeEnded = params.splitParams.showTimeEndedOnSplitPreview
        val isDarkTheme = prefsInteractor.getDarkMode()
        val action = RecordQuickAction.SPLIT

        val result = mutableListOf<ViewHolderType>()
        result += HintViewData(
            text = recordQuickActionMapper.mapHint(action).orEmpty(),
        )
        result += ChangeRecordTimePreviewViewData(
            block = ChangeRecordActionsBlock.SplitTimePreview,
            text = loadTimeSplitValue(newTimeSplit),
        )
        result += ChangeRecordTimeAdjustmentViewData(
            block = ChangeRecordActionsBlock.SplitTimeAdjustment,
            items = loadTimeSplitAdjustmentItems(),
        )
        result += ChangeRecordSliderViewData(
            block = ChangeRecordActionsBlock.SplitSlider,
            min = 0f,
            max = TimeUnit.MILLISECONDS.toSeconds(newTimeEnded - newTimeStarted).toFloat(),
            value = TimeUnit.MILLISECONDS.toSeconds(newTimeSplit - newTimeStarted).toFloat(),
        )
        val previewData = loadSplitPreviewViewData(
            firstTypeId = firstTypeId,
            secondTypeId = secondTypeId,
            newTimeStarted = newTimeStarted,
            newTimeSplit = newTimeSplit,
            newTimeEnded = newTimeEnded,
            showTimeEnded = showTimeEnded,
        )
        result += ChangeRecordChangePreviewViewData(
            id = previewData.id,
            before = previewData.before,
            after = previewData.after,
            marginTopDp = 2,
            isChecked = false,
            isRemoveVisible = false,
            isCheckVisible = false,
            isCompareVisible = false,
            isBeforeActionVisible = true,
            isAfterActionVisible = true,
        )
        result += changeRecordViewDataMapper.mapRecordActionButton(
            action = action,
            isEnabled = params.baseParams.isButtonEnabled,
            isDarkTheme = isDarkTheme,
        )
        return result
    }

    private fun loadTimeSplitAdjustmentItems(): List<ViewHolderType> {
        return changeRecordViewDataInteractor.getTimeAdjustmentItems(
            dateTimeFieldState = ChangeRecordDateTimeFieldsState.State.DateTime,
        )
    }

    private suspend fun loadTimeSplitValue(
        newTimeSplit: Long,
    ): String {
        return changeRecordViewDataInteractor.mapTime(newTimeSplit)
    }

    private suspend fun loadSplitPreviewViewData(
        firstTypeId: Long,
        secondTypeId: Long,
        newTimeStarted: Long,
        newTimeSplit: Long,
        newTimeEnded: Long,
        showTimeEnded: Boolean,
    ): ChangeRecordPreview {
        val dateTimeFieldState = ChangeRecordDateTimeFieldsState(
            start = ChangeRecordDateTimeFieldsState.State.DateTime,
            end = ChangeRecordDateTimeFieldsState.State.DateTime,
        )
        val firstRecord = Record(
            typeId = firstTypeId,
            timeStarted = newTimeStarted,
            timeEnded = newTimeSplit,
            comment = "",
            tags = emptyList(),
        ).let {
            changeRecordViewDataInteractor.getPreviewViewData(it, dateTimeFieldState)
        }
        val secondRecord = Record(
            typeId = secondTypeId,
            timeStarted = newTimeSplit,
            timeEnded = newTimeEnded,
            comment = "",
            tags = emptyList(),
        ).let {
            changeRecordViewDataInteractor.getPreviewViewData(it, dateTimeFieldState)
        }

        return ChangeRecordPreview(
            id = 0,
            before = changeRecordViewDataMapper.mapSimple(
                preview = firstRecord,
                showTimeEnded = true,
                timeStartedChanged = false,
                timeEndedChanged = true,
            ),
            after = changeRecordViewDataMapper.mapSimple(
                preview = secondRecord,
                showTimeEnded = showTimeEnded,
                timeStartedChanged = true,
                timeEndedChanged = false,
            ),
        )
    }
}