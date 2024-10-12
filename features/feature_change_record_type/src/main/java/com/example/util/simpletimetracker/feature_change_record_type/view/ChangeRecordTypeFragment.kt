package com.example.util.simpletimetracker.feature_change_record_type.view

import android.animation.ValueAnimator
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.delegates.iconSelection.adapter.createIconSelectionAdapterDelegate
import com.example.util.simpletimetracker.core.delegates.iconSelection.adapter.createIconSelectionCategoryAdapterDelegate
import com.example.util.simpletimetracker.core.delegates.iconSelection.adapter.createIconSelectionCategoryInfoAdapterDelegate
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionScrollViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionSelectorStateViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionStateViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewDelegate.IconSelectionViewDelegate
import com.example.util.simpletimetracker.core.dialog.ColorSelectionDialogListener
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.dialog.EmojiSelectionDialogListener
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.core.extension.addOnBackPressedListener
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setSharedTransitions
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.repo.DeviceRepo
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.core.view.ViewChooserStateDelegate
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAddAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorFavouriteAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorPaletteAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.createDayOfWeekAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.emoji.createEmojiAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.createHintBigAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_goals.api.ChangeRecordTypeGoalsViewData
import com.example.util.simpletimetracker.feature_change_goals.views.GoalsViewDelegate
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeAdditionalState
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeCategoriesViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeChooserState.Additional
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeChooserState.Category
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeChooserState.Closed
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeChooserState.Color
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeChooserState.GoalTime
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeChooserState.Icon
import com.example.util.simpletimetracker.feature_change_record_type.viewModel.ChangeRecordTypeViewModel
import com.example.util.simpletimetracker.feature_views.extension.animateColor
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTypeParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_change_record_type.databinding.ChangeRecordTypeFragmentBinding as Binding

@AndroidEntryPoint
class ChangeRecordTypeFragment :
    BaseFragment<Binding>(),
    DurationDialogListener,
    EmojiSelectionDialogListener,
    ColorSelectionDialogListener,
    StandardDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.root }

    @Inject
    lateinit var deviceRepo: DeviceRepo

    private val viewModel: ChangeRecordTypeViewModel by viewModels()

    private val colorsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createColorAdapterDelegate(viewModel::onColorClick),
            createColorPaletteAdapterDelegate(viewModel::onColorPaletteClick),
            createColorFavouriteAdapterDelegate(viewModel::onColorFavouriteClick),
            createHintAdapterDelegate(),
        )
    }
    private val iconsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createIconSelectionAdapterDelegate(viewModel::onIconClick),
            createEmojiAdapterDelegate(viewModel::onEmojiClick),
            createIconSelectionCategoryInfoAdapterDelegate(),
        )
    }
    private val iconCategoriesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createIconSelectionCategoryAdapterDelegate {
                viewModel.onIconCategoryClick(it)
                binding.containerChangeRecordTypeIcon.rvIconSelection.stopScroll()
            },
        )
    }
    private val categoriesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createCategoryAdapterDelegate(
                onClick = viewModel::onCategoryClick,
                onLongClickWithTransition = viewModel::onCategoryLongClick,
            ),
            createCategoryAddAdapterDelegate { throttle(viewModel::onAddCategoryClick).invoke() },
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createHintAdapterDelegate(),
            createHintBigAdapterDelegate(),
            createEmptyAdapterDelegate(),
        )
    }
    private val dailyGoalDayOfWeekAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createDayOfWeekAdapterDelegate(viewModel::onDayOfWeekClick),
        )
    }
    private var iconsLayoutManager: GridLayoutManager? = null
    private var typeColorAnimator: ValueAnimator? = null
    private var iconTextWatcher: TextWatcher? = null
    private var goalTextWatchers: GoalsViewDelegate.TextWatchers? = null

    private val params: ChangeRecordTypeParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS,
        default = ChangeRecordTypeParams.New(ChangeRecordTypeParams.SizePreview()),
    )

    override fun initUi(): Unit = with(binding) {
        postponeEnterTransition()

        setPreview()

        setSharedTransitions(
            additionalCondition = { params !is ChangeRecordTypeParams.New },
            transitionName = (params as? ChangeRecordTypeParams.Change)?.transitionName.orEmpty(),
            sharedView = previewChangeRecordType,
        )

        rvChangeRecordTypeColor.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = colorsAdapter
        }

        iconsLayoutManager = IconSelectionViewDelegate.initUi(
            context = requireContext(),
            resources = resources,
            deviceRepo = deviceRepo,
            layout = containerChangeRecordTypeIcon,
            iconsAdapter = iconsAdapter,
            iconCategoriesAdapter = iconCategoriesAdapter,
        )

        rvChangeRecordTypeCategories.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = categoriesAdapter
        }

        GoalsViewDelegate.initGoalUi(
            layout = binding.layoutChangeRecordTypeGoals,
            dayOfWeekAdapter = dailyGoalDayOfWeekAdapter,
        )

        setOnPreDrawListener {
            startPostponedEnterTransition()
        }
    }

    override fun initUx(): Unit = with(binding) {
        etChangeRecordTypeName.doAfterTextChanged { viewModel.onNameChange(it.toString()) }
        etChangeRecordTypeNote.doAfterTextChanged { viewModel.onNoteChange(it.toString()) }
        fieldChangeRecordTypeColor.setOnClick(viewModel::onColorChooserClick)
        fieldChangeRecordTypeIcon.setOnClick(viewModel::onIconChooserClick)
        fieldChangeRecordTypeCategory.setOnClick(viewModel::onCategoryChooserClick)
        fieldChangeRecordTypeGoalTime.setOnClick(viewModel::onGoalTimeChooserClick)
        fieldChangeRecordTypeAdditional.setOnClick(viewModel::onAdditionalChooserClick)
        btnChangeRecordTypeSave.setOnClick(viewModel::onSaveClick)
        btnChangeRecordTypeArchive.setOnClick(viewModel::onArchiveClick)
        btnChangeRecordTypeDelete.setOnClick(throttle(viewModel::onDeleteClick))
        btnChangeRecordTypeStatistics.setOnClick(viewModel::onStatisticsClick)
        layoutChangeRecordTypeAdditional.btnChangeRecordTypeAdditionalDuplicate
            .setOnClick(viewModel::onDuplicateClick)
        layoutChangeRecordTypeAdditional.groupChangeRecordTypeAdditionalDefaultDurationSelector
            .setOnClick(viewModel::onDefaultDurationClick)
        IconSelectionViewDelegate.initUx(
            viewModel = viewModel,
            layout = containerChangeRecordTypeIcon,
            iconsLayoutManager = iconsLayoutManager,
        )
        GoalsViewDelegate.initGoalUx(
            viewModel = viewModel,
            layout = layoutChangeRecordTypeGoals,
        )
        addOnBackPressedListener(action = viewModel::onBackPressed)
    }

    override fun initViewModel(): Unit = with(binding) {
        with(viewModel) {
            extra = params
            archiveIconVisibility.observeOnce(viewLifecycleOwner, btnChangeRecordTypeArchive::isVisible::set)
            deleteIconVisibility.observeOnce(viewLifecycleOwner, btnChangeRecordTypeDelete::isVisible::set)
            statsIconVisibility.observeOnce(viewLifecycleOwner, btnChangeRecordTypeStatistics::isVisible::set)
            saveButtonEnabled.observe(btnChangeRecordTypeSave::setEnabled)
            archiveButtonEnabled.observe(btnChangeRecordTypeArchive::setEnabled)
            deleteButtonEnabled.observe(btnChangeRecordTypeDelete::setEnabled)
            recordType.observeOnce(viewLifecycleOwner, ::updateUi)
            recordType.observe(::updatePreview)
            colors.observe(colorsAdapter::replace)
            icons.observe(::updateIconsState)
            iconCategories.observe(::updateIconCategories)
            iconsTypeViewData.observe(::updateIconsTypeViewData)
            iconSelectorViewData.observe(::updateIconSelectorViewData)
            expandIconTypeSwitch.observe { updateBarExpanded() }
            categories.observe(::updateCategories)
            goalsViewData.observe(::updateGoalsState)
            nameErrorMessage.observe(::updateNameErrorMessage)
            additionalState.observe(::updateAdditionalState)
            noteState.observe(::updateNoteState)
            duplicateButtonEnabled.observe(
                layoutChangeRecordTypeAdditional.btnChangeRecordTypeAdditionalDuplicate::setEnabled,
            )
            notificationsHintVisible.observe(
                layoutChangeRecordTypeGoals.containerChangeRecordTypeGoalNotificationsHint::visible::set,
            )
            chooserState.observe(::updateChooserState)
            keyboardVisibility.observe { visible ->
                if (visible) showKeyboard(etChangeRecordTypeName) else hideKeyboard()
            }
            iconsScrollPosition.observe {
                if (it is IconSelectionScrollViewData.ScrollTo) {
                    iconsLayoutManager?.scrollToPositionWithOffset(it.position, 0)
                    onScrolled()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
        goalTextWatchers = GoalsViewDelegate.onResume(
            layout = binding.layoutChangeRecordTypeGoals,
            viewModel = viewModel,
        )
    }

    override fun onPause() {
        GoalsViewDelegate.onPause(
            layout = binding.layoutChangeRecordTypeGoals,
            textWatchers = goalTextWatchers,
        )
        super.onPause()
    }

    override fun onDestroyView() {
        IconSelectionViewDelegate.onDestroyView(
            textWatcher = iconTextWatcher,
            layout = binding.containerChangeRecordTypeIcon,
        )
        super.onDestroyView()
    }

    override fun onDestroy() {
        typeColorAnimator?.cancel()
        super.onDestroy()
    }

    override fun onDurationSet(durationSeconds: Long, tag: String?) {
        viewModel.onDurationSet(
            tag = tag,
            duration = durationSeconds,
            anchor = binding.btnChangeRecordTypeSave,
        )
    }

    override fun onDisable(tag: String?) {
        viewModel.onDurationDisabled(tag)
    }

    override fun onEmojiSelected(emojiText: String) {
        viewModel.onEmojiSelected(emojiText)
    }

    override fun onColorSelected(colorInt: Int) {
        viewModel.onCustomColorSelected(colorInt)
    }

    override fun onPositiveClick(tag: String?, data: Any?) {
        viewModel.onPositiveDialogClick(tag)
    }

    private fun updateUi(item: RecordTypeViewData) = with(binding) {
        etChangeRecordTypeName.setText(item.name)
        etChangeRecordTypeName.setSelection(item.name.length)
        iconTextWatcher = IconSelectionViewDelegate.updateUi(
            icon = item.iconId,
            viewModel = viewModel,
            layout = containerChangeRecordTypeIcon,
        )
    }

    private fun updatePreview(item: RecordTypeViewData) {
        with(binding.previewChangeRecordType) {
            itemName = item.name
            itemIcon = item.iconId

            typeColorAnimator?.cancel()
            typeColorAnimator = animateColor(
                from = itemColor,
                to = item.color,
                doOnUpdate = { value ->
                    itemColor = value
                    binding.layoutChangeRecordTypeColorPreview.setCardBackgroundColor(value)
                },
            )
        }
        with(binding) {
            layoutChangeRecordTypeIconPreview.setCardBackgroundColor(item.color)
            iconChangeRecordTypeIconPreview.itemIcon = item.iconId
            layoutChangeRecordTypeCategoriesPreview.setCardBackgroundColor(item.color)
            layoutChangeRecordTypeGoalPreview.setCardBackgroundColor(item.color)
        }
    }

    private fun setPreview() {
        with(binding.previewChangeRecordType) {
            itemIsRow = params.sizePreview.asRow
            layoutParams = layoutParams.also { layoutParams ->
                params.sizePreview.width?.dpToPx()?.let { layoutParams.width = it }
                params.sizePreview.height?.dpToPx()?.let { layoutParams.height = it }
            }

            (params as? ChangeRecordTypeParams.Change)?.preview?.let {
                itemName = it.name
                itemIcon = it.iconId.toViewData()
                itemColor = it.color

                binding.layoutChangeRecordTypeColorPreview.setCardBackgroundColor(it.color)
                binding.layoutChangeRecordTypeIconPreview.setCardBackgroundColor(it.color)
                binding.iconChangeRecordTypeIconPreview.itemIcon = it.iconId.toViewData()
                binding.layoutChangeRecordTypeCategoriesPreview.setCardBackgroundColor(it.color)
                binding.layoutChangeRecordTypeGoalPreview.setCardBackgroundColor(it.color)
            }
        }
    }

    private fun updateChooserState(
        state: ViewChooserStateDelegate.States,
    ) = with(binding) {
        ViewChooserStateDelegate.updateChooser<Color>(
            state = state,
            chooserData = rvChangeRecordTypeColor,
            chooserView = fieldChangeRecordTypeColor,
            chooserArrow = arrowChangeRecordTypeColor,
        )
        ViewChooserStateDelegate.updateChooser<Icon>(
            state = state,
            chooserData = containerChangeRecordTypeIcon.root,
            chooserView = fieldChangeRecordTypeIcon,
            chooserArrow = arrowChangeRecordTypeIcon,
        )
        ViewChooserStateDelegate.updateChooser<Category>(
            state = state,
            chooserData = rvChangeRecordTypeCategories,
            chooserView = fieldChangeRecordTypeCategory,
            chooserArrow = arrowChangeRecordTypeCategory,
        )
        ViewChooserStateDelegate.updateChooser<GoalTime>(
            state = state,
            chooserData = containerChangeRecordTypeGoalTime,
            chooserView = fieldChangeRecordTypeGoalTime,
            chooserArrow = arrowChangeRecordTypeGoalTime,
        )
        ViewChooserStateDelegate.updateChooser<Additional>(
            state = state,
            chooserData = containerChangeRecordTypeAdditional,
            chooserView = fieldChangeRecordTypeAdditional,
            chooserArrow = arrowChangeRecordTypeAdditional,
        )

        val isClosed = state.current is Closed
        inputChangeRecordTypeName.isVisible = isClosed
        btnChangeRecordTypeStatistics.isVisible =
            viewModel.statsIconVisibility.value.orFalse() && isClosed
        btnChangeRecordTypeArchive.isVisible =
            viewModel.archiveIconVisibility.value.orFalse() && isClosed
        btnChangeRecordTypeDelete.isVisible =
            viewModel.deleteIconVisibility.value.orFalse() && isClosed
        inputChangeRecordTypeNote.isVisible = isClosed
        dividerChangeRecordTypeBottom.isInvisible = isClosed

        // Chooser fields
        fieldChangeRecordTypeColor.isVisible = isClosed || state.current is Color
        fieldChangeRecordTypeIcon.isVisible = isClosed || state.current is Icon
        fieldChangeRecordTypeCategory.isVisible = isClosed || state.current is Category
        fieldChangeRecordTypeGoalTime.isVisible = isClosed || state.current is GoalTime
        fieldChangeRecordTypeAdditional.isVisible = isClosed || state.current is Additional
    }

    private fun updateGoalsState(state: ChangeRecordTypeGoalsViewData) = with(binding) {
        GoalsViewDelegate.updateGoalsState(
            state = state,
            layout = layoutChangeRecordTypeGoals,
        )
        layoutChangeRecordTypeGoalPreview.isVisible = state.selectedCount > 0
        tvChangeRecordTypeGoalPreview.text = state.selectedCount.toString()
    }

    private fun updateCategories(
        data: ChangeRecordTypeCategoriesViewData,
    ) = with(binding) {
        categoriesAdapter.replace(data.viewData)
        layoutChangeRecordTypeCategoriesPreview.isVisible = data.selectedCount > 0
        tvChangeRecordTypeCategoryPreview.text = data.selectedCount.toString()
    }

    private fun updateBarExpanded() {
        IconSelectionViewDelegate.updateBarExpanded(
            layout = binding.containerChangeRecordTypeIcon,
        )
    }

    private fun updateIconsState(state: IconSelectionStateViewData) {
        IconSelectionViewDelegate.updateIconsState(
            state = state,
            layout = binding.containerChangeRecordTypeIcon,
            iconsAdapter = iconsAdapter,
        )
    }

    private fun updateIconCategories(data: List<ViewHolderType>) {
        IconSelectionViewDelegate.updateIconCategories(
            data = data,
            iconCategoriesAdapter = iconCategoriesAdapter,
        )
    }

    private fun updateIconSelectorViewData(
        data: IconSelectionSelectorStateViewData,
    ) {
        IconSelectionViewDelegate.updateIconSelectorViewData(
            data = data,
            layout = binding.containerChangeRecordTypeIcon,
        )
    }

    private fun updateIconsTypeViewData(data: List<ViewHolderType>) {
        IconSelectionViewDelegate.updateIconsTypeViewData(
            data = data,
            layout = binding.containerChangeRecordTypeIcon,
        )
    }

    private fun updateNameErrorMessage(error: String) = with(binding) {
        inputChangeRecordTypeName.error = error
        inputChangeRecordTypeName.isErrorEnabled = error.isNotEmpty()
    }

    private fun updateNoteState(text: String) = with(binding) {
        if (etChangeRecordTypeNote.text.toString() != text) {
            etChangeRecordTypeNote.setText(text)
        }
    }

    private fun updateAdditionalState(
        data: ChangeRecordTypeAdditionalState,
    ) = with(binding.layoutChangeRecordTypeAdditional) {
        tvChangeRecordTypeAdditionalDuplicateHint.isVisible = data.isDuplicateVisible
        btnChangeRecordTypeAdditionalDuplicate.isVisible = data.isDuplicateVisible
        viewChangeRecordTypeAdditionalDuplicateDivider.isVisible = data.isDuplicateVisible

        tvChangeRecordTypeAdditionalDefaultDurationSelectorValue.text = data.defaultDuration
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: ChangeRecordTypeParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}