package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.statistics.model.StatisticsDetailTagValueSettings
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailTagValueInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.mapToViewData
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailTagValuesCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsTagValuesSettingsParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailTagValueViewModelDelegate @Inject constructor(
    private val router: Router,
    private val tagValueInteractor: StatisticsDetailTagValueInteractor,
    private val prefsInteractor: PrefsInteractor,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val viewData: LiveData<StatisticsDetailTagValuesCompositeViewData> by lazy {
        return@lazy MutableLiveData()
    }

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null
    private var chartGrouping: ChartGrouping = ChartGrouping.DAILY
    private var chartLength: ChartLength = ChartLength.TEN
    private var loadedTagSettings = StatisticsDetailTagValueSettings.getDefault()

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    override fun getViewData(): StatisticsDetailViewData? {
        return viewData.value?.viewData?.let(::mapToViewData)
    }

    fun onChartGroupingClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailGroupingViewData) return
        this.chartGrouping = viewData.chartGrouping
        updateViewData()
    }

    fun onChartLengthClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailChartLengthViewData) return
        this.chartLength = viewData.chartLength
        updateViewData()
    }

    fun onTagValuesSettingsChanged(
        result: StatisticsDetailTagValueSettings,
    ) = delegateScope.launch {
        saveSettingsForSelectedTag(result)
        updateViewData()
    }

    fun onTagValuesSettingsClick() {
        router.navigate(
            StatisticsTagValuesSettingsParams(
                tagId = loadedTagSettings.tagId,
                chartValueMode = loadedTagSettings.chartValueMode,
                multiplyDuration = loadedTagSettings.multiplyDuration,
                fillEmptyPeriods = loadedTagSettings.fillEmptyPeriods,
                yAxisZoomed = loadedTagSettings.yAxisZoomed,
            ),
        )
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

    private suspend fun loadViewData(): StatisticsDetailTagValuesCompositeViewData? {
        val parent = parent ?: return null
        val valuedTag = tagValueInteractor.getSelectedTagWithValueId(parent.filter)
            ?: return StatisticsDetailTagValuesCompositeViewData(
                viewData = emptyList(),
                appliedChartGrouping = chartGrouping,
                appliedChartLength = chartLength,
            ) // Clears data with empty list if switched to other filter.
        val settings = getSettingsForCurrentTag(valuedTag.id)

        return tagValueInteractor.getViewData(
            valuedTag = valuedTag,
            records = parent.records,
            currentChartGrouping = chartGrouping,
            currentChartLength = chartLength,
            settings = settings,
            rangeLength = parent.rangeLength,
            rangePosition = parent.rangePosition,
        )
    }

    private suspend fun getSettingsForCurrentTag(
        tagId: Long,
    ): StatisticsDetailTagValueSettings {
        if (loadedTagSettings.tagId == tagId) return loadedTagSettings
        loadedTagSettings = prefsInteractor.getStatisticsDetailTagValueSettings(tagId)
        return loadedTagSettings
    }

    private suspend fun saveSettingsForSelectedTag(
        result: StatisticsDetailTagValueSettings,
    ) {
        prefsInteractor.setStatisticsDetailTagValueSettings(result)
        loadedTagSettings = result
    }

    companion object : StatisticsDetailViewData.Key
}
