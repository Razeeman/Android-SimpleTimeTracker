package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.ButtonsRowItemViewData
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailGoalsInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailGoalsViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.mapItems
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.mapToViewData
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.StatisticsDetailGoalOptionsListItem
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGoalsCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailGoalsViewModelDelegate @Inject constructor(
    private val router: Router,
    private val goalsInteractor: StatisticsDetailGoalsInteractor,
    private val statisticsDetailGoalsViewDataMapper: StatisticsDetailGoalsViewDataMapper,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val viewData: LiveData<StatisticsDetailGoalsCompositeViewData?> by lazySuspend {
        loadViewData().also { parent?.updateContent() }
    }

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null
    private var chartGrouping: ChartGrouping = ChartGrouping.DAILY
    private var chartLength: ChartLength = ChartLength.TEN
    private var goalPosition: Int? = null

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    override fun getViewData(): StatisticsDetailViewData? {
        return viewData.value?.viewData?.mapItems()?.let(::mapToViewData)
    }

    override suspend fun doOnFiltersChanged() {
        // TODO GOAL Preserve goal selection across date navigation
        goalPosition = null
    }

    override fun onButtonsRowClick(
        block: ButtonsRowItemViewData.ButtonsRowId,
        viewData: ButtonsRowViewData,
    ) {
        when (block) {
            StatisticsDetailBlock.GoalChartGrouping -> onChartGroupingClick(viewData)
            StatisticsDetailBlock.GoalChartLength -> onChartLengthClick(viewData)
        }
    }

    private fun onChartGroupingClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailGroupingViewData) return
        this.chartGrouping = viewData.chartGrouping
        this.goalPosition = null
        updateViewData()
    }

    private fun onChartLengthClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailChartLengthViewData) return
        this.chartLength = viewData.chartLength
        updateViewData()
    }

    override fun onButtonClick(block: StatisticsDetailBlock) {
        if (block != StatisticsDetailBlock.GoalSelect) return
        delegateScope.launch { showGoalSelectionDialog() }
    }

    fun onGoalSelected(position: Int) {
        goalPosition = position
        updateViewData()
    }

    private suspend fun showGoalSelectionDialog() {
        val parent = parent ?: return
        val goals = goalsInteractor.getGoalsForRange(parent.filter, chartGrouping)
        val selectedGoal = goalsInteractor.getSelectedGoal(goals, goalPosition)
        val selectedPosition = goals.indexOf(selectedGoal).coerceAtLeast(0)
        val items = goals.mapIndexed { position, goal ->
            OptionsListParams.Item(
                id = StatisticsDetailGoalOptionsListItem(
                    position = position,
                    type = StatisticsDetailGoalOptionsListItem.Type.GOALS,
                ),
                text = statisticsDetailGoalsViewDataMapper.mapGoalName(goal),
                icon = null,
                isSelected = position == selectedPosition,
            )
        }
        if (items.isNotEmpty()) {
            router.navigate(OptionsListParams(items))
        }
    }

    override fun updateViewData(animate: Boolean) {
        delegateScope.launch {
            val data = loadViewData() ?: return@launch
            viewData.set(data)
            chartGrouping = data.appliedChartGrouping
            chartLength = data.appliedChartLength
            parent?.updateContent()
        }
    }

    private suspend fun loadViewData(): StatisticsDetailGoalsCompositeViewData? {
        val parent = parent ?: return null
        return goalsInteractor.getChartViewData(
            records = parent.records,
            filter = parent.filter,
            currentChartGrouping = chartGrouping,
            currentChartLength = chartLength,
            rangeLength = parent.rangeLength,
            rangePosition = parent.rangePosition,
            goalPosition = goalPosition,
        )
    }

    companion object : StatisticsDetailViewData.Key
}