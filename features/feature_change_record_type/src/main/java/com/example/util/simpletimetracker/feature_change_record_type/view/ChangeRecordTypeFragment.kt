package com.example.util.simpletimetracker.feature_change_record_type.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.ColorSelectionDialogListener
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.dialog.EmojiSelectionDialogListener
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setSharedTransitions
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.repo.DeviceRepo
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.core.utils.setChooserColor
import com.example.util.simpletimetracker.domain.model.IconEmojiType
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorPaletteAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.emoji.createEmojiAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAddAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record_type.adapter.createChangeRecordTypeIconAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record_type.adapter.createChangeRecordTypeIconCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record_type.adapter.createChangeRecordTypeIconCategoryInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconCategoryInfoViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeScrollViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewModel.ChangeRecordTypeViewModel
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.pxToDp
import com.example.util.simpletimetracker.feature_views.extension.rotateDown
import com.example.util.simpletimetracker.feature_views.extension.rotateUp
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setSpanSizeLookup
import com.example.util.simpletimetracker.feature_views.extension.updatePadding
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTypeParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.max
import com.example.util.simpletimetracker.feature_change_record_type.databinding.ChangeRecordTypeFragmentBinding as Binding

@AndroidEntryPoint
class ChangeRecordTypeFragment :
    BaseFragment<Binding>(),
    DurationDialogListener,
    EmojiSelectionDialogListener,
    ColorSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<ChangeRecordTypeViewModel>

    @Inject
    lateinit var deviceRepo: DeviceRepo

    private val viewModel: ChangeRecordTypeViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val colorsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createColorAdapterDelegate(viewModel::onColorClick),
            createColorPaletteAdapterDelegate(viewModel::onColorPaletteClick),
        )
    }
    private val iconsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createChangeRecordTypeIconAdapterDelegate(viewModel::onIconClick),
            createEmojiAdapterDelegate(viewModel::onEmojiClick),
            createChangeRecordTypeIconCategoryInfoAdapterDelegate()
        )
    }
    private val iconCategoriesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createChangeRecordTypeIconCategoryAdapterDelegate(viewModel::onIconCategoryClick)
        )
    }
    private val categoriesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createCategoryAdapterDelegate(
                onClick = viewModel::onCategoryClick,
                onLongClickWithTransition = viewModel::onCategoryLongClick
            ),
            createCategoryAddAdapterDelegate { viewModel.onAddCategoryClick() },
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createHintAdapterDelegate(),
            createEmptyAdapterDelegate()
        )
    }
    private var iconsLayoutManager: GridLayoutManager? = null
    private val params: ChangeRecordTypeParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS,
        default = ChangeRecordTypeParams.New(ChangeRecordTypeParams.SizePreview())
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

        rvChangeRecordTypeIcon.apply {
            layoutManager = GridLayoutManager(requireContext(), getIconsColumnCount())
                .also { iconsLayoutManager = it }
            adapter = iconsAdapter
            setIconsSpanSize()
        }

        rvChangeRecordTypeIconCategory.apply {
            layoutManager = GridLayoutManager(requireContext(), IconEmojiType.values().size)
            adapter = iconCategoriesAdapter
        }

        rvChangeRecordTypeCategories.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = categoriesAdapter
        }

        root.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }
    }

    override fun initUx() = with(binding) {
        etChangeRecordTypeName.doAfterTextChanged { viewModel.onNameChange(it.toString()) }
        fieldChangeRecordTypeColor.setOnClick(viewModel::onColorChooserClick)
        fieldChangeRecordTypeIcon.setOnClick(viewModel::onIconChooserClick)
        fieldChangeRecordTypeCategory.setOnClick(viewModel::onCategoryChooserClick)
        groupChangeRecordTypeGoalTime.setOnClick(viewModel::onGoalTimeClick)
        btnChangeRecordTypeSave.setOnClick(viewModel::onSaveClick)
        btnChangeRecordTypeDelete.setOnClick(viewModel::onDeleteClick)
        btnChangeRecordTypeIconSwitch.listener = viewModel::onIconTypeClick
    }

    override fun initViewModel(): Unit = with(binding) {
        with(viewModel) {
            extra = params
            deleteIconVisibility.observeOnce(viewLifecycleOwner, btnChangeRecordTypeDelete::visible::set)
            saveButtonEnabled.observe(btnChangeRecordTypeSave::setEnabled)
            deleteButtonEnabled.observe(btnChangeRecordTypeDelete::setEnabled)
            recordType.observeOnce(viewLifecycleOwner, ::updateUi)
            recordType.observe(::updatePreview)
            colors.observe(colorsAdapter::replace)
            icons.observe(iconsAdapter::replace)
            iconCategories.observe(iconCategoriesAdapter::replace)
            iconsTypeViewData.observe(btnChangeRecordTypeIconSwitch.adapter::replace)
            categories.observe(categoriesAdapter::replace)
            goalTimeViewData.observe(tvChangeRecordTypeGoalTimeTime::setText)
            flipColorChooser.observe { opened ->
                rvChangeRecordTypeColor.visible = opened
                fieldChangeRecordTypeColor.setChooserColor(opened)
                arrowChangeRecordTypeColor.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            flipIconChooser.observe { opened ->
                containerChangeRecordTypeIcon.visible = opened
                fieldChangeRecordTypeIcon.setChooserColor(opened)
                arrowChangeRecordTypeIcon.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            flipCategoryChooser.observe { opened ->
                rvChangeRecordTypeCategories.visible = opened
                fieldChangeRecordTypeCategory.setChooserColor(opened)
                arrowChangeRecordTypeCategory.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            keyboardVisibility.observe { visible ->
                if (visible) showKeyboard(etChangeRecordTypeName) else hideKeyboard()
            }
            iconsScrollPosition.observe {
                if (it is ChangeRecordTypeScrollViewData.ScrollTo) {
                    iconsLayoutManager?.scrollToPositionWithOffset(it.position, 0)
                    onScrolled()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    override fun onDurationSet(duration: Long, tag: String?) {
        viewModel.onDurationSet(tag, duration)
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

    private fun updateUi(item: RecordTypeViewData) = with(binding) {
        etChangeRecordTypeName.setText(item.name)
        etChangeRecordTypeName.setSelection(item.name.length)
    }

    private fun updatePreview(item: RecordTypeViewData) {
        with(binding.previewChangeRecordType) {
            itemName = item.name
            itemIcon = item.iconId
            itemColor = item.color
        }
    }

    private fun setPreview() {
        val maxWidth = resources.displayMetrics.widthPixels.pxToDp() - DELETE_BUTTON_SIZE

        with(binding.previewChangeRecordType) {
            itemIsRow = params.sizePreview.asRow
            layoutParams = layoutParams.also { layoutParams ->
                params.sizePreview.width?.coerceAtMost(maxWidth)?.dpToPx()?.let { layoutParams.width = it }
                params.sizePreview.height?.dpToPx()?.let { layoutParams.height = it }
            }

            (params as? ChangeRecordTypeParams.Change)?.preview?.let {
                itemName = it.name
                itemIcon = it.iconId.toViewData()
                itemColor = it.color
            }
        }
    }

    private fun getIconsColumnCount(): Int {
        val screenWidth = deviceRepo.getScreenWidthInDp().dpToPx()
        val recyclerWidth = screenWidth -
            2 * resources.getDimensionPixelOffset(R.dimen.color_icon_recycler_margin)
        val elementWidth = resources.getDimensionPixelOffset(R.dimen.color_icon_item_width) +
            2 * resources.getDimensionPixelOffset(R.dimen.color_icon_item_margin)
        val columnCount = max(recyclerWidth / elementWidth, 1)

        val rowWidth = elementWidth * columnCount
        val recyclerPadding = (recyclerWidth - rowWidth) / 2
        binding.rvChangeRecordTypeIcon
            .updatePadding(left = recyclerPadding, right = recyclerPadding)

        return columnCount
    }

    private fun setIconsSpanSize() {
        iconsLayoutManager?.setSpanSizeLookup { position ->
            if (iconsAdapter.getItemByPosition(position) is ChangeRecordTypeIconCategoryInfoViewData) {
                iconsLayoutManager?.spanCount ?: 1
            } else {
                1
            }
        }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"
        private const val DELETE_BUTTON_SIZE = 72 // TODO get from dimens or viewModel

        fun createBundle(data: ChangeRecordTypeParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}