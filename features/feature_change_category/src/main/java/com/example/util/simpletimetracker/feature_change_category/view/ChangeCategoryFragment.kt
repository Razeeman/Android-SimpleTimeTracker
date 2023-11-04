package com.example.util.simpletimetracker.feature_change_category.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.dialog.ColorSelectionDialogListener
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setSharedTransitions
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorPaletteAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_change_category.viewModel.ChangeCategoryViewModel
import com.example.util.simpletimetracker.feature_change_record_type.goals.GoalsViewDelegate
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeChooserState
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeChooserState.State.Closed
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeChooserState.State.Color
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeChooserState.State.GoalTime
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeChooserState.State.Type
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeGoalsViewData
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.ChangeCategoryFromScreen
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_change_category.databinding.ChangeCategoryFragmentBinding as Binding

@AndroidEntryPoint
class ChangeCategoryFragment :
    BaseFragment<Binding>(),
    ColorSelectionDialogListener,
    DurationDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding = Binding::inflate

    private val viewModel: ChangeCategoryViewModel by viewModels()

    private val colorsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createColorAdapterDelegate(viewModel::onColorClick),
            createColorPaletteAdapterDelegate(viewModel::onColorPaletteClick),
        )
    }
    private val typesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onTypeClick),
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createEmptyAdapterDelegate(),
        )
    }

    private val params: ChangeTagData by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = ChangeTagData.New(),
    )

    override fun initUi(): Unit = with(binding) {
        setPreview()

        setSharedTransitions(
            additionalCondition = { params !is ChangeTagData.New },
            transitionName = (params as? ChangeTagData.Change)?.transitionName.orEmpty(),
            sharedView = previewChangeCategory,
        )

        rvChangeCategoryColor.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = colorsAdapter
        }

        rvChangeCategoryType.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = typesAdapter
        }

        GoalsViewDelegate.initGoalUi(binding.layoutChangeCategoryGoals)
    }

    override fun initUx() = with(binding) {
        etChangeCategoryName.doAfterTextChanged { viewModel.onNameChange(it.toString()) }
        fieldChangeCategoryColor.setOnClick(viewModel::onColorChooserClick)
        fieldChangeCategoryType.setOnClick(viewModel::onTypeChooserClick)
        fieldChangeCategoryGoalTime.setOnClick(viewModel::onGoalTimeChooserClick)
        btnChangeCategorySave.setOnClick(viewModel::onSaveClick)
        btnChangeCategoryDelete.setOnClick(viewModel::onDeleteClick)
        GoalsViewDelegate.initGoalUx(
            viewModel = viewModel,
            layout = layoutChangeCategoryGoals,
        )
    }

    override fun initViewModel(): Unit = with(binding) {
        with(viewModel) {
            extra = params
            deleteIconVisibility.observeOnce(viewLifecycleOwner, btnChangeCategoryDelete::visible::set)
            saveButtonEnabled.observe(btnChangeCategorySave::setEnabled)
            deleteButtonEnabled.observe(btnChangeCategoryDelete::setEnabled)
            categoryPreview.observeOnce(viewLifecycleOwner, ::updateUi)
            categoryPreview.observe(::updatePreview)
            colors.observe(colorsAdapter::replace)
            types.observe(typesAdapter::replace)
            goalsViewData.observe(::updateGoalsState)
            notificationsHintVisible.observe(
                layoutChangeCategoryGoals.containerChangeRecordTypeGoalNotificationsHint::visible::set
            )
            chooserState.observe(::updateChooserState)
            keyboardVisibility.observe { visible ->
                if (visible) showKeyboard(etChangeCategoryName) else hideKeyboard()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
        GoalsViewDelegate.onResume(binding.layoutChangeCategoryGoals)
    }

    override fun onColorSelected(colorInt: Int) {
        viewModel.onCustomColorSelected(colorInt)
    }

    override fun onDurationSet(duration: Long, tag: String?) {
        viewModel.onDurationSet(
            tag = tag,
            duration = duration,
            anchor = binding.btnChangeCategorySave,
        )
    }

    override fun onDisable(tag: String?) {
        viewModel.onDurationDisabled(tag)
    }

    private fun updateUi(item: CategoryViewData) = with(binding) {
        etChangeCategoryName.setText(item.name)
        etChangeCategoryName.setSelection(item.name.length)
    }

    private fun setPreview() = (params as? ChangeTagData.Change)?.preview?.run {
        with(binding.previewChangeCategory) {
            itemName = name
            itemColor = color
        }
    }

    private fun updatePreview(item: CategoryViewData) {
        with(binding.previewChangeCategory) {
            itemName = item.name
            itemColor = item.color
        }
    }

    private fun updateChooserState(state: ChangeRecordTypeChooserState) = with(binding) {
        GoalsViewDelegate.updateChooser<Color>(
            state = state,
            chooserData = rvChangeCategoryColor,
            chooserView = fieldChangeCategoryColor,
            chooserArrow = arrowChangeCategoryColor,
        )
        GoalsViewDelegate.updateChooser<Type>(
            state = state,
            chooserData = rvChangeCategoryType,
            chooserView = fieldChangeCategoryType,
            chooserArrow = arrowChangeCategoryType,
        )
        GoalsViewDelegate.updateChooser<GoalTime>(
            state = state,
            chooserData = containerChangeCategoryGoalTime,
            chooserView = fieldChangeCategoryGoalTime,
            chooserArrow = arrowChangeCategoryGoalTime,
        )

        val isClosed = state.current is Closed
        inputChangeCategoryName.isVisible = isClosed

        // Chooser fields
        fieldChangeCategoryColor.isVisible = isClosed || state.current is Color
        fieldChangeCategoryType.isVisible = isClosed || state.current is Type
        fieldChangeCategoryGoalTime.isVisible = isClosed || state.current is GoalTime
    }

    private fun updateGoalsState(state: ChangeRecordTypeGoalsViewData) = with(binding) {
        GoalsViewDelegate.updateGoalsState(
            state = state,
            layout = layoutChangeCategoryGoals,
        )
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: ChangeCategoryFromScreen): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data.params)
        }
    }
}