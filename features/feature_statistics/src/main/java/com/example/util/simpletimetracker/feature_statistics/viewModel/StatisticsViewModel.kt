package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.interactor.WidgetInteractor
import com.example.util.simpletimetracker.core.utils.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.statistics.StatisticsViewData
import com.example.util.simpletimetracker.feature_statistics.extra.StatisticsExtra
import com.example.util.simpletimetracker.feature_statistics.interactor.StatisticsViewDataInteractor
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChartFilterDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams
import com.example.util.simpletimetracker.navigation.params.screen.TypesFilterParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
    private val router: Router,
    private val statisticsViewDataInteractor: StatisticsViewDataInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val widgetInteractor: WidgetInteractor,
) : ViewModel() {

    var extra: StatisticsExtra? = null

    val statistics: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

    private var rangeLength: RangeLength = RangeLength.Day
    private var isVisible: Boolean = false

    fun onVisible() {
        isVisible = true
        updateStatistics()
    }

    fun onHidden() {
        isVisible = false
    }

    fun onNewRange(newRangeLength: RangeLength) {
        rangeLength = newRangeLength
        if (isVisible) updateStatistics()
    }

    fun onFilterClick() {
        router.navigate(ChartFilterDialogParams)
    }

    fun onItemClick(
        item: StatisticsViewData,
        sharedElements: Map<Any, String>,
    ) = viewModelScope.launch {
        // TODO untracked detailed statistics
        if (item.id == UNTRACKED_ITEM_ID) return@launch

        val filterType = prefsInteractor.getChartFilterType()

        router.navigate(
            data = StatisticsDetailParams(
                transitionName = TransitionNames.STATISTICS_DETAIL + item.id,
                filter = TypesFilterParams(
                    selectedIds = listOf(item.id),
                    filterType = filterType
                ),
                preview = StatisticsDetailParams.Preview(
                    name = item.name,
                    iconId = item.icon?.toParams(),
                    color = item.color
                )
            ),
            sharedElements = sharedElements
        )
    }

    fun onFilterApplied() {
        updateStatistics()
        widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
    }

    private fun updateStatistics() = viewModelScope.launch {
        val data = loadStatisticsViewData()
        (statistics as MutableLiveData).value = data
    }

    private suspend fun loadStatisticsViewData(): List<ViewHolderType> {
        return statisticsViewDataInteractor.getViewData(rangeLength, extra?.shift.orZero())
    }
}
