package com.example.util.simpletimetracker.feature_change_record_tag.view

import android.animation.ValueAnimator
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.delegates.iconSelection.adapter.createIconSelectionAdapterDelegate
import com.example.util.simpletimetracker.core.delegates.iconSelection.adapter.createIconSelectionCategoryAdapterDelegate
import com.example.util.simpletimetracker.core.delegates.iconSelection.adapter.createIconSelectionCategoryInfoAdapterDelegate
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionSelectorStateViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionStateViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewDelegate.IconSelectionViewDelegate
import com.example.util.simpletimetracker.core.dialog.ColorSelectionDialogListener
import com.example.util.simpletimetracker.core.dialog.EmojiSelectionDialogListener
import com.example.util.simpletimetracker.core.dialog.TypesSelectionDialogListener
import com.example.util.simpletimetracker.core.extension.addOnBackPressedListener
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setSharedTransitions
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.repo.DeviceRepo
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.core.view.UpdateViewChooserState
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorPaletteAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.emoji.createEmojiAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.createHintBigAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record_tag.R
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagChooserState
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagChooserState.State
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagChooserState.State.Closed
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagChooserState.State.Color
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagChooserState.State.DefaultType
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagChooserState.State.Icon
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagChooserState.State.Type
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagTypesViewData
import com.example.util.simpletimetracker.feature_change_record_tag.viewModel.ChangeRecordTagViewModel
import com.example.util.simpletimetracker.feature_views.extension.animateColor
import com.example.util.simpletimetracker.feature_views.extension.setCompoundDrawableWithIntrinsicBounds
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
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
    TypesSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var deviceRepo: DeviceRepo

    private val viewModel: ChangeRecordTagViewModel by viewModels()

    private val colorsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createColorAdapterDelegate(viewModel::onColorClick),
            createColorPaletteAdapterDelegate(viewModel::onColorPaletteClick),
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
    private var iconsLayoutManager: GridLayoutManager? = null
    private var typeColorAnimator: ValueAnimator? = null
    private var iconTextWatcher: TextWatcher? = null

    private val params: ChangeTagData by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = ChangeTagData.New(),
    )

    override fun initUi(): Unit = with(binding) {
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
    }

    override fun initUx(): Unit = with(binding) {
        etChangeRecordTagName.doAfterTextChanged { viewModel.onNameChange(it.toString()) }
        fieldChangeRecordTagColor.setOnClick(viewModel::onColorChooserClick)
        fieldChangeRecordTagIcon.setOnClick(viewModel::onIconChooserClick)
        fieldChangeRecordTagType.setOnClick(viewModel::onTypeChooserClick)
        fieldChangeRecordTagDefaultType.setOnClick(viewModel::onDefaultTypeChooserClick)
        btnChangeRecordTagSelectActivity.setOnClick(viewModel::onSelectActivityClick)
        btnChangeRecordTagSave.setOnClick(viewModel::onSaveClick)
        btnChangeRecordTagDelete.setOnClick(viewModel::onDeleteClick)
        btnChangeRecordTagStatistics.setOnClick(viewModel::onStatisticsClick)
        containerChangeRecordTypeIcon.btnIconSelectionNoIcon.setOnClick(viewModel::onNoIconClick)
        IconSelectionViewDelegate.initUx(
            viewModel = viewModel,
            layout = containerChangeRecordTypeIcon,
            iconsLayoutManager = iconsLayoutManager,
        )
        addOnBackPressedListener(action = viewModel::onBackPressed)
    }

    override fun initViewModel(): Unit = with(binding) {
        with(viewModel) {
            extra = params
            deleteIconVisibility.observeOnce(viewLifecycleOwner, btnChangeRecordTagDelete::visible::set)
            statsIconVisibility.observeOnce(viewLifecycleOwner, btnChangeRecordTagStatistics::isVisible::set)
            saveButtonEnabled.observe(btnChangeRecordTagSave::setEnabled)
            deleteButtonEnabled.observe(btnChangeRecordTagDelete::setEnabled)
            iconColorSourceSelected.observe(::updateIconColorSourceSelected)
            preview.observeOnce(viewLifecycleOwner, ::updateUi)
            preview.observe(::updatePreview)
            colors.observe(colorsAdapter::replace)
            icons.observe(::updateIconsState)
            iconCategories.observe(::updateIconCategories)
            iconsTypeViewData.observe(::updateIconsTypeViewData)
            iconSelectorViewData.observe(::updateIconSelectorViewData)
            expandIconTypeSwitch.observe { updateBarExpanded() }
            types.observe(::updateTypes)
            defaultTypes.observe(::updateDefaultTypes)
            chooserState.observe(::updateChooserState)
            keyboardVisibility.observe { visible ->
                if (visible) showKeyboard(etChangeRecordTagName) else hideKeyboard()
            }
        }
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

    override fun onTypesSelected(typeIds: List<Long>, tag: String?) {
        viewModel.onTypesSelected(typeIds, tag)
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
                    binding.layoutChangeRecordTagIconPreview.isVisible = true
                    binding.iconChangeRecordTagIconPreview.itemIcon = icon.toViewData()
                } else {
                    itemIconVisible = false
                    binding.layoutChangeRecordTagIconPreview.isVisible = false
                }

                binding.layoutChangeRecordTagColorPreview.setCardBackgroundColor(it.color)
                binding.layoutChangeRecordTagIconPreview.setCardBackgroundColor(it.color)
                binding.layoutChangeRecordTagTypesPreview.setCardBackgroundColor(it.color)
                binding.layoutChangeRecordTagDefaultTypePreview.setCardBackgroundColor(it.color)
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
                binding.layoutChangeRecordTagIconPreview.isVisible = true
                binding.iconChangeRecordTagIconPreview.itemIcon = icon
            } else {
                itemIconVisible = false
                binding.layoutChangeRecordTagIconPreview.isVisible = false
            }

            typeColorAnimator?.cancel()
            typeColorAnimator = animateColor(
                from = itemColor,
                to = item.color,
                doOnUpdate = { value ->
                    itemColor = value
                    binding.layoutChangeRecordTagColorPreview.setCardBackgroundColor(value)
                },
            )
        }
        with(binding) {
            layoutChangeRecordTagIconPreview.setCardBackgroundColor(item.color)
            layoutChangeRecordTagTypesPreview.setCardBackgroundColor(item.color)
            layoutChangeRecordTagDefaultTypePreview.setCardBackgroundColor(item.color)
        }
    }

    private fun updateChooserState(state: ChangeRecordTagChooserState) = with(binding) {
        updateChooser<Color>(
            state = state,
            chooserData = rvChangeRecordTagColor,
            chooserView = fieldChangeRecordTagColor,
            chooserArrow = arrowChangeRecordTagColor,
        )
        updateChooser<Icon>(
            state = state,
            chooserData = containerChangeRecordTypeIcon.root,
            chooserView = fieldChangeRecordTagIcon,
            chooserArrow = arrowChangeRecordTagIcon,
        )
        updateChooser<Type>(
            state = state,
            chooserData = rvChangeRecordTagType,
            chooserView = fieldChangeRecordTagType,
            chooserArrow = arrowChangeRecordTagType,
        )
        updateChooser<DefaultType>(
            state = state,
            chooserData = rvChangeRecordTagDefaultType,
            chooserView = fieldChangeRecordTagDefaultType,
            chooserArrow = arrowChangeRecordTagDefaultType,
        )

        val isClosed = state.current is Closed
        inputChangeRecordTagName.isVisible = isClosed
        btnChangeRecordTagSelectActivity.isVisible = isClosed
        btnChangeRecordTagStatistics.isVisible =
            viewModel.statsIconVisibility.value.orFalse() && isClosed
        btnChangeRecordTagDelete.isVisible =
            viewModel.deleteIconVisibility.value.orFalse() && isClosed
        dividerChangeRecordTagBottom.isVisible = !isClosed

        // Chooser fields
        fieldChangeRecordTagColor.isVisible = isClosed || state.current is Color
        fieldChangeRecordTagIcon.isVisible = isClosed || state.current is Icon
        fieldChangeRecordTagType.isVisible = isClosed || state.current is Type
        fieldChangeRecordTagDefaultType.isVisible = isClosed || state.current is DefaultType
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

    private inline fun <reified T : State> updateChooser(
        state: ChangeRecordTagChooserState,
        chooserData: View,
        chooserView: CardView,
        chooserArrow: View,
    ) {
        UpdateViewChooserState.updateChooser<State, T, Closed>(
            stateCurrent = state.current,
            statePrevious = state.previous,
            chooserData = chooserData,
            chooserView = chooserView,
            chooserArrow = chooserArrow,
        )
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: ChangeRecordTagFromScreen): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data.params)
        }
    }
}