package com.example.util.simpletimetracker.feature_change_record.viewModel.delegates

import com.example.util.simpletimetracker.core.mapper.RecordQuickActionMapper
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.recordAction.interactor.RecordActionMergeMediator
import com.example.util.simpletimetracker.domain.recordAction.model.RecordQuickAction
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.api.model.ChangeRecordDateTimeFieldsState
import com.example.util.simpletimetracker.feature_change_record.api.viewData.ChangeRecordChangePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordPreview
import com.example.util.simpletimetracker.feature_change_record.viewModel.base.ChangeRecordDelegateBridge
import com.example.util.simpletimetracker.feature_change_record.viewModel.base.ChangeRecordActionsSubDelegate
import com.example.util.simpletimetracker.navigation.Router
import javax.inject.Inject

class ChangeRecordActionsMergeDelegate @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val recordActionMergeMediator: RecordActionMergeMediator,
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
        viewData = loadViewData()
        bridge?.send(ChangeRecordDelegateBridge.Action.UpdateViewData)
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        val params = bridge?.getParams() ?: return emptyList()
        if (!params.mergeParams.mergeAvailable) return emptyList()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val action = RecordQuickAction.MERGE

        val result = mutableListOf<ViewHolderType>()
        val previewData = loadMergePreviewViewData(
            prevRecord = params.mergeParams.prevRecord,
            newTimeEnded = params.baseParams.newTimeEnded,
        )
        if (previewData != null) {
            result += HintViewData(
                text = recordQuickActionMapper.mapHint(action).orEmpty(),
            )
            result += ChangeRecordChangePreviewViewData(
                id = previewData.id,
                before = previewData.before,
                after = previewData.after,
                isChecked = false,
                marginTopDp = 0,
                isRemoveVisible = false,
                isCheckVisible = false,
                isCompareVisible = true,
                isBeforeActionVisible = false,
                isAfterActionVisible = false,
            )
            result += changeRecordViewDataMapper.mapRecordActionButton(
                action = action,
                isEnabled = params.baseParams.isButtonEnabled,
                isDarkTheme = isDarkTheme,
            )
        }
        return result
    }

    suspend fun onMergeClickDelegate() {
        val params = bridge?.getParams() ?: return
        recordActionMergeMediator.execute(
            prevRecord = params.mergeParams.prevRecord,
            newTimeEnded = params.baseParams.newTimeEnded,
            onMergeComplete = { router.back() },
        )
    }

    private fun getChangedRecord(
        previousRecord: Record,
        newTimeEnded: Long,
    ): Record {
        return previousRecord.copy(
            timeEnded = newTimeEnded,
        )
    }

    private suspend fun loadMergePreviewViewData(
        prevRecord: Record?,
        newTimeEnded: Long,
    ): ChangeRecordPreview? {
        if (prevRecord == null) return null

        val dateTimeFieldState = ChangeRecordDateTimeFieldsState(
            start = ChangeRecordDateTimeFieldsState.State.DateTime,
            end = ChangeRecordDateTimeFieldsState.State.DateTime,
        )
        val changedRecord = getChangedRecord(prevRecord, newTimeEnded)
        val previousRecordPreview = changeRecordViewDataInteractor
            .getPreviewViewData(prevRecord, dateTimeFieldState)
        val changedRecordPreview = changeRecordViewDataInteractor
            .getPreviewViewData(changedRecord, dateTimeFieldState)

        return ChangeRecordPreview(
            id = 0,
            before = changeRecordViewDataMapper.mapSimple(
                preview = previousRecordPreview,
                showTimeEnded = true,
                timeStartedChanged = false,
                timeEndedChanged = false,
            ),
            after = changeRecordViewDataMapper.mapSimple(
                preview = changedRecordPreview,
                showTimeEnded = true,
                timeStartedChanged = changedRecord.timeStarted != prevRecord.timeStarted,
                timeEndedChanged = changedRecord.timeEnded != prevRecord.timeEnded,
            ),
        )
    }
}