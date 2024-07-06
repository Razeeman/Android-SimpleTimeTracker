package com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.RecordQuickActionDialogListener
import com.example.util.simpletimetracker.core.extension.findListener
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.sharedViewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.model.RecordQuickActionsState
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.viewModel.RecordQuickActionsViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams
import com.google.android.flexbox.FlexboxLayout
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_dialogs.databinding.RecordQuickActionsDialogFragmentBinding as Binding

@AndroidEntryPoint
class RecordQuickActionsDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var removeRecordViewModelFactory: BaseViewModelFactory<RemoveRecordViewModel>

    private val viewModel: RecordQuickActionsViewModel by viewModels()
    private val removeRecordViewModel: RemoveRecordViewModel by activityViewModels(
        factoryProducer = { removeRecordViewModelFactory },
    )

    private val params: RecordQuickActionsParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = RecordQuickActionsParams(),
    )
    private var listener: RecordQuickActionDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context.findListener<RecordQuickActionDialogListener>()
    }

    override fun initDialog() {
        setSkipCollapsed()
    }

    override fun initUx(): Unit = with(binding) {
        btnRecordQuickActionsDelete.setOnClick {
            viewModel.onDeleteClicked()
            removeRecordViewModel.onDeleteClick(ChangeRecordParams.From.Records)
        }
        btnRecordQuickActionsStatistics.setOnClick(viewModel::onStatisticsClicked)
        btnRecordQuickActionsContinue.setOnClick(viewModel::onContinueClicked)
        btnRecordQuickActionsRepeat.setOnClick(viewModel::onRepeatClicked)
        btnRecordQuickActionsDuplicate.setOnClick(viewModel::onDuplicateClicked)
        btnRecordQuickActionsMerge.setOnClick(viewModel::onMergeClicked)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        state.observe(::updateState)
        buttonsEnabled.observe(::updateButtonsEnabled)
        requestUpdate.observe { requestUpdate() }
        prepareRemoveRecordViewModel()
    }

    private fun updateState(state: RecordQuickActionsState) = with(binding) {
        fun CardView.setButtonState(state: RecordQuickActionsState.Button?) {
            if (state != null) {
                this.visible = true
                (layoutParams as? FlexboxLayout.LayoutParams)?.apply {
                    isWrapBefore = state.wrapBefore
                }
            } else {
                this.visible = false
            }
        }

        getButtonsList().forEach { (view, clazz) ->
            state.buttons.firstOrNull { it::class.java == clazz }.let(view::setButtonState)
        }
    }

    private fun updateButtonsEnabled(isEnabled: Boolean) {
        getButtonsList().forEach { (view, _) ->
            view.isEnabled = isEnabled
        }
    }

    private fun prepareRemoveRecordViewModel() {
        val recordId = (params.type as? RecordQuickActionsParams.Type.RecordTracked)?.id.orZero()
        removeRecordViewModel.prepare(recordId)
    }

    private fun requestUpdate() {
        listener?.onUpdate()
    }

    private fun getButtonsList(): List<Pair<CardView, Class<out RecordQuickActionsState.Button>>> = with(binding) {
        return listOf(
            btnRecordQuickActionsStatistics to RecordQuickActionsState.Button.Statistics::class.java,
            btnRecordQuickActionsDelete to RecordQuickActionsState.Button.Delete::class.java,
            btnRecordQuickActionsContinue to RecordQuickActionsState.Button.Continue::class.java,
            btnRecordQuickActionsRepeat to RecordQuickActionsState.Button.Repeat::class.java,
            btnRecordQuickActionsDuplicate to RecordQuickActionsState.Button.Duplicate::class.java,
            btnRecordQuickActionsMerge to RecordQuickActionsState.Button.Merge::class.java,
        )
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: RecordQuickActionsParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}