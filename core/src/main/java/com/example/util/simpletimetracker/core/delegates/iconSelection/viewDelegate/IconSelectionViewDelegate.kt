package com.example.util.simpletimetracker.core.delegates.iconSelection.viewDelegate

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.text.TextWatcher
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.databinding.IconSelectionLayoutBinding
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewModelDelegate.IconSelectionViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionCategoryInfoViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionSelectorStateViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionStateViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionSwitchViewData
import com.example.util.simpletimetracker.core.repo.DeviceRepo
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

        rvIconSelection.apply {
            layoutManager = manager
            adapter = iconsAdapter
            itemAnimator = null
            setIconsSpanSize(
                iconsLayoutManager = manager,
                iconsAdapter = iconsAdapter,
            )
        }

        rvIconSelectionCategory.apply {
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
        btnIconSelectionSwitch.listener = {
            updateIconContainerScroll(it, layout)
            viewModel.onIconTypeClick(it)
        }
        etIconSelectionSearch.doAfterTextChanged { viewModel.onIconImageSearch(it.toString()) }
        btnIconSelectionSearch.setOnClick(viewModel::onIconImageSearchClicked)
        btnIconSelectionFavourite.setOnClick(viewModel::onIconImageFavouriteClicked)
        rvIconSelection.addOnScrollListenerAdapter(
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

    fun onDestroyView(
        textWatcher: TextWatcher?,
        layout: IconSelectionLayoutBinding,
    ) = with(layout) {
        // Remove textWatcher because it will be set again on init ViewModel,
        // to avoid several watcher being set on screen navigation forward and backward.
        textWatcher?.let(etIconSelectionText::removeTextChangedListener)
    }

    fun updateUi(
        icon: RecordTypeIcon?,
        viewModel: IconSelectionViewModelDelegate,
        layout: IconSelectionLayoutBinding,
    ): TextWatcher = with(layout) {
        (icon as? RecordTypeIcon.Text)?.text?.let {
            etIconSelectionText.setText(it)
        }
        // Set listener only after text is set to avoid trigger on screen return.
        etIconSelectionText.doAfterTextChanged { viewModel.onIconTextChange(it.toString()) }
    }

    fun updateBarExpanded(
        layout: IconSelectionLayoutBinding,
    ) = with(layout) {
        appBarIconSelection.setExpanded(true)
    }

    fun updateIconsTypeViewData(
        data: List<ViewHolderType>,
        layout: IconSelectionLayoutBinding,
    ) = with(layout) {
        btnIconSelectionSwitch.adapter.replace(data)
    }

    fun updateIconsState(
        state: IconSelectionStateViewData,
        layout: IconSelectionLayoutBinding,
        iconsAdapter: BaseRecyclerAdapter,
    ) = with(layout) {
        when (state) {
            is IconSelectionStateViewData.Icons -> {
                rvIconSelection.isVisible = true
                inputIconSelectionText.isVisible = false
                iconsAdapter.replaceAsNew(state.items)
            }
            is IconSelectionStateViewData.Text -> {
                rvIconSelection.isVisible = false
                inputIconSelectionText.isVisible = true
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
        rvIconSelection.updatePadding(left = recyclerPadding, right = recyclerPadding)

        return columnCount
    }

    private fun setIconsSpanSize(
        iconsLayoutManager: GridLayoutManager?,
        iconsAdapter: BaseRecyclerAdapter,
    ) {
        iconsLayoutManager?.setSpanSizeLookup { position ->
            when (iconsAdapter.getItemByPosition(position)) {
                is IconSelectionCategoryInfoViewData,
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
        if (item !is IconSelectionSwitchViewData) return

        val scrollFlags = if (item.iconType == IconType.TEXT) {
            0
        } else {
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
        }

        (btnIconSelectionSwitch.layoutParams as? AppBarLayout.LayoutParams)
            ?.scrollFlags = scrollFlags
    }

    fun updateIconSelectorViewData(
        data: IconSelectionSelectorStateViewData,
        layout: IconSelectionLayoutBinding,
    ) = with(layout) {
        if (data is IconSelectionSelectorStateViewData.Available) {
            btnIconSelectionSearch.isVisible = true
            ivIconSelectionSearch.backgroundTintList = ColorStateList.valueOf(data.searchButtonColor)
            btnIconSelectionFavourite.isVisible = true
            ivIconSelectionFavourite.backgroundTintList = ColorStateList.valueOf(data.favouriteButtonColor)
            rvIconSelectionCategory.isVisible = data.state is IconImageState.Chooser
            inputIconSelectionSearch.isVisible = data.state is IconImageState.Search
        } else {
            btnIconSelectionSearch.isVisible = false
            btnIconSelectionFavourite.isVisible = false
            rvIconSelectionCategory.isVisible = false
            inputIconSelectionSearch.isVisible = false
        }
    }
}