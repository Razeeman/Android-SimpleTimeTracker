package com.example.util.simpletimetracker.feature_widget.statistics.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.ChartFilterViewDataInteractor
import com.example.util.simpletimetracker.core.mapper.ChartFilterViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.core.viewData.ChartFilterTypeViewData
import com.example.util.simpletimetracker.core.viewData.RangeViewData
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.StatisticsWidgetData
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class WidgetStatisticsSettingsViewModel @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val chartFilterViewDataMapper: ChartFilterViewDataMapper,
    private val rangeViewDataMapper: RangeViewDataMapper,
    private val chartFilterViewDataInteractor: ChartFilterViewDataInteractor,
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

    private var recordTypesCache: List<RecordType>? = null
    private var categoriesCache: List<Category>? = null
    private var recordTagsCache: List<RecordTag>? = null

    private var widgetData: StatisticsWidgetData = StatisticsWidgetData(
        chartFilterType = ChartFilterType.ACTIVITY,
        rangeLength = RangeLength.Day,
        filteredTypes = emptySet(),
        filteredCategories = emptySet(),
        filteredTags = emptySet(),
    )

    fun onFilterTypeClick(viewData: ButtonsRowViewData) {
        if (viewData !is ChartFilterTypeViewData) return
        viewModelScope.launch {
            widgetData = widgetData.copy(
                chartFilterType = viewData.filterType,
            )
            updateFilterTypeViewData()
            updateTypesViewData()
        }
    }

    fun onRecordTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            val oldIds = widgetData.filteredTypes.toMutableList()
            widgetData = widgetData.copy(
                filteredTypes = oldIds.apply { addOrRemove(item.id) }.toSet(),
            )
            updateRecordTypesViewData()
        }
    }

    fun onCategoryClick(item: CategoryViewData) {
        when (item) {
            is CategoryViewData.Category -> {
                val oldIds = widgetData.filteredCategories.toMutableList()
                widgetData = widgetData.copy(
                    filteredCategories = oldIds.apply { addOrRemove(item.id) }.toSet(),
                )
                updateCategoriesViewData()
            }
            is CategoryViewData.Record -> {
                val oldIds = widgetData.filteredTags.toMutableList()
                widgetData = widgetData.copy(
                    filteredTags = oldIds.apply { addOrRemove(item.id) }.toSet(),
                )
                updateTagsViewData()
            }
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
                ChartFilterType.RECORD_TAG -> {
                    widgetData.copy(filteredTags = emptySet())
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
                        filteredTypes = (
                            getTypesCache().map(RecordType::id) +
                                UNTRACKED_ITEM_ID
                            ).toSet(),
                    )
                }
                ChartFilterType.CATEGORY -> {
                    widgetData.copy(
                        filteredCategories = (
                            getCategoriesCache().map(Category::id) +
                                UNTRACKED_ITEM_ID +
                                UNCATEGORIZED_ITEM_ID
                            ).toSet(),
                    )
                }
                ChartFilterType.RECORD_TAG -> {
                    widgetData.copy(
                        filteredTags = (
                            getTagsCache().map(RecordTag::id) +
                                UNTRACKED_ITEM_ID +
                                UNCATEGORIZED_ITEM_ID
                            )
                            .toSet(),
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
            ChartFilterType.RECORD_TAG -> updateTagsViewData()
        }
    }

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        return when (widgetData.chartFilterType) {
            ChartFilterType.ACTIVITY -> loadRecordTypesViewData()
            ChartFilterType.CATEGORY -> loadCategoriesViewData()
            ChartFilterType.RECORD_TAG -> loadTagsViewData()
        }
    }

    private suspend fun getTypesCache(): List<RecordType> {
        return recordTypesCache ?: run {
            recordTypeInteractor.getAll().also { recordTypesCache = it }
        }
    }

    private suspend fun getCategoriesCache(): List<Category> {
        return categoriesCache ?: run {
            categoryInteractor.getAll().also { categoriesCache = it }
        }
    }

    private suspend fun getTagsCache(): List<RecordTag> {
        return recordTagsCache ?: run {
            recordTagInteractor.getAll().also { recordTagsCache = it }
        }
    }

    private fun updateRecordTypesViewData() = viewModelScope.launch {
        val data = loadRecordTypesViewData()
        types.set(data)
    }

    private suspend fun loadRecordTypesViewData(): List<ViewHolderType> {
        val typeIdsFiltered = widgetData.filteredTypes.toList()

        return chartFilterViewDataInteractor.loadRecordTypesViewData(
            types = getTypesCache(),
            typeIdsFiltered = typeIdsFiltered,
        )
    }

    private fun updateCategoriesViewData() = viewModelScope.launch {
        val data = loadCategoriesViewData()
        types.set(data)
    }

    private suspend fun loadCategoriesViewData(): List<ViewHolderType> {
        val categoryIdsFiltered = widgetData.filteredCategories.toList()

        return chartFilterViewDataInteractor.loadCategoriesViewData(
            categories = getCategoriesCache(),
            categoryIdsFiltered = categoryIdsFiltered,
        )
    }

    private fun updateTagsViewData() = viewModelScope.launch {
        val data = loadTagsViewData()
        types.set(data)
    }

    private suspend fun loadTagsViewData(): List<ViewHolderType> {
        val tagIdsFiltered = widgetData.filteredTags.toList()

        return chartFilterViewDataInteractor.loadTagsViewData(
            tags = getTagsCache(),
            types = getTypesCache(),
            recordTagsFiltered = tagIdsFiltered,
        )
    }

    private fun updateTitle() = viewModelScope.launch {
        title.set(loadTitle())
    }

    private suspend fun loadTitle(): String {
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        return rangeViewDataMapper.mapToTitle(
            rangeLength = widgetData.rangeLength,
            position = 0,
            startOfDayShift = startOfDayShift,
            firstDayOfWeek = firstDayOfWeek,
        )
    }

    private fun updateRanges() = viewModelScope.launch {
        rangeItems.set(loadRanges())
    }

    private fun loadRanges(): RangesViewData {
        return rangeViewDataMapper.mapToRanges(widgetData.rangeLength, addSelection = false)
    }
}
