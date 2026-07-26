package com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.feature_dialogs.api.DateTimeDialogListener
import com.example.util.simpletimetracker.feature_dialogs.api.RecordQuickActionDialogListener
import com.example.util.simpletimetracker.feature_dialogs.api.TypesSelectionDialogListener
import com.example.util.simpletimetracker.core.extension.findListener
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.sharedViewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.adapter.RecordQuickActionsWidthHolder
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.adapter.createRecordQuickActionsButtonAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.adapter.createRecordQuickActionsButtonBigAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.model.RecordQuickActionsState
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.viewModel.RecordQuickActionsViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setSpanSizeLookup
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_dialogs.databinding.RecordQuickActionsDialogFragmentBinding as Binding

@AndroidEntryPoint
class RecordQuickActionsDialogFragment :
    BaseBottomSheetFragment<Binding>(),
    TypesSelectionDialogListener,
    DateTimeDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var removeRecordViewModelFactory: BaseViewModelFactory<RemoveRecordViewModel>

    private val viewModel: RecordQuickActionsViewModel by viewModels()
    private val removeRecordViewModel: RemoveRecordViewModel by activityViewModels(
        factoryProducer = { removeRecordViewModelFactory },
    )

    private val contentAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordQuickActionsButtonBigAdapterDelegate(viewModel::onButtonClick),
            createRecordQuickActionsButtonAdapterDelegate(viewModel::onButtonClick),
        )
    }

    private val params: RecordQuickActionsParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = RecordQuickActionsParams.Empty,
    )
    private var listener: RecordQuickActionDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context.findListener<RecordQuickActionDialogListener>()
    }

    override fun initDialog() {
        setSkipCollapsed()
    }

    override fun initUi(): Unit = with(binding) {
        rvRecordQuickActions.apply {
            val manager = GridLayoutManager(context, SPAN_COUNT)
            layoutManager = manager
            adapter = contentAdapter
            setIconsSpanSize(manager, contentAdapter)
        }
    }

    override fun initUx() = with(binding) {
        btnRecordQuickActionsHint.setOnClick(throttle(viewModel::onHintClick))
        hintRecordQuickActionsMultiselect.setOnActionClick(throttle(viewModel::onMultiselectCancelClick))
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        state.observe(::updateState)
        actionComplete.observe { onActionComplete() }
        removeRecordIds.observe {
            removeRecordViewModel.onDeleteClick(
                recordIds = it,
                from = ChangeRecordParams.From.Records,
            )
        }
        removeRecordViewModel.prepare()
    }

    override fun onDataSelected(
        tag: String?,
        dataIds: List<Long>,
        tagValues: List<RecordBase.Tag>,
        selectValueOnStartTagIds: List<Long>,
    ) {
        viewModel.onTypesSelected(
            tag = tag,
            dataIds = dataIds,
            tagValues = tagValues,
        )
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    private fun updateState(state: RecordQuickActionsState) = with(binding) {
        contentAdapter.replace(state.buttons)
        btnRecordQuickActionsHint.isVisible = state.helpData.isNotEmpty()
        setHintData(state.hintData)
    }

    private fun setHintData(data: RecordQuickActionsState.Hint?) = with(binding) {
        dividerRecordQuickActions.isVisible = data != null
        hintRecordQuickActionsMultiselect.isVisible = data is RecordQuickActionsState.Hint.MultiSelect
        hintRecordQuickActionsSelected.isVisible = data is RecordQuickActionsState.Hint.Record

        when (data) {
            is RecordQuickActionsState.Hint.Record -> {
                hintRecordQuickActionsSelected.itemName = data.name
                hintRecordQuickActionsSelected.itemIcon = data.iconId
                hintRecordQuickActionsSelected.itemColor = data.color
                hintRecordQuickActionsSelected.itemTimeStarted = data.timeStarted
                hintRecordQuickActionsSelected.itemTimeEnded = data.timeEnded.orEmpty()
                hintRecordQuickActionsSelected.itemDuration = data.duration
            }
            is RecordQuickActionsState.Hint.MultiSelect -> {
                hintRecordQuickActionsMultiselect.itemText = data.hint
            }
            null -> Unit
        }
    }

    private fun onActionComplete() {
        listener?.onActionComplete()
    }

    private fun setIconsSpanSize(
        layoutManager: GridLayoutManager?,
        adapter: BaseRecyclerAdapter,
    ) {
        layoutManager?.setSpanSizeLookup { position ->
            val item = adapter.getItemByPosition(position)
            val isFullWidth = item is RecordQuickActionsWidthHolder &&
                item.width is RecordQuickActionsWidthHolder.Width.Full
            if (isFullWidth) SPAN_COUNT else 1
        }
    }

    companion object {
        private const val SPAN_COUNT = 2

        fun createBundle(data: RecordQuickActionsParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}