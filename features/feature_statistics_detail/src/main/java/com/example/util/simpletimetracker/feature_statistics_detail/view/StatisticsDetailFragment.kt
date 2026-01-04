package com.example.util.simpletimetracker.feature_statistics_detail.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.delegates.dateSelector.viewDelegate.DateSelectorViewDelegate
import com.example.util.simpletimetracker.core.dialog.CustomRangeSelectionDialogListener
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.dialog.OptionsListDialogListener
import com.example.util.simpletimetracker.core.dialog.RecordsFilterListener
import com.example.util.simpletimetracker.core.extension.addOnBackPressedListener
import com.example.util.simpletimetracker.core.extension.onItemSwiped
import com.example.util.simpletimetracker.core.extension.setSharedTransitions
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.core.viewData.RangeSelectionOptionsListItem
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.createButtonsRowAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.statistics.StatisticsSelectableViewData
import com.example.util.simpletimetracker.feature_base_adapter.statistics.createStatisticsAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.statistics.createStatisticsSelectableAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailBarChartAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailButtonAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailCardAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailCardDoubleAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailDayCalendarAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailHintAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailNextActivitiesAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailPieChartAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailPreviewsAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailSeriesCalendarAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailSeriesChartAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.api.StatisticsDetailOptionsListItem
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.StatisticsDetailViewModel
import com.example.util.simpletimetracker.feature_views.ColorUtils
import com.example.util.simpletimetracker.feature_views.extension.getThemedAttr
import com.example.util.simpletimetracker.feature_views.extension.postDelayed
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterResultParams
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailFragmentBinding as Binding

@AndroidEntryPoint
class StatisticsDetailFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener,
    DurationDialogListener,
    CustomRangeSelectionDialogListener,
    RecordsFilterListener,
    OptionsListDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.root }

    private val viewModel: StatisticsDetailViewModel by viewModels()

    private val contentAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createStatisticsDetailPreviewsAdapterDelegate(
                onClick = viewModel::onPreviewItemClick,
                onLongClick = viewModel::onPreviewItemLongClick,
            ),
            createStatisticsDetailBarChartAdapterDelegate(
                onBarClick = viewModel::onChartClick,
            ),
            createStatisticsDetailPieChartAdapterDelegate(
                onPieClick = viewModel::onChartClick,
            ),
            createStatisticsDetailDayCalendarAdapterDelegate(),
            createButtonsRowAdapterDelegate(
                onClick = viewModel::onButtonsRowClick,
            ),
            createStatisticsDetailButtonAdapterDelegate(
                onClick = viewModel::onButtonClick,
            ),
            createStatisticsDetailCardAdapterDelegate(
                onClick = throttle(viewModel::onCardClick),
            ),
            createStatisticsDetailCardDoubleAdapterDelegate(
                onFirstClick = throttle(viewModel::onCardClick),
                onSecondClick = throttle(viewModel::onCardClick),
            ),
            createStatisticsDetailSeriesChartAdapterDelegate(),
            createStatisticsDetailSeriesCalendarAdapterDelegate(
                onClick = throttle(viewModel::onStreaksCalendarClick),
            ),
            createStatisticsDetailHintAdapterDelegate(),
            createStatisticsDetailNextActivitiesAdapterDelegate(),
            createHintAdapterDelegate(),
            createStatisticsAdapterDelegate(
                onItemClick = null,
            ),
            createStatisticsSelectableAdapterDelegate(
                onItemClick = viewModel::onStatisticsItemClick,
            ),
        )
    }
    private val dateSelectorViewHolder by lazy {
        DateSelectorViewDelegate.getViewHolder(
            viewModel = viewModel.dateSelectorViewModelDelegate,
        )
    }
    private val params: StatisticsDetailParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = StatisticsDetailParams.Empty,
    )

    override fun initUi(): Unit = with(binding) {
        setPreview()

        setSharedTransitions(
            additionalCondition = { params.transitionName.isNotEmpty() },
            transitionName = params.transitionName,
            sharedView = viewStatisticsDetailItem,
        )

        rvStatisticsDetailContent.adapter = contentAdapter

        DateSelectorViewDelegate.initUi(
            fragment = this@StatisticsDetailFragment,
            viewHolder = dateSelectorViewHolder,
            viewModel = viewModel.dateSelectorViewModelDelegate,
            binding = containerDatesSelector,
        )
    }

    override fun initUx() = with(binding) {
        addOnBackPressedListener {
            // Force show preview otherwise shared transition would brake.
            viewStatisticsDetailItem.isVisible = true
            viewModel.onBackPressed()
        }
        DateSelectorViewDelegate.initUx(
            fragment = this@StatisticsDetailFragment,
            binding = binding.containerDatesSelector,
            onRecordAddClick = {},
            onOptionsClick = viewModel::onOptionsClick,
            onOptionsLongClick = viewModel::onOptionsLongClick,
        )
        initOnItemSwiped()
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    override fun onCustomRangeSelected(range: Range) {
        viewModel.onCustomRangeSelected(range)
    }

    override fun onCountSet(count: Long, tag: String?) {
        viewModel.onCountSet(count, tag)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        initialize(params)
        scrollToTop.observe { scrollToTop() } // TODO expand appbar on short list.
        content.observe(contentAdapter::replace)
        previewViewData.observe(::setPreviewViewData)
        DateSelectorViewDelegate.initViewModel(
            fragment = this@StatisticsDetailFragment,
            viewHolder = dateSelectorViewHolder,
            viewModel = viewModel.dateSelectorViewModelDelegate,
            binding = binding.containerDatesSelector,
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    override fun onFilterChanged(result: RecordsFilterResultParams) {
        viewModel.onTypesFilterSelected(result)
    }

    override fun onFilterDismissed(tag: String) {
        viewModel.onTypesFilterDismissed(tag)
    }

    override fun onOptionsItemClick(id: OptionsListParams.Item.Id) {
        when (id) {
            is StatisticsDetailOptionsListItem -> {
                viewModel.onOptionsItemClick(id)
            }
            is RangeSelectionOptionsListItem -> {
                viewModel.onRangeSelected(id)
            }
        }
    }

    private fun setPreview() = params.preview?.run {
        val preview = StatisticsDetailPreviewViewData(
            id = 0L,
            type = StatisticsDetailPreviewViewData.Type.FILTER,
            dataType = StatisticsDetailPreviewViewData.DataType.ACTIVITY,
            name = name,
            iconId = iconId?.toViewData(),
            iconColor = null,
            color = color,
            isFiltered = false,
        )

        StatisticsDetailPreviewCompositeViewData(
            previewColor = preview.color,
            comparisonPreviewColor = null,
            mainPreview = preview,
            additionalData = emptyList(),
            comparisonData = emptyList(),
        ).let(::setPreviewViewData)
    }

    private fun setPreviewViewData(viewData: StatisticsDetailPreviewCompositeViewData?) = with(binding) {
        val preview = viewData?.mainPreview

        if (preview == null) {
            viewStatisticsDetailItem.isVisible = false
            return@with
        } else {
            val firstItem = (rvStatisticsDetailContent.layoutManager as? LinearLayoutManager)
                ?.findFirstCompletelyVisibleItemPosition().orZero()
            viewStatisticsDetailItem.isVisible = true
            // If content is scrolled - prevent app bar from expanding on item visible.
            if (firstItem > 1) {
                appBarStatisticsDetail.setExpanded(false, false)
            }
        }

        viewStatisticsDetailItem.itemName = preview.name
        viewStatisticsDetailItem.itemColor = preview.color
        if (preview.iconId != null) {
            viewStatisticsDetailItem.itemIconVisible = true
            viewStatisticsDetailItem.itemIcon = preview.iconId
        } else {
            viewStatisticsDetailItem.itemIconVisible = false
        }
    }

    private fun scrollToTop() = with(binding) {
        rvStatisticsDetailContent.apply {
            postDelayed(300) {
                appBarStatisticsDetail.setExpanded(true)
                scrollToPosition(0)
            }
        }
    }

    private fun initOnItemSwiped() = with(binding) {
        fun ViewHolderType.canBeSwiped(): Boolean = this is StatisticsSelectableViewData
        fun Int.changeAlpha(alpha: Float): Int = ColorUtils.changeAlpha(this, alpha)
        fun RecyclerView.ViewHolder.getItemType(): ViewHolderType? =
            adapterPosition.let(contentAdapter::getItemByPosition)

        fun RecyclerView.ViewHolder.isSelectable(): Boolean {
            val itemsCount = contentAdapter.currentList
                .filterIsInstance<StatisticsSelectableViewData>().size
            val currentItem = getItemType()
            return itemsCount > 1 && currentItem?.canBeSwiped().orFalse()
        }

        val context = rvStatisticsDetailContent.context
        rvStatisticsDetailContent.onItemSwiped(
            startIconRes = R.drawable.show,
            endIconRes = R.drawable.hide,
            iconColor = context.getThemedAttr(R.attr.appContrastColor),
            startText = context.getString(R.string.records_filter_exclude_other),
            endText = context.getString(R.string.records_filter_exclude),
            textColor = context.getThemedAttr(R.attr.appContrastColor).changeAlpha(0.3f),
            backgroundColor = context.getThemedAttr(R.attr.appContrastColor).changeAlpha(0.1f),
            getIsSelectable = { it?.isSelectable().orFalse() },
            onSwipedStart = { it?.getItemType()?.let(viewModel::onSwipedStart) },
            onSwipedEnd = { it?.getItemType()?.let(viewModel::onSwipedEnd) },
        )
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: StatisticsDetailParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}
