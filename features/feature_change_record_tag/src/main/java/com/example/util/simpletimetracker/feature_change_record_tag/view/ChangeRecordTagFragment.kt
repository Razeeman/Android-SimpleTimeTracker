package com.example.util.simpletimetracker.feature_change_record_tag.view

import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.delegates.iconSelection.adapter.createIconSelectionAdapterDelegate
import com.example.util.simpletimetracker.core.delegates.iconSelection.adapter.createIconSelectionCategoryAdapterDelegate
import com.example.util.simpletimetracker.core.delegates.iconSelection.adapter.createIconSelectionCategoryInfoAdapterDelegate
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewDelegate.IconSelectionViewDelegate
import com.example.util.simpletimetracker.core.dialog.ColorSelectionDialogListener
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.dialog.EmojiSelectionDialogListener
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.core.dialog.TypesSelectionDialogListener
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
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.createButtonsRowAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorFavouriteAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorPaletteAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.commentField.createCommentFieldAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.emoji.createEmojiAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.createHintBigAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_change_goals.api.ChangeRecordTypeGoalsViewData
import com.example.util.simpletimetracker.feature_change_goals.views.GoalsViewDelegate
import com.example.util.simpletimetracker.feature_change_record_tag.R
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagChooserState.Closed
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagChooserState.Color
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagChooserState.DefaultType
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagChooserState.GoalTime
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagChooserState.Icon
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagChooserState.Type
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagChooserState.ValueType
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagFieldsState
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagTypesViewData
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagValueViewData
import com.example.util.simpletimetracker.feature_change_record_tag.viewModel.ChangeRecordTagViewModel
import com.example.util.simpletimetracker.feature_views.extension.animateColor
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.setCompoundDrawableWithIntrinsicBounds
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromScreen
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_change_record_tag.databinding.ChangeRecordTagFragmentBinding as Binding

@AndroidEntryPoint
class ChangeRecordTagFragment :
    BaseFragment<Binding>(),
    EmojiSelectionDialogListener,
    ColorSelectionDialogListener,
    TypesSelectionDialogListener,
    StandardDialogListener,
    DurationDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.root }

    @Inject
    lateinit var deviceRepo: DeviceRepo

    private val viewModel: ChangeRecordTagViewModel by viewModels()

    private val colorsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createColorAdapterDelegate(viewModel::onColorClick),
            createColorPaletteAdapterDelegate(viewModel::onColorPaletteClick),
            createColorFavouriteAdapterDelegate(viewModel::onColorFavouriteClick),
            createHintAdapterDelegate(),
        )
    }
    private val iconsAdapter: BaseRecyclerAdapter by lazy {
        // TODO move creation to delegate
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
    private val typesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onTypeClick),
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createHintBigAdapterDelegate(),
        )
    }
    private val defaultTypesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onDefaultTypeClick),
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createHintBigAdapterDelegate(),
        )
    }
    private val dailyGoalDayOfWeekAdapter: BaseRecyclerAdapter by lazy {
        GoalsViewDelegate.getDayOfWeekAdapter(viewModel)
    }
    private val valueStateAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createHintAdapterDelegate(),
            createDividerAdapterDelegate(),
            createButtonsRowAdapterDelegate(viewModel::onButtonsRowClick),
            createCommentFieldAdapterDelegate(viewModel::onValueChange),
        )
    }
    private var iconsLayoutManager: GridLayoutManager? = null
    private var typeColorAnimator: ValueAnimator? = null
    private var iconTextWatcher: TextWatcher? = null
    private var goalTextWatchers: GoalsViewDelegate.TextWatchers? = null
    private val colorPreviewGradient = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.LEFT_RIGHT
    }

    private val params: ChangeTagData by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = ChangeTagData.New(),
    )

    override fun initUi(): Unit = with(binding) {
        postponeEnterTransition()

        setPreview()

        setSharedTransitions(
            additionalCondition = { params !is ChangeTagData.New },
            transitionName = (params as? ChangeTagData.Change)?.transitionName.orEmpty(),
            sharedView = previewChangeRecordTag,
        )

        rvChangeRecordTagColor.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = colorsAdapter
        }

        containerChangeRecordTypeIcon.btnIconSelectionNoIcon.isVisible = true
        iconsLayoutManager = IconSelectionViewDelegate.initUi(
            context = requireContext(),
            resources = resources,
            deviceRepo = deviceRepo,
            layout = containerChangeRecordTypeIcon,
            iconsAdapter = iconsAdapter,
            iconCategoriesAdapter = iconCategoriesAdapter,
        )

        rvChangeRecordTagType.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = typesAdapter
        }

        rvChangeRecordTagDefaultType.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = defaultTypesAdapter
        }

        rvChangeRecordTagValueType.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = valueStateAdapter
        }

        GoalsViewDelegate.initGoalUi(
            layout = binding.layoutChangeRecordTagGoals,
            dayOfWeekAdapter = dailyGoalDayOfWeekAdapter,
        )

        setOnPreDrawListener {
            startPostponedEnterTransition()
        }
    }

    override fun initUx(): Unit = with(binding) {
        etChangeRecordTagName.doAfterTextChanged { viewModel.onNameChange(it.toString()) }
        etChangeRecordTagNote.doAfterTextChanged { viewModel.onNoteChange(it.toString()) }
        fieldChangeRecordTagColor.setOnClick(viewModel::onColorChooserClick)
        fieldChangeRecordTagIcon.setOnClick(viewModel::onIconChooserClick)
        fieldChangeRecordTagType.setOnClick(viewModel::onTypeChooserClick)
        fieldChangeRecordTagDefaultType.setOnClick(viewModel::onDefaultTypeChooserClick)
        fieldChangeRecordTagGoalTime.setOnClick(viewModel::onGoalTimeChooserClick)
        fieldChangeRecordTagValueType.setOnClick(viewModel::onValueTypeChooserClick)
        btnChangeRecordTagSelectActivity.setOnClick(viewModel::onSelectActivityClick)
        btnChangeRecordTagSave.setOnClick(viewModel::onSaveClick)
        btnChangeRecordTagArchive.setOnClick(viewModel::onArchiveClick)
        btnChangeRecordTagDelete.setOnClick(throttle(viewModel::onDeleteClick))
        btnChangeRecordTagStatistics.setOnClick(viewModel::onStatisticsClick)
        tvChangeRecordTagMoreFields.setOnClick(viewModel::onMoreFieldsClick)
        containerChangeRecordTypeIcon.btnIconSelectionNoIcon.setOnClick(viewModel::onNoIconClick)
        IconSelectionViewDelegate.initUx(
            viewModel = viewModel,
            layout = containerChangeRecordTypeIcon,
            iconsLayoutManager = iconsLayoutManager,
        )
        GoalsViewDelegate.initGoalUx(
            viewModel = viewModel,
            layout = layoutChangeRecordTagGoals,
        )
        addOnBackPressedListener(action = viewModel::onBackPressed)
    }

    override fun initViewModel(): Unit = with(binding) {
        with(viewModel) {
            extra = params
            archiveIconVisibility.observeOnce(viewLifecycleOwner, btnChangeRecordTagArchive::visible::set)
            deleteIconVisibility.observeOnce(viewLifecycleOwner, btnChangeRecordTagDelete::visible::set)
            statsIconVisibility.observeOnce(viewLifecycleOwner, btnChangeRecordTagStatistics::isVisible::set)
            saveButtonEnabled.observe(btnChangeRecordTagSave::setEnabled)
            archiveButtonEnabled.observe(btnChangeRecordTagArchive::setEnabled)
            deleteButtonEnabled.observe(btnChangeRecordTagDelete::setEnabled)
            iconColorSourceSelected.observe(::updateIconColorSourceSelected)
            preview.observeOnce(viewLifecycleOwner, ::updateUi)
            preview.observe(::updatePreview)
            colors.observe(colorsAdapter::replace)
            types.observe(::updateTypes)
            defaultTypes.observe(::updateDefaultTypes)
            goalsViewData.observe(::updateGoalsState)
            valueState.observe(::updateValueState)
            chooserState.observe(::updateChooserState)
            nameErrorMessage.observe(::updateNameErrorMessage)
            noteState.observe(::updateNoteState)
            notificationsHintVisible.observe(
                layoutChangeRecordTagGoals.containerChangeRecordTypeGoalNotificationsHint::visible::set,
            )
            keyboardVisibility.observe { visible ->
                if (visible) showKeyboard(etChangeRecordTagName) else hideKeyboard()
            }
            IconSelectionViewDelegate.initViewModel(
                fragment = this@ChangeRecordTagFragment,
                viewModel = viewModel,
                layout = containerChangeRecordTypeIcon,
                iconsAdapter = iconsAdapter,
                iconCategoriesAdapter = iconCategoriesAdapter,
                iconsLayoutManager = iconsLayoutManager,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        goalTextWatchers = GoalsViewDelegate.onResume(
            layout = binding.layoutChangeRecordTagGoals,
            viewModel = viewModel,
        )
    }

    override fun onPause() {
        GoalsViewDelegate.onPause(
            layout = binding.layoutChangeRecordTagGoals,
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

    override fun onEmojiSelected(emojiText: String) {
        viewModel.onEmojiSelected(emojiText)
    }

    override fun onColorSelected(colorInt: Int) {
        viewModel.onCustomColorSelected(colorInt)
    }

    override fun onDurationSet(durationSeconds: Long, tag: String?) {
        viewModel.onGoalDurationSet(
            tag = tag,
            duration = durationSeconds,
            anchor = binding.btnChangeRecordTagSave,
        )
    }

    override fun onDisable(tag: String?) {
        viewModel.onGoalDurationDisabled(tag)
    }

    override fun onDataSelected(
        tag: String?,
        dataIds: List<Long>,
        tagValues: List<RecordBase.Tag>,
    ) {
        viewModel.onTypesSelected(dataIds, tag)
    }

    override fun onPositiveClick(tag: String?, data: Any?) {
        viewModel.onPositiveDialogClick(tag)
    }

    private fun updateUi(item: CategoryViewData.Record) = with(binding) {
        etChangeRecordTagName.setText(item.name)
        etChangeRecordTagName.setSelection(item.name.length)
        iconTextWatcher = IconSelectionViewDelegate.updateUi(
            icon = item.icon,
            viewModel = viewModel,
            layout = containerChangeRecordTypeIcon,
        )
    }

    private fun setPreview() {
        with(binding.previewChangeRecordTag) {
            (params as? ChangeTagData.Change)?.preview?.let {
                itemName = it.name
                itemColor = it.color
                val icon = it.icon
                if (icon != null) {
                    itemIconVisible = true
                    itemIcon = icon.toViewData()
                } else {
                    itemIconVisible = false
                }

                updateColorPreview(it.color)
                updateIconPreview(icon?.toViewData(), it.color)
                binding.layoutChangeRecordTagTypesPreview.setCardBackgroundColor(it.color)
                binding.layoutChangeRecordTagDefaultTypePreview.setCardBackgroundColor(it.color)
                binding.layoutChangeRecordTagValueTypePreview.setCardBackgroundColor(it.color)
                binding.layoutChangeRecordTagGoalPreview.setCardBackgroundColor(it.color)
            }
        }
    }

    private fun updatePreview(item: CategoryViewData.Record) {
        with(binding.previewChangeRecordTag) {
            itemName = item.name
            val icon = item.icon
            if (icon != null) {
                itemIconVisible = true
                itemIcon = icon
            } else {
                itemIconVisible = false
            }

            typeColorAnimator?.cancel()
            typeColorAnimator = animateColor(
                from = itemColor,
                to = item.color,
                doOnUpdate = { value ->
                    itemColor = value
                    updateColorPreview(value)
                },
            )
        }

        with(binding) {
            updateIconPreview(item.icon, item.color)
            layoutChangeRecordTagTypesPreview.setCardBackgroundColor(item.color)
            layoutChangeRecordTagDefaultTypePreview.setCardBackgroundColor(item.color)
            layoutChangeRecordTagValueTypePreview.setCardBackgroundColor(item.color)
            layoutChangeRecordTagGoalPreview.setCardBackgroundColor(item.color)
        }
    }

    private fun updateChooserState(
        fieldsState: ChangeRecordTagFieldsState,
    ) = with(binding) {
        val state = fieldsState.chooserState
        ViewChooserStateDelegate.updateChooser<Color>(
            state = state,
            chooserData = rvChangeRecordTagColor,
            chooserView = fieldChangeRecordTagColor,
            chooserArrow = arrowChangeRecordTagColor,
        )
        ViewChooserStateDelegate.updateChooser<Icon>(
            state = state,
            chooserData = containerChangeRecordTypeIcon.root,
            chooserView = fieldChangeRecordTagIcon,
            chooserArrow = arrowChangeRecordTagIcon,
        )
        ViewChooserStateDelegate.updateChooser<Type>(
            state = state,
            chooserData = rvChangeRecordTagType,
            chooserView = fieldChangeRecordTagType,
            chooserArrow = arrowChangeRecordTagType,
        )
        ViewChooserStateDelegate.updateChooser<DefaultType>(
            state = state,
            chooserData = rvChangeRecordTagDefaultType,
            chooserView = fieldChangeRecordTagDefaultType,
            chooserArrow = arrowChangeRecordTagDefaultType,
        )
        ViewChooserStateDelegate.updateChooser<ValueType>(
            state = state,
            chooserData = rvChangeRecordTagValueType,
            chooserView = fieldChangeRecordTagValueType,
            chooserArrow = arrowChangeRecordTagValueType,
        )
        ViewChooserStateDelegate.updateChooser<GoalTime>(
            state = state,
            chooserData = containerChangeRecordTagGoalTime,
            chooserView = fieldChangeRecordTagGoalTime,
            chooserArrow = arrowChangeRecordTagGoalTime,
        )

        val isClosed = state.current is Closed
        spaceChangeRecordTagFieldsTop.isVisible = !isClosed
        inputChangeRecordTagName.isVisible = isClosed
        btnChangeRecordTagStatistics.isVisible =
            viewModel.statsIconVisibility.value.orFalse() && isClosed
        btnChangeRecordTagArchive.isVisible =
            viewModel.archiveIconVisibility.value.orFalse() && isClosed
        btnChangeRecordTagDelete.isVisible =
            viewModel.deleteIconVisibility.value.orFalse() && isClosed

        // Main fields
        fieldChangeRecordTagColor.isVisible = isClosed || state.current is Color
        fieldChangeRecordTagIcon.isVisible = isClosed || state.current is Icon
        fieldChangeRecordTagType.isVisible = isClosed || state.current is Type

        // Additional fields
        val isAdditionalVisible = fieldsState.additionalFieldsVisible
        containerChangeRecordTagMoreFields.isVisible = isClosed
        fieldChangeRecordTagDefaultType.isVisible = (isAdditionalVisible && isClosed) || state.current is DefaultType
        fieldChangeRecordTagGoalTime.isVisible = (isAdditionalVisible && isClosed) || state.current is GoalTime
        fieldChangeRecordTagValueType.isVisible = (isAdditionalVisible && isClosed) || state.current is ValueType
        btnChangeRecordTagSelectActivity.isVisible = isAdditionalVisible && isClosed
        inputChangeRecordTagNote.isVisible = isAdditionalVisible && isClosed
        dividerChangeRecordTagBottom.isInvisible = isClosed

        // Chooser size
        val sizeDefault = resources.getDimensionPixelSize(R.dimen.input_field_height)
        val sizeBig = resources.getDimensionPixelSize(R.dimen.input_field_height_big)
        val colorSize = if (state.current is Color) sizeDefault else sizeBig
        fieldChangeRecordTagColor.updateLayoutParams { height = colorSize }
        val iconSize = if (state.current is Icon) sizeDefault else sizeBig
        fieldChangeRecordTagIcon.updateLayoutParams { height = iconSize }
        val iconPreviewPadding = if (state.current is Icon) 4 else 8
        iconChangeRecordTagIconPreview.setPadding(iconPreviewPadding.dpToPx())
        val activitiesSize = if (state.current is Type) sizeDefault else sizeBig
        fieldChangeRecordTagType.updateLayoutParams { height = activitiesSize }

        // More button
        val moreButtonText = if (isAdditionalVisible) {
            R.string.change_record_type_less_fields
        } else {
            R.string.change_record_type_more_fields
        }
        tvChangeRecordTagMoreFieldsText.setText(moreButtonText)
    }

    private fun updateIconColorSourceSelected(selected: Boolean) = with(binding) {
        val drawable = R.drawable.spinner_check_mark
            .takeIf { selected }.orZero()
        btnChangeRecordTagSelectActivity
            .setCompoundDrawableWithIntrinsicBounds(right = drawable)
    }

    private fun updateTypes(
        data: ChangeRecordTagTypesViewData,
    ) = with(binding) {
        typesAdapter.replace(data.viewData)
        layoutChangeRecordTagTypesPreview.isVisible = data.selectedCount > 0
        tvChangeRecordTagTypesPreview.text = data.selectedCount.toString()
    }

    private fun updateDefaultTypes(
        data: ChangeRecordTagTypesViewData,
    ) = with(binding) {
        defaultTypesAdapter.replace(data.viewData)
        layoutChangeRecordTagDefaultTypePreview.isVisible = data.selectedCount > 0
        tvChangeRecordTagDefaultTypePreview.text = data.selectedCount.toString()
    }

    private fun updateGoalsState(state: ChangeRecordTypeGoalsViewData) = with(binding) {
        GoalsViewDelegate.updateGoalsState(
            state = state,
            layout = layoutChangeRecordTagGoals,
        )
        layoutChangeRecordTagGoalPreview.isVisible = state.selectedCount > 0
        tvChangeRecordTagGoalPreview.text = state.selectedCount.toString()
    }

    private fun updateValueState(
        data: ChangeRecordTagValueViewData,
    ) = with(binding) {
        valueStateAdapter.replace(data.viewData)
        tvChangeRecordTagValueTypePreview.text = data.hint
    }

    private fun updateNameErrorMessage(error: String) = with(binding) {
        inputChangeRecordTagName.error = error
        inputChangeRecordTagName.isErrorEnabled = error.isNotEmpty()
    }

    private fun updateNoteState(text: String) = with(binding) {
        if (etChangeRecordTagNote.text.toString() != text) {
            etChangeRecordTagNote.setText(text)
        }
    }

    private fun updateColorPreview(@ColorInt color: Int) = with(binding) {
        colorPreviewGradient.colors = intArrayOf(android.graphics.Color.TRANSPARENT, color)
        layoutChangeRecordTagColorPreview.setCardBackgroundColor(color)
        viewChangeRecordTagColorPreviewLong.background = colorPreviewGradient
    }

    private fun updateIconPreview(
        iconId: RecordTypeIcon?,
        @ColorInt color: Int,
    ) = with(binding) {
        if (iconId != null) {
            binding.layoutChangeRecordTagIconPreview.isVisible = true
            binding.viewChangeRecordTagIconPreviewLong.isVisible = true

            layoutChangeRecordTagIconPreview.setCardBackgroundColor(color)
            iconChangeRecordTagIconPreview.itemIcon = iconId
            viewChangeRecordTagIconPreviewLong.setIcon(iconId)
        } else {
            binding.layoutChangeRecordTagIconPreview.isVisible = false
            binding.viewChangeRecordTagIconPreviewLong.isVisible = false
        }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: ChangeRecordTagFromScreen): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data.params)
        }
    }
}