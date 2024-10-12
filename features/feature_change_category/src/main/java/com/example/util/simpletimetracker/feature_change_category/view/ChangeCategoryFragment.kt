package com.example.util.simpletimetracker.feature_change_category.view

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.dialog.ColorSelectionDialogListener
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.extension.addOnBackPressedListener
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setSharedTransitions
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.core.view.ViewChooserStateDelegate
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorFavouriteAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorPaletteAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.createDayOfWeekAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_change_category.viewData.ChangeCategoryTypesViewData
import com.example.util.simpletimetracker.feature_change_category.viewModel.ChangeCategoryViewModel
import com.example.util.simpletimetracker.feature_change_category.viewData.ChangeCategoryChooserState.Closed
import com.example.util.simpletimetracker.feature_change_category.viewData.ChangeCategoryChooserState.Color
import com.example.util.simpletimetracker.feature_change_category.viewData.ChangeCategoryChooserState.GoalTime
import com.example.util.simpletimetracker.feature_change_category.viewData.ChangeCategoryChooserState.Type
import com.example.util.simpletimetracker.feature_change_goals.api.ChangeRecordTypeGoalsViewData
import com.example.util.simpletimetracker.feature_change_goals.views.GoalsViewDelegate
import com.example.util.simpletimetracker.feature_views.extension.animateColor
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

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.root }

    private val viewModel: ChangeCategoryViewModel by viewModels()

    private val colorsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createColorAdapterDelegate(viewModel::onColorClick),
            createColorPaletteAdapterDelegate(viewModel::onColorPaletteClick),
            createColorFavouriteAdapterDelegate(viewModel::onColorFavouriteClick),
            createHintAdapterDelegate(),
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
    private val dailyGoalDayOfWeekAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createDayOfWeekAdapterDelegate(viewModel::onDayOfWeekClick),
        )
    }
    private var typeColorAnimator: ValueAnimator? = null
    private var goalTextWatchers: GoalsViewDelegate.TextWatchers? = null

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

        GoalsViewDelegate.initGoalUi(
            layout = binding.layoutChangeCategoryGoals,
            dayOfWeekAdapter = dailyGoalDayOfWeekAdapter,
        )
    }

    override fun initUx(): Unit = with(binding) {
        etChangeCategoryName.doAfterTextChanged { viewModel.onNameChange(it.toString()) }
        etChangeRecordCategoryNote.doAfterTextChanged { viewModel.onNoteChange(it.toString()) }
        fieldChangeCategoryColor.setOnClick(viewModel::onColorChooserClick)
        fieldChangeCategoryType.setOnClick(viewModel::onTypeChooserClick)
        fieldChangeCategoryGoalTime.setOnClick(viewModel::onGoalTimeChooserClick)
        btnChangeCategorySave.setOnClick(viewModel::onSaveClick)
        btnChangeCategoryDelete.setOnClick(viewModel::onDeleteClick)
        btnChangeCategoryStatistics.setOnClick(viewModel::onStatisticsClick)
        GoalsViewDelegate.initGoalUx(
            viewModel = viewModel,
            layout = layoutChangeCategoryGoals,
        )
        addOnBackPressedListener(action = viewModel::onBackPressed)
    }

    override fun initViewModel(): Unit = with(binding) {
        with(viewModel) {
            extra = params
            deleteIconVisibility.observeOnce(viewLifecycleOwner, btnChangeCategoryDelete::visible::set)
            statsIconVisibility.observeOnce(viewLifecycleOwner, btnChangeCategoryStatistics::isVisible::set)
            saveButtonEnabled.observe(btnChangeCategorySave::setEnabled)
            deleteButtonEnabled.observe(btnChangeCategoryDelete::setEnabled)
            categoryPreview.observeOnce(viewLifecycleOwner, ::updateUi)
            categoryPreview.observe(::updatePreview)
            colors.observe(colorsAdapter::replace)
            types.observe(::updateTypes)
            goalsViewData.observe(::updateGoalsState)
            nameErrorMessage.observe(::updateNameErrorMessage)
            noteState.observe(::updateNoteState)
            notificationsHintVisible.observe(
                layoutChangeCategoryGoals.containerChangeRecordTypeGoalNotificationsHint::visible::set,
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
        goalTextWatchers = GoalsViewDelegate.onResume(
            layout = binding.layoutChangeCategoryGoals,
            viewModel = viewModel,
        )
    }

    override fun onPause() {
        GoalsViewDelegate.onPause(
            layout = binding.layoutChangeCategoryGoals,
            textWatchers = goalTextWatchers,
        )
        super.onPause()
    }

    override fun onDestroy() {
        typeColorAnimator?.cancel()
        super.onDestroy()
    }

    override fun onColorSelected(colorInt: Int) {
        viewModel.onCustomColorSelected(colorInt)
    }

    override fun onDurationSet(durationSeconds: Long, tag: String?) {
        viewModel.onGoalDurationSet(
            tag = tag,
            duration = durationSeconds,
            anchor = binding.btnChangeCategorySave,
        )
    }

    override fun onDisable(tag: String?) {
        viewModel.onGoalDurationDisabled(tag)
    }

    private fun updateUi(item: CategoryViewData) = with(binding) {
        etChangeCategoryName.setText(item.name)
        etChangeCategoryName.setSelection(item.name.length)
    }

    private fun setPreview() {
        with(binding.previewChangeCategory) {
            (params as? ChangeTagData.Change)?.preview?.let {
                itemName = it.name
                itemColor = it.color

                binding.layoutChangeCategoryColorPreview.setCardBackgroundColor(it.color)
                binding.layoutChangeCategoryTypePreview.setCardBackgroundColor(it.color)
                binding.layoutChangeCategoryGoalPreview.setCardBackgroundColor(it.color)
            }
        }
    }

    private fun updatePreview(item: CategoryViewData) {
        with(binding.previewChangeCategory) {
            itemName = item.name

            typeColorAnimator?.cancel()
            typeColorAnimator = animateColor(
                from = itemColor,
                to = item.color,
                doOnUpdate = { value ->
                    itemColor = value
                    binding.layoutChangeCategoryColorPreview.setCardBackgroundColor(value)
                },
            )
        }
        with(binding) {
            layoutChangeCategoryTypePreview.setCardBackgroundColor(item.color)
            layoutChangeCategoryGoalPreview.setCardBackgroundColor(item.color)
        }
    }

    private fun updateChooserState(
        state: ViewChooserStateDelegate.States,
    ) = with(binding) {
        ViewChooserStateDelegate.updateChooser<Color>(
            state = state,
            chooserData = rvChangeCategoryColor,
            chooserView = fieldChangeCategoryColor,
            chooserArrow = arrowChangeCategoryColor,
        )
        ViewChooserStateDelegate.updateChooser<Type>(
            state = state,
            chooserData = rvChangeCategoryType,
            chooserView = fieldChangeCategoryType,
            chooserArrow = arrowChangeCategoryType,
        )
        ViewChooserStateDelegate.updateChooser<GoalTime>(
            state = state,
            chooserData = containerChangeCategoryGoalTime,
            chooserView = fieldChangeCategoryGoalTime,
            chooserArrow = arrowChangeCategoryGoalTime,
        )

        val isClosed = state.current is Closed
        inputChangeCategoryName.isVisible = isClosed
        btnChangeCategoryStatistics.isVisible =
            viewModel.statsIconVisibility.value.orFalse() && isClosed
        btnChangeCategoryDelete.isVisible =
            viewModel.deleteIconVisibility.value.orFalse() && isClosed
        inputChangeRecordCategoryNote.isVisible = isClosed
        dividerChangeCategoryBottom.isInvisible = isClosed

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
        layoutChangeCategoryGoalPreview.isVisible = state.selectedCount > 0
        tvChangeCategoryGoalPreview.text = state.selectedCount.toString()
    }

    private fun updateTypes(
        data: ChangeCategoryTypesViewData,
    ) = with(binding) {
        typesAdapter.replace(data.viewData)
        layoutChangeCategoryTypePreview.isVisible = data.selectedCount > 0
        tvChangeCategoryTypePreview.text = data.selectedCount.toString()
    }

    private fun updateNameErrorMessage(error: String) = with(binding) {
        inputChangeCategoryName.error = error
        inputChangeCategoryName.isErrorEnabled = error.isNotEmpty()
    }

    private fun updateNoteState(text: String) = with(binding) {
        if (etChangeRecordCategoryNote.text.toString() != text) {
            etChangeRecordCategoryNote.setText(text)
        }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: ChangeCategoryFromScreen): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data.params)
        }
    }
}