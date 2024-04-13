package com.example.util.simpletimetracker.core.delegates.iconSelection.viewDelegate

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.databinding.IconSelectionLayoutBinding
import com.example.util.simpletimetracker.core.delegates.iconSelection.IconSelectionViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.ChangeRecordTypeIconCategoryInfoViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.ChangeRecordTypeIconSelectorStateViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.ChangeRecordTypeIconStateViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.ChangeRecordTypeIconSwitchViewData
import com.example.util.simpletimetracker.core.repo.DeviceRepo
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowView
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.model.IconEmojiType
import com.example.util.simpletimetracker.domain.model.IconImageState
import com.example.util.simpletimetracker.domain.model.IconType
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_views.extension.addOnScrollListenerAdapter
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setSpanSizeLookup
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.textfield.TextInputLayout
import kotlin.math.max

object IconSelectionViewDelegate {

    fun initUi(
        context: Context,
        resources: Resources,
        deviceRepo: DeviceRepo,
        layout: IconSelectionLayoutBinding,
        iconsAdapter: BaseRecyclerAdapter,
        iconCategoriesAdapter: BaseRecyclerAdapter,
    ): GridLayoutManager = with(layout) {
        val columnCount = getIconsColumnCount(
            resources,
            deviceRepo,
            layout,
        )
        val manager = GridLayoutManager(context, columnCount)

        rvChangeRecordTypeIcon.apply {
            layoutManager = manager
            adapter = iconsAdapter
            itemAnimator = null
            setIconsSpanSize(
                iconsLayoutManager = manager,
                iconsAdapter = iconsAdapter,
            )
        }

        rvChangeRecordTypeIconCategory.apply {
            layoutManager = GridLayoutManager(context, IconEmojiType.values().size)
            adapter = iconCategoriesAdapter
            itemAnimator = null
        }

        return@with manager
    }

    fun initUx(
        viewModel: IconSelectionViewModelDelegate,
        layout: IconSelectionLayoutBinding,
        iconsLayoutManager: GridLayoutManager?,
    ) = with(layout) {
        btnChangeRecordTypeIconSwitch.listener = {
            updateIconContainerScroll(it, layout)
            viewModel.onIconTypeClick(it)
        }
        etChangeRecordTypeIconSearch.doAfterTextChanged { viewModel.onIconImageSearch(it.toString()) }
        btnChangeRecordTypeIconSearch.setOnClick(viewModel::onIconImageSearchClicked)
        rvChangeRecordTypeIcon.addOnScrollListenerAdapter(
            onScrolled = { _, _, _ ->
                iconsLayoutManager?.let {
                    viewModel.onIconsScrolled(
                        firstVisiblePosition = it.findFirstCompletelyVisibleItemPosition(),
                        lastVisiblePosition = it.findLastCompletelyVisibleItemPosition(),
                    )
                }
            },
        )
    }

    fun updateUi(
        icon: RecordTypeIcon?,
        viewModel: IconSelectionViewModelDelegate,
        layout: IconSelectionLayoutBinding,
    ) = with(layout) {
        (icon as? RecordTypeIcon.Text)?.text?.let {
            etChangeRecordTypeIconText.setText(it)
        }
        // Set listener only after text is set to avoid trigger on screen return.
        etChangeRecordTypeIconText.doAfterTextChanged { viewModel.onIconTextChange(it.toString()) }
    }

    fun updateBarExpanded(
        layout: IconSelectionLayoutBinding,
    ) = with(layout) {
        appBarChangeRecordTypeIcon.setExpanded(true)
    }

    fun updateIconsTypeViewData(
        data: List<ViewHolderType>,
        layout: IconSelectionLayoutBinding,
    ) = with(layout) {
        btnChangeRecordTypeIconSwitch.adapter.replace(data)
    }

    fun updateIconsState(
        state: ChangeRecordTypeIconStateViewData,
        layout: IconSelectionLayoutBinding,
        iconsAdapter: BaseRecyclerAdapter,
    ) = with(layout) {
        when (state) {
            is ChangeRecordTypeIconStateViewData.Icons -> {
                rvChangeRecordTypeIcon.isVisible = true
                inputChangeRecordTypeIconText.isVisible = false
                iconsAdapter.replaceAsNew(state.items)
            }
            is ChangeRecordTypeIconStateViewData.Text -> {
                rvChangeRecordTypeIcon.isVisible = false
                inputChangeRecordTypeIconText.isVisible = true
            }
        }
    }

    fun updateIconCategories(
        data: List<ViewHolderType>,
        iconCategoriesAdapter: BaseRecyclerAdapter,
    ) {
        iconCategoriesAdapter.replaceAsNew(data)
    }

    private fun getIconsColumnCount(
        resources: Resources,
        deviceRepo: DeviceRepo,
        layout: IconSelectionLayoutBinding,
    ): Int = with(layout) {
        val screenWidth = deviceRepo.getScreenWidthInDp().dpToPx()
        val recyclerWidth = screenWidth -
            2 * resources.getDimensionPixelOffset(R.dimen.color_icon_recycler_margin)
        val elementWidth = resources.getDimensionPixelOffset(R.dimen.color_icon_item_width) +
            2 * resources.getDimensionPixelOffset(R.dimen.color_icon_item_margin)
        val columnCount = max(recyclerWidth / elementWidth, 1)

        val rowWidth = elementWidth * columnCount
        val recyclerPadding = (recyclerWidth - rowWidth) / 2
        rvChangeRecordTypeIcon.updatePadding(left = recyclerPadding, right = recyclerPadding)

        return columnCount
    }

    private fun setIconsSpanSize(
        iconsLayoutManager: GridLayoutManager?,
        iconsAdapter: BaseRecyclerAdapter,
    ) {
        iconsLayoutManager?.setSpanSizeLookup { position ->
            when (iconsAdapter.getItemByPosition(position)) {
                is ChangeRecordTypeIconCategoryInfoViewData,
                is LoaderViewData,
                -> iconsLayoutManager.spanCount
                else -> 1
            }
        }
    }

    private fun updateIconContainerScroll(
        item: ButtonsRowViewData,
        layout: IconSelectionLayoutBinding,
    ) = with(layout) {
        if (item !is ChangeRecordTypeIconSwitchViewData) return

        val scrollFlags = if (item.iconType == IconType.TEXT) {
            0
        } else {
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
        }

        (btnChangeRecordTypeIconSwitch.layoutParams as? AppBarLayout.LayoutParams)
            ?.scrollFlags = scrollFlags
    }

    fun updateIconSelectorViewData(
        data: ChangeRecordTypeIconSelectorStateViewData,
        layout: IconSelectionLayoutBinding,
    ) = with(layout) {
        if (data is ChangeRecordTypeIconSelectorStateViewData.Available) {
            btnChangeRecordTypeIconSearch.isVisible = data.searchButtonIsVisible
            ivChangeRecordTypeIconSearch.backgroundTintList = ColorStateList.valueOf(data.searchButtonColor)
            rvChangeRecordTypeIconCategory.isVisible = data.state is IconImageState.Chooser
            inputChangeRecordTypeIconSearch.isVisible = data.state is IconImageState.Search
        } else {
            btnChangeRecordTypeIconSearch.isVisible = false
            rvChangeRecordTypeIconCategory.isVisible = false
            inputChangeRecordTypeIconSearch.isVisible = false
        }
    }
}