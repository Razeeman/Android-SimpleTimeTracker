package com.example.util.simpletimetracker.feature_widget.statistics.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.WidgetInteractor
import com.example.util.simpletimetracker.core.mapper.RangeMapper
import com.example.util.simpletimetracker.core.utils.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.core.viewData.RangeViewData
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.StatisticsWidgetData
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.mapper.ChartFilterViewDataMapper
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.viewData.ChartFilterTypeViewData
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import kotlinx.coroutines.launch
import javax.inject.Inject

class WidgetStatisticsSettingsViewModel @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val chartFilterViewDataMapper: ChartFilterViewDataMapper,
    private val rangeMapper: RangeMapper,
) : ViewModel() {

    lateinit var extra: WidgetStatisticsSettingsExtra

    val filterTypeViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initializeWidgetData()
                initial.value = loadFilterTypeViewData()
            }
            initial
        }
    }
    val types: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initializeWidgetData()
                initial.value = listOf(LoaderViewData())
                initial.value = loadTypesViewData()
            }
            initial
        }
    }
    val title: LiveData<String> by lazy {
        return@lazy MutableLiveData<String>().let { initial ->
            viewModelScope.launch {
                initializeWidgetData()
                initial.value = loadTitle()
            }
            initial
        }
    }
    val rangeItems: LiveData<RangesViewData> by lazy {
        return@lazy MutableLiveData<RangesViewData>().let { initial ->
            viewModelScope.launch {
                initializeWidgetData()
                initial.value = loadRanges()
            }
            initial
        }
    }
    val handled: LiveData<Int> = MutableLiveData()

    private var recordTypes: List<RecordType> = emptyList()
    private var categories: List<Category> = emptyList()
    private var widgetData: StatisticsWidgetData = StatisticsWidgetData(
        chartFilterType = ChartFilterType.ACTIVITY,
        rangeLength = RangeLength.Day,
        filteredTypes = emptySet(),
        filteredCategories = emptySet(),
    )

    fun onFilterTypeClick(viewData: ButtonsRowViewData) {
        if (viewData !is ChartFilterTypeViewData) return
        viewModelScope.launch {
            widgetData = widgetData.copy(
                chartFilterType = viewData.filterType
            )
            updateFilterTypeViewData()
            updateTypesViewData()
        }
    }

    fun onRecordTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            val oldIds = widgetData.filteredTypes.toMutableList()
            widgetData = widgetData.copy(
                filteredTypes = oldIds.apply { addOrRemove(item.id) }.toSet()
            )
            updateRecordTypesViewData()
        }
    }

    fun onCategoryClick(item: CategoryViewData) {
        viewModelScope.launch {
            val oldIds = widgetData.filteredCategories.toMutableList()
            widgetData = widgetData.copy(
                filteredCategories = oldIds.apply { addOrRemove(item.id) }.toSet()
            )
            updateCategoriesViewData()
        }
    }

    fun onShowAllClick() {
        viewModelScope.launch {
            widgetData = when (widgetData.chartFilterType) {
                ChartFilterType.ACTIVITY -> {
                    widgetData.copy(filteredTypes = emptySet())
                }
                ChartFilterType.CATEGORY -> {
                    widgetData.copy(filteredCategories = emptySet())
                }
            }
            updateTypesViewData()
        }
    }

    fun onHideAllClick() {
        viewModelScope.launch {
            widgetData = when (widgetData.chartFilterType) {
                ChartFilterType.ACTIVITY -> {
                    widgetData.copy(
                        filteredTypes = (recordTypes.map { it.id } + UNTRACKED_ITEM_ID).toSet()
                    )
                }
                ChartFilterType.CATEGORY -> {
                    widgetData.copy(
                        filteredTypes = categories.map { it.id }.toSet()
                    )
                }
            }
            updateTypesViewData()
        }
    }

    fun onRangeSelected(item: CustomSpinner.CustomSpinnerItem) {
        when (item) {
            is RangeViewData -> {
                widgetData = widgetData.copy(rangeLength = item.range)
                updateTitle()
                updateRanges()
            }
        }
    }

    fun onSaveClick() {
        viewModelScope.launch {
            prefsInteractor.setStatisticsWidget(extra.widgetId, widgetData)
            widgetInteractor.updateStatisticsWidget(extra.widgetId)
            (handled as MutableLiveData).value = extra.widgetId
        }
    }

    private suspend fun initializeWidgetData() {
        widgetData = prefsInteractor.getStatisticsWidget(extra.widgetId)
    }

    private fun updateFilterTypeViewData() {
        val data = loadFilterTypeViewData()
        filterTypeViewData.set(data)
    }

    private fun loadFilterTypeViewData(): List<ViewHolderType> {
        return chartFilterViewDataMapper.mapToFilterTypeViewData(widgetData.chartFilterType)
    }

    private fun updateTypesViewData() {
        when (widgetData.chartFilterType) {
            ChartFilterType.ACTIVITY -> updateRecordTypesViewData()
            ChartFilterType.CATEGORY -> updateCategoriesViewData()
        }
    }

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        return when (widgetData.chartFilterType) {
            ChartFilterType.ACTIVITY -> loadRecordTypesViewData()
            ChartFilterType.CATEGORY -> loadCategoriesViewData()
        }
    }

    private fun updateRecordTypesViewData() = viewModelScope.launch {
        val data = loadRecordTypesViewData()
        types.set(data)
    }

    private suspend fun loadRecordTypesViewData(): List<ViewHolderType> {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val typeIdsFiltered = widgetData.filteredTypes.toList()

        if (recordTypes.isEmpty()) recordTypes = recordTypeInteractor.getAll()

        return recordTypes
            .map { type ->
                chartFilterViewDataMapper
                    .mapRecordType(type, typeIdsFiltered, numberOfCards, isDarkTheme)
            }
            .plus(
                chartFilterViewDataMapper
                    .mapToUntrackedItem(typeIdsFiltered, numberOfCards, isDarkTheme)
            )
    }

    private fun updateCategoriesViewData() = viewModelScope.launch {
        val data = loadCategoriesViewData()
        types.set(data)
    }

    private suspend fun loadCategoriesViewData(): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val categoryIdsFiltered = widgetData.filteredCategories.toList()

        if (categories.isEmpty()) categories = categoryInteractor.getAll()

        return categories
            .map { type ->
                chartFilterViewDataMapper
                    .mapCategory(type, categoryIdsFiltered, isDarkTheme)
            }
            .takeUnless { it.isEmpty() }
            ?: chartFilterViewDataMapper.mapCategoriesEmpty()
    }

    private fun updateTitle() = viewModelScope.launch {
        title.set(loadTitle())
    }

    private suspend fun loadTitle(): String {
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        return rangeMapper.mapToTitle(
            rangeLength = widgetData.rangeLength,
            position = 0,
            startOfDayShift = startOfDayShift,
            firstDayOfWeek = firstDayOfWeek
        )
    }

    private fun updateRanges() = viewModelScope.launch {
        rangeItems.set(loadRanges())
    }

    private fun loadRanges(): RangesViewData {
        return rangeMapper.mapToRanges(widgetData.rangeLength, addSelection = false)
    }
}
