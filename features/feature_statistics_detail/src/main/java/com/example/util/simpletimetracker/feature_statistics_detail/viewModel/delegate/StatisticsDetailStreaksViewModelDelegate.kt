package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.extension.getDaily
import com.example.util.simpletimetracker.domain.model.Coordinates
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.customView.SeriesCalendarView
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailGetGoalFromFilterInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailStreaksInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.model.StreaksGoal
import com.example.util.simpletimetracker.feature_statistics_detail.model.StreaksType
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStreaksGoalViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStreaksTypeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStreaksViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.PopupParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailStreaksViewModelDelegate @Inject constructor(
    private val router: Router,
    private val timeMapper: TimeMapper,
    private val streaksInteractor: StatisticsDetailStreaksInteractor,
    private val statisticsDetailGetGoalFromFilterInteractor: StatisticsDetailGetGoalFromFilterInteractor,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val streaksViewData: LiveData<StatisticsDetailStreaksViewData?> by lazySuspend {
        loadEmptyStreaksViewData().also { parent?.updateContent() }
    }
    val streaksTypeViewData: LiveData<List<ViewHolderType>> by lazySuspend {
        loadStreaksTypeViewData().also { parent?.updateContent() }
    }
    val streaksGoalViewData: LiveData<List<ViewHolderType>> by lazySuspend {
        loadStreaksGoalViewData().also { parent?.updateContent() }
    }

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null
    private var streaksType: StreaksType = StreaksType.LONGEST
    private var streaksGoal: StreaksGoal = StreaksGoal.ANY
    private var dailyGoal: Result<RecordTypeGoal.Type?>? = null
    private var compareDailyGoal: Result<RecordTypeGoal.Type?>? = null

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    fun updateStreaksViewData() = delegateScope.launch {
        streaksViewData.set(loadStreaksViewData())
        parent?.updateContent()
    }

    fun updateStreaksGoalViewData() = delegateScope.launch {
        streaksGoalViewData.set(loadStreaksGoalViewData())
        parent?.updateContent()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun updateStreaksTypeViewData() {
        streaksTypeViewData.set(loadStreaksTypeViewData())
        parent?.updateContent()
    }

    suspend fun onTypesFilterDismissed() {
        val parent = parent ?: return
        dailyGoal = Result.success(getDailyGoalType(parent.filter))
        compareDailyGoal = Result.success(getDailyGoalType(parent.comparisonFilter))
    }

    fun onStreaksTypeClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailStreaksTypeViewData) return
        streaksType = viewData.type
        updateStreaksTypeViewData()
        updateStreaksViewData()
    }

    fun onStreaksGoalClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailStreaksGoalViewData) return
        streaksGoal = viewData.type
        updateStreaksGoalViewData()
        updateStreaksViewData()
    }

    fun onStreaksCalendarClick(
        viewData: SeriesCalendarView.ViewData,
        coordinates: Coordinates,
    ) {
        PopupParams(
            message = timeMapper.formatDayDateYear(viewData.rangeStart),
            anchorCoordinates = coordinates,
        ).let(router::show)
    }

    private suspend fun getDailyGoalType(
        filters: List<RecordsFilter>,
    ): RecordTypeGoal.Type? {
        return statisticsDetailGetGoalFromFilterInteractor.execute(filters)
            .getDaily()?.type
    }

    private suspend fun getDailyGoal(): RecordTypeGoal.Type? {
        // Initialize if null.
        val goal = dailyGoal
        val parent = parent ?: return null
        return if (goal == null) {
            getDailyGoalType(parent.filter)
                .also { dailyGoal = Result.success(it) }
        } else {
            goal.getOrNull()
        }
    }

    private suspend fun getCompareDailyGoal(): RecordTypeGoal.Type? {
        // Initialize if null.
        val goal = compareDailyGoal
        val parent = parent ?: return null
        return if (goal == null) {
            getDailyGoalType(parent.comparisonFilter)
                .also { compareDailyGoal = Result.success(it) }
        } else {
            goal.getOrNull()
        }
    }

    private fun loadEmptyStreaksViewData(): StatisticsDetailStreaksViewData {
        return streaksInteractor.getEmptyStreaksViewData()
    }

    private suspend fun loadStreaksViewData(): StatisticsDetailStreaksViewData? {
        val parent = parent ?: return null

        return streaksInteractor.getStreaksViewData(
            records = parent.records,
            compareRecords = parent.compareRecords,
            showComparison = parent.comparisonFilter.isNotEmpty(),
            rangeLength = parent.rangeLength,
            rangePosition = parent.rangePosition,
            streaksType = streaksType,
            streaksGoal = streaksGoal,
            goalType = getDailyGoal(),
            compareGoalType = getCompareDailyGoal(),
        )
    }

    private fun loadStreaksTypeViewData(): List<ViewHolderType> {
        return streaksInteractor.mapToStreaksTypeViewData(streaksType)
    }

    private suspend fun loadStreaksGoalViewData(): List<ViewHolderType> {
        val parent = parent ?: return emptyList()

        return streaksInteractor.mapToStreaksGoalViewData(
            streaksGoal = streaksGoal,
            dailyGoal = getDailyGoal(),
            compareGoalType = getCompareDailyGoal(),
            rangeLength = parent.rangeLength,
        )
    }
}