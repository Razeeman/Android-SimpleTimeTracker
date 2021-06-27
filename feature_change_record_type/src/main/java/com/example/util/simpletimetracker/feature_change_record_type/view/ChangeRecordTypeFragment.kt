package com.example.util.simpletimetracker.feature_change_record_type.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.TransitionInflater
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.color.createColorAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.emoji.createEmojiAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseBindingFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.dialog.EmojiSelectionDialogListener
import com.example.util.simpletimetracker.core.extension.dpToPx
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.pxToDp
import com.example.util.simpletimetracker.core.extension.rotateDown
import com.example.util.simpletimetracker.core.extension.rotateUp
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.setSpanSizeLookup
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.extension.updatePadding
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.core.repo.DeviceRepo
import com.example.util.simpletimetracker.core.utils.BuildVersions
import com.example.util.simpletimetracker.core.utils.setFlipChooserColor
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.model.IconEmojiType
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.adapter.createChangeRecordTypeIconAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record_type.adapter.createChangeRecordTypeIconCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record_type.adapter.createChangeRecordTypeIconCategoryInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconCategoryInfoViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeScrollViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewModel.ChangeRecordTypeViewModel
import com.example.util.simpletimetracker.navigation.params.ChangeRecordTypeParams
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
    BaseBindingFragment<Binding>(),
    DurationDialogListener,
    EmojiSelectionDialogListener {

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
            createColorAdapterDelegate(viewModel::onColorClick)
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
            createCategoryAdapterDelegate(viewModel::onCategoryClick),
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createEmptyAdapterDelegate()
        )
    }
    private val iconsLayoutManager: GridLayoutManager by lazy {
        GridLayoutManager(requireContext(), getIconsColumnCount())
    }
    private val params: ChangeRecordTypeParams by lazy {
        arguments?.getParcelable<ChangeRecordTypeParams>(ARGS_PARAMS)
            ?: ChangeRecordTypeParams.New(ChangeRecordTypeParams.SizePreview())
    }

    override fun initUi(): Unit = with(binding) {
        setPreview()

        if (BuildVersions.isLollipopOrHigher() && params !is ChangeRecordTypeParams.New) {
            sharedElementEnterTransition = TransitionInflater.from(context)
                .inflateTransition(android.R.transition.move)
        }

        ViewCompat.setTransitionName(
            previewChangeRecordType,
            TransitionNames.RECORD_TYPE + params.id
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
            layoutManager = iconsLayoutManager
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
            iconCategories.observe(iconCategoriesAdapter::replaceAsNew)
            iconsTypeViewData.observe(btnChangeRecordTypeIconSwitch.adapter::replace)
            categories.observe(categoriesAdapter::replace)
            goalTimeViewData.observe(tvChangeRecordTypeGoalTimeTime::setText)
            flipColorChooser.observe { opened ->
                rvChangeRecordTypeColor.visible = opened
                setFlipChooserColor(fieldChangeRecordTypeColor, opened)
                arrowChangeRecordTypeColor.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            flipIconChooser.observe { opened ->
                containerChangeRecordTypeIcon.visible = opened
                setFlipChooserColor(fieldChangeRecordTypeIcon, opened)
                arrowChangeRecordTypeIcon.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            flipCategoryChooser.observe { opened ->
                rvChangeRecordTypeCategories.visible = opened
                setFlipChooserColor(fieldChangeRecordTypeCategory, opened)
                arrowChangeRecordTypeCategory.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            keyboardVisibility.observe { visible ->
                if (visible) showKeyboard(etChangeRecordTypeName) else hideKeyboard()
            }
            iconsScrollPosition.observe {
                if (it is ChangeRecordTypeScrollViewData.ScrollTo) {
                    iconsLayoutManager.scrollToPositionWithOffset(it.position, 0)
                    onScrolled()
                }
            }
        }
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
        iconsLayoutManager.setSpanSizeLookup { position ->
            if (iconsAdapter.getItem(position) is ChangeRecordTypeIconCategoryInfoViewData) {
                iconsLayoutManager.spanCount
            } else {
                1
            }
        }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"
        private const val DELETE_BUTTON_SIZE = 72 // TODO get from dimens or viewModel

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is ChangeRecordTypeParams -> putParcelable(ARGS_PARAMS, data)
            }
        }
    }
}