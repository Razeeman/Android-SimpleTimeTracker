package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.SharingInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsDetailNavigationInteractor
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.domain.darkMode.interactor.ThemeChangedInteractor
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.StatisticsUpdateInteractor
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.statistics.StatisticsViewData
import com.example.util.simpletimetracker.feature_base_adapter.statisticsGoal.StatisticsGoalViewData
import com.example.util.simpletimetracker.feature_statistics.extra.StatisticsExtra
import com.example.util.simpletimetracker.feature_statistics.interactor.StatisticsDetailTotalNavigator
import com.example.util.simpletimetracker.feature_statistics.interactor.StatisticsViewDataInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChartFilterDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val router: Router,
    private val statisticsViewDataInteractor: StatisticsViewDataInteractor,
    private val sharingInteractor: SharingInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsDetailNavigationInteractor: StatisticsDetailNavigationInteractor,
    private val statisticsDetailTotalNavigator: StatisticsDetailTotalNavigator,
    private val themeChangedInteractor: ThemeChangedInteractor,
    private val statisticsUpdateInteractor: StatisticsUpdateInteractor,
) : ViewModel() {

    var extra: StatisticsExtra? = null

    val statistics: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }
    val sharingData: SingleLiveEvent<List<ViewHolderType>> = SingleLiveEvent()
    val resetScreen: SingleLiveEvent<Unit> = SingleLiveEvent()
    val animateChartParticles: LiveData<Boolean> = MutableLiveData()

    private var isVisible: Boolean = false
    private var isTabScrolling: Boolean = false
    private var isChartAttached: Boolean = false
    private var isChartFilterOpened: Boolean = false
    private var isOptionsListOpened: Boolean = false
    private var timerJob: Job? = null
    private val shift: Int get() = extra?.shift.orZero()

    init {
        subscribeToUpdates()
    }

    fun onVisible() {
        isVisible = true
        if (shift == 0) {
            startUpdate()
        } else {
            updateStatistics()
        }
        updateAnimateChartParticles()
    }

    fun onHidden() {
        isVisible = false
        stopUpdate()
        updateAnimateChartParticles()
    }

    fun onChartAttached(isAttached: Boolean) {
        isChartAttached = isAttached
        updateAnimateChartParticles()
    }

    fun isScrolling(isScrolling: Boolean) {
        isTabScrolling = isScrolling
        updateAnimateChartParticles()
    }

    fun onItemClick(
        item: StatisticsViewData,
        sharedElements: Map<Any, String>,
    ) = viewModelScope.launch {
        statisticsDetailNavigationInteractor.navigate(
            transitionName = item.transitionName.orEmpty(),
            filterType = prefsInteractor.getChartFilterType(),
            shift = getShift(),
            overrideStatisticsRange = null,
            sharedElements = sharedElements,
            itemId = item.id,
            itemName = item.name,
            itemIcon = item.icon,
            itemColor = item.color,
        )
    }

    fun onGoalClick(item: StatisticsGoalViewData) = viewModelScope.launch {
        statisticsDetailNavigationInteractor.navigateByGoal(
            goalId = item.id,
            shift = getShift(),
            range = null,
        )
    }

    fun onTotalClicked(
        item: StatisticsViewData,
        sharedElements: Map<Any, String>,
    ) = viewModelScope.launch {
        statisticsDetailTotalNavigator.execute(
            shift = getShift(),
            item = item,
            sharedElements = sharedElements,
        )
    }

    fun onFilterApplied(
        chartFilterType: ChartFilterType,
        dataIds: List<Long>,
    ) = viewModelScope.launch {
        prefsInteractor.setChartFilterType(chartFilterType)
        when (chartFilterType) {
            ChartFilterType.ACTIVITY -> prefsInteractor.setFilteredTypes(dataIds)
            ChartFilterType.CATEGORY -> prefsInteractor.setFilteredCategories(dataIds)
            ChartFilterType.RECORD_TAG -> prefsInteractor.setFilteredTags(dataIds)
        }
    }

    fun onFilterClosed() {
        updateStatistics()
        isChartFilterOpened = false
        updateAnimateChartParticles()
    }

    fun onFilterOpened() {
        isChartFilterOpened = true
        updateAnimateChartParticles()
    }

    fun onShareView(view: Any) = viewModelScope.launch {
        sharingInteractor.execute(view = view, filename = SHARING_NAME)
    }

    fun onTabReselected(tab: NavigationTab?) {
        if (isVisible && tab is NavigationTab.Statistics) {
            resetScreen.set(Unit)
        }
    }

    private fun subscribeToUpdates() {
        viewModelScope.launch {
            themeChangedInteractor.themeChanged.collect { updateStatistics() }
        }
        viewModelScope.launch {
            statisticsUpdateInteractor.shareClicked.collect { if (isVisible) onShareClick() }
        }
        viewModelScope.launch {
            statisticsUpdateInteractor.filterClicked.collect { if (isVisible) onFilterClick() }
        }
        viewModelScope.launch {
            statisticsUpdateInteractor.rangeChanged.collect { if (isVisible) updateStatistics() }
        }
        viewModelScope.launch {
            statisticsUpdateInteractor.optionsVisible.collect {
                isOptionsListOpened = it
                updateAnimateChartParticles()
            }
        }
    }

    private fun onFilterClick() = viewModelScope.launch {
        val params = ChartFilterDialogParams(
            chartFilterType = prefsInteractor.getChartFilterType(),
            filteredTypeIds = prefsInteractor.getFilteredTypes(),
            filteredCategoryIds = prefsInteractor.getFilteredCategories(),
            filteredTagIds = prefsInteractor.getFilteredTags(),
        )
        router.navigate(params)
    }

    private fun onShareClick() = viewModelScope.launch {
        val data = loadStatisticsViewData(forSharing = true)
        sharingData.set(data)
    }

    private suspend fun getShift(): Int {
        return if (prefsInteractor.getKeepStatisticsRange()) shift else 0
    }

    private fun updateAnimateChartParticles() {
        val shouldAnimate = isVisible &&
            isChartAttached &&
            !isTabScrolling &&
            !isChartFilterOpened &&
            !isOptionsListOpened
        animateChartParticles.set(shouldAnimate)
    }

    private fun updateStatistics() = viewModelScope.launch {
        val data = loadStatisticsViewData()
        statistics.set(data)
    }

    private suspend fun loadStatisticsViewData(forSharing: Boolean = false): List<ViewHolderType> {
        return statisticsViewDataInteractor.getViewData(
            shift = shift,
            forSharing = forSharing,
        )
    }

    private fun startUpdate() {
        timerJob = viewModelScope.launch {
            timerJob?.cancelAndJoin()
            while (isActive) {
                updateStatistics()
                delay(TIMER_UPDATE)
            }
        }
    }

    private fun stopUpdate() {
        viewModelScope.launch {
            timerJob?.cancelAndJoin()
        }
    }

    companion object {
        private const val TIMER_UPDATE = 1000L
        private const val SHARING_NAME = "stt_statistics"
    }
}
