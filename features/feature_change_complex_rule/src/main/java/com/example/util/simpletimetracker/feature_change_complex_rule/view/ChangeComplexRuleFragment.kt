package com.example.util.simpletimetracker.feature_change_complex_rule.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.dialog.TypesSelectionDialogListener
import com.example.util.simpletimetracker.core.extension.addOnBackPressedListener
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.core.view.ViewChooserStateDelegate
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.createDayOfWeekAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_change_complex_rule.adapter.createComplexRuleActionAdapterDelegate
import com.example.util.simpletimetracker.feature_change_complex_rule.viewData.ChangeComplexRuleActionChooserViewData
import com.example.util.simpletimetracker.feature_change_complex_rule.viewData.ChangeComplexRuleChooserState.Action
import com.example.util.simpletimetracker.feature_change_complex_rule.viewData.ChangeComplexRuleChooserState.Closed
import com.example.util.simpletimetracker.feature_change_complex_rule.viewData.ChangeComplexRuleChooserState.CurrentTypes
import com.example.util.simpletimetracker.feature_change_complex_rule.viewData.ChangeComplexRuleChooserState.DayOfWeek
import com.example.util.simpletimetracker.feature_change_complex_rule.viewData.ChangeComplexRuleChooserState.StartingTypes
import com.example.util.simpletimetracker.feature_change_complex_rule.viewData.ChangeComplexRuleTypesChooserViewData
import com.example.util.simpletimetracker.feature_change_complex_rule.viewModel.ChangeComplexRuleViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.ChangeComplexRuleParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_change_complex_rule.databinding.ChangeComplexRuleFragmentBinding as Binding

@AndroidEntryPoint
class ChangeComplexRuleFragment :
    BaseFragment<Binding>(),
    TypesSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.root }

    private val viewModel: ChangeComplexRuleViewModel by viewModels()

    private val actionAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createHintAdapterDelegate(),
            createComplexRuleActionAdapterDelegate(viewModel::onActionClick),
        )
    }
    private val startingTypesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createInfoAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createDividerAdapterDelegate(),
            createRecordTypeAdapterDelegate(viewModel::onStartingTypeClick),
        )
    }
    private val currentTypesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createInfoAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createDividerAdapterDelegate(),
            createRecordTypeAdapterDelegate(viewModel::onCurrentTypeClick),
        )
    }
    private val dayOfWeekAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createDayOfWeekAdapterDelegate(viewModel::onDayOfWeekClick),
        )
    }

    private val params: ChangeComplexRuleParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = ChangeComplexRuleParams.New,
    )

    override fun initUi(): Unit = with(binding) {
        rvChangeComplexRuleActionType.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = actionAdapter
        }
        rvChangeComplexRuleStartingTypes.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = startingTypesAdapter
        }
        rvChangeComplexRuleCurrentTypes.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = currentTypesAdapter
        }
        rvChangeComplexRuleDaysOfWeek.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.NOWRAP
            }
            adapter = dayOfWeekAdapter
        }
    }

    override fun initUx(): Unit = with(binding) {
        fieldChangeComplexRuleAction.setOnClick(viewModel::onActionTypeChooserClick)
        fieldChangeComplexRuleStartingTypes.setOnClick(viewModel::onStartingTypesChooserClick)
        fieldChangeComplexRuleCurrentTypes.setOnClick(viewModel::onCurrentTypesChooserClick)
        fieldChangeComplexRuleDaysOfWeek.setOnClick(viewModel::onDaysOfWeekChooserClick)
        btnChangeComplexRuleSave.setOnClick(viewModel::onSaveClick)
        btnChangeComplexRuleDelete.setOnClick(viewModel::onDeleteClick)
        addOnBackPressedListener(action = viewModel::onBackPressed)
    }

    override fun initViewModel(): Unit = with(binding) {
        with(viewModel) {
            extra = params
            deleteIconVisibility.observeOnce(viewLifecycleOwner, btnChangeComplexRuleDelete::visible::set)
            saveButtonEnabled.observe(btnChangeComplexRuleSave::setEnabled)
            deleteButtonEnabled.observe(btnChangeComplexRuleDelete::setEnabled)
            actionViewData.observe(::updateAction)
            startingTypesViewData.observe(::updateStartingTypes)
            currentTypesViewData.observe(::updateCurrentTypes)
            daysOfWeekViewData.observe(::updateDaysOfWeek)
            chooserState.observe(::updateChooserState)
            initialize()
        }
    }

    override fun onDataSelected(dataIds: List<Long>, tag: String?) {
        viewModel.onDataSelected(dataIds, tag)
    }

    private fun updateChooserState(
        state: ViewChooserStateDelegate.States,
    ) = with(binding) {
        ViewChooserStateDelegate.updateChooser<Action>(
            state = state,
            chooserData = rvChangeComplexRuleActionType,
            chooserView = fieldChangeComplexRuleAction,
            chooserArrow = arrowChangeComplexRuleAction,
        )
        ViewChooserStateDelegate.updateChooser<StartingTypes>(
            state = state,
            chooserData = rvChangeComplexRuleStartingTypes,
            chooserView = fieldChangeComplexRuleStartingTypes,
            chooserArrow = arrowChangeComplexRuleStartingTypes,
        )
        ViewChooserStateDelegate.updateChooser<CurrentTypes>(
            state = state,
            chooserData = rvChangeComplexRuleCurrentTypes,
            chooserView = fieldChangeComplexRuleCurrentTypes,
            chooserArrow = arrowChangeComplexRuleCurrentTypes,
        )
        ViewChooserStateDelegate.updateChooser<DayOfWeek>(
            state = state,
            chooserData = rvChangeComplexRuleDaysOfWeek,
            chooserView = fieldChangeComplexRuleDaysOfWeek,
            chooserArrow = arrowChangeComplexRuleDaysOfWeek,
        )

        val isClosed = state.current is Closed
        btnChangeComplexRuleDelete.isVisible =
            viewModel.deleteIconVisibility.value.orFalse() && isClosed
        dividerChangeComplexRuleAction.isVisible = isClosed
        tvChangeComplexRuleAction.isVisible = isClosed
        tvChangeComplexRuleConditions.isVisible = isClosed
        dividerChangeComplexRuleBottom.isVisible = !isClosed

        // Chooser fields
        fieldChangeComplexRuleAction.isVisible = isClosed || state.current is Action
        fieldChangeComplexRuleStartingTypes.isVisible = isClosed || state.current is StartingTypes
        fieldChangeComplexRuleCurrentTypes.isVisible = isClosed || state.current is CurrentTypes
        fieldChangeComplexRuleDaysOfWeek.isVisible = isClosed || state.current is DayOfWeek
    }

    private fun updateAction(
        data: ChangeComplexRuleActionChooserViewData,
    ) = with(binding) {
        actionAdapter.replace(data.viewData)
        etComplexRuleAction.text = data.title
        layoutChangeComplexRuleActionPreview.isVisible = data.selectedCount > 0
        tvChangeComplexRuleActionPreview.text = data.selectedCount.toString()
    }

    private fun updateStartingTypes(
        data: ChangeComplexRuleTypesChooserViewData,
    ) = with(binding) {
        startingTypesAdapter.replace(data.viewData)
        layoutChangeComplexRuleStartingTypesPreview.isVisible = data.selectedCount > 0
        tvChangeComplexRuleStartingTypesPreview.text = data.selectedCount.toString()
    }

    private fun updateCurrentTypes(
        data: ChangeComplexRuleTypesChooserViewData,
    ) = with(binding) {
        currentTypesAdapter.replace(data.viewData)
        layoutChangeComplexRuleCurrentTypesPreview.isVisible = data.selectedCount > 0
        tvChangeComplexRuleCurrentTypesPreview.text = data.selectedCount.toString()
    }

    private fun updateDaysOfWeek(
        data: ChangeComplexRuleTypesChooserViewData,
    ) = with(binding) {
        dayOfWeekAdapter.replace(data.viewData)
        layoutChangeComplexRuleDaysOfWeekPreview.isVisible = data.selectedCount > 0
        tvChangeComplexRuleDaysOfWeekPreview.text = data.selectedCount.toString()
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: ChangeComplexRuleParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}