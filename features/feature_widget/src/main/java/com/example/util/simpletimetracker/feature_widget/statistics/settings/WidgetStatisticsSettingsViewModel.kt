package com.example.util.simpletimetracker.feature_widget.statistics.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toModel
import com.example.util.simpletimetracker.core.interactor.ChartFilterViewDataInteractor
import com.example.util.simpletimetracker.core.mapper.ChartFilterViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RangeTitleMapper
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.core.viewData.ChartFilterTypeViewData
import com.example.util.simpletimetracker.core.viewData.RangeSelectionOptionsListItem
import com.example.util.simpletimetracker.domain.base.ARCHIVED_BUTTON_ITEM_ID
import com.example.util.simpletimetracker.domain.category.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.category.model.Category
import com.example.util.simpletimetracker.domain.daysOfWeek.interactor.GetProcessedLastDaysCountInteractor
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.widget.model.StatisticsWidgetData
import com.example.util.simpletimetracker.domain.widget.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.widget.model.WidgetDataFilterType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_widget.statistics.interactor.WidgetStatisticsIdsInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetStatisticsSettingsViewModel @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val chartFilterViewDataMapper: ChartFilterViewDataMapper,
    private val rangeViewDataMapper: RangeViewDataMapper,
    private val rangeTitleMapper: RangeTitleMapper,
    private val chartFilterViewDataInteractor: ChartFilterViewDataInteractor,
    private val getProcessedLastDaysCountInteractor: GetProcessedLastDaysCountInteractor,
    private val widgetStatisticsIdsInteractor: WidgetStatisticsIdsInteractor,
) : BaseViewModel() {

    lateinit var extra: WidgetStatisticsSettingsExtra

    val filterTypeViewData: LiveData<List<ViewHolderType>> by lazySuspend {
        initializeWidgetData()
        loadFilterTypeViewData()
    }
    val types: LiveData<List<ViewHolderType>> by lazySuspend {
        initializeWidgetData()
        updateTypesViewData()
        listOf(LoaderViewData())
    }
    val title: LiveData<String> by lazySuspend {
        initializeWidgetData()
        loadTitle()
    }
    val doNotIncludeNewItems: LiveData<Boolean> by lazySuspend {
        initializeWidgetData()
        loadDoNotIncludeNewItems()
    }
    val handled: LiveData<Int> = MutableLiveData()

    private var recordTypesCache: List<RecordType>? = null
    private var categoriesCache: List<Category>? = null
    private var recordTagsCache: List<RecordTag>? = null
    private var initialized: Boolean = false
    private var isArchivedShown: Boolean = false

    private var widgetData: StatisticsWidgetData = StatisticsWidgetData(
        chartFilterType = ChartFilterType.ACTIVITY,
        rangeLength = RangeLength.Day,
        typeIds = emptySet(),
        categoryIds = emptySet(),
        tagIds = emptySet(),
        filteringType = WidgetDataFilterType.FILTER,
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

    fun onRecordTypeClick(item: RecordTypeViewData) = viewModelScope.launch {
        if (item.id == ARCHIVED_BUTTON_ITEM_ID) {
            isArchivedShown = !isArchivedShown
        } else {
            val oldIds = widgetData.typeIds.toMutableList()
            widgetData = widgetData.copy(
                typeIds = oldIds.apply { addOrRemove(item.id) }.toSet(),
            )
        }
        updateTypesViewData()
    }

    fun onCategoryClick(item: CategoryViewData) {
        when (item) {
            is CategoryViewData.Category -> {
                val oldIds = widgetData.categoryIds.toMutableList()
                widgetData = widgetData.copy(
                    categoryIds = oldIds.apply { addOrRemove(item.id) }.toSet(),
                )
            }
            is CategoryViewData.Record -> {
                if (item.id == ARCHIVED_BUTTON_ITEM_ID) {
                    isArchivedShown = !isArchivedShown
                } else {
                    val oldIds = widgetData.tagIds.toMutableList()
                    widgetData = widgetData.copy(
                        tagIds = oldIds.apply { addOrRemove(item.id) }.toSet(),
                    )
                }
            }
        }
        updateTypesViewData()
    }

    fun onShowAllClick() {
        when (widgetData.filteringType) {
            WidgetDataFilterType.FILTER -> removeAllIds()
            WidgetDataFilterType.SELECT -> addAllIds()
        }
    }

    fun onHideAllClick() {
        when (widgetData.filteringType) {
            WidgetDataFilterType.FILTER -> addAllIds()
            WidgetDataFilterType.SELECT -> removeAllIds()
        }
    }

    fun onRangeSelected(id: RangeSelectionOptionsListItem) {
        when (id) {
            is RangeSelectionOptionsListItem.Simple -> {
                val newRange = id.rangeLengthParams.toModel()
                widgetData = widgetData.copy(rangeLength = newRange)
                updateTitle()
            }
            is RangeSelectionOptionsListItem.Last -> {
                onSelectLastDaysClick()
            }
            RangeSelectionOptionsListItem.Custom,
            RangeSelectionOptionsListItem.SelectDate,
            -> Unit
        }
    }

    fun onCountSet(count: Long, tag: String?) = viewModelScope.launch {
        if (tag != LAST_DAYS_COUNT_TAG) return@launch

        val lastDaysCount = getProcessedLastDaysCountInteractor.execute(count)
        val newRange = RangeLength.Last(lastDaysCount)
        widgetData = widgetData.copy(rangeLength = newRange)
        updateTitle()
    }

    fun onDoNotIncludeNewItemsClick() = viewModelScope.launch {
        val newState = when (widgetData.filteringType) {
            WidgetDataFilterType.FILTER -> WidgetDataFilterType.SELECT
            WidgetDataFilterType.SELECT -> WidgetDataFilterType.FILTER
        }

        // Revert all ids.
        val newTypeIds = widgetStatisticsIdsInteractor.getAllTypeIds(
            getTypesCache().map(RecordType::id).toSet(),
        ).filter { it !in widgetData.typeIds }.toSet()
        val newCategoryIds = widgetStatisticsIdsInteractor.getAllCategoryIds(
            getCategoriesCache().map(Category::id).toSet(),
        ).filter { it !in widgetData.categoryIds }.toSet()
        val newTagIds = widgetStatisticsIdsInteractor.getAllTagIds(
            getTagsCache().map(RecordTag::id).toSet(),
        ).filter { it !in widgetData.tagIds }.toSet()

        widgetData = widgetData.copy(
            typeIds = newTypeIds,
            categoryIds = newCategoryIds,
            tagIds = newTagIds,
            filteringType = newState,
        )
        updateDoNotIncludeNewItems()
        updateTypesViewData()
    }

    fun onSaveClick() {
        viewModelScope.launch {
            prefsInteractor.setStatisticsWidget(extra.widgetId, widgetData)
            widgetInteractor.updateStatisticsWidget(extra.widgetId)
            (handled as MutableLiveData).value = extra.widgetId
        }
    }

    fun onSelectRangeClick() = viewModelScope.launch {
        val data = rangeViewDataMapper.mapToRangesOptions(
            currentRange = widgetData.rangeLength,
            addSelection = false,
            lastDaysCount = getCurrentLastDaysCount(),
        )
        router.navigate(data)
    }

    private fun removeAllIds() = viewModelScope.launch {
        widgetData = when (widgetData.chartFilterType) {
            ChartFilterType.ACTIVITY -> widgetData.copy(typeIds = emptySet())
            ChartFilterType.CATEGORY -> widgetData.copy(categoryIds = emptySet())
            ChartFilterType.RECORD_TAG -> widgetData.copy(tagIds = emptySet())
        }
        updateTypesViewData()
    }

    private fun addAllIds() = viewModelScope.launch {
        widgetData = when (widgetData.chartFilterType) {
            ChartFilterType.ACTIVITY -> {
                val newIds = widgetStatisticsIdsInteractor.getAllTypeIds(
                    getTypesCache().map(RecordType::id).toSet(),
                )
                widgetData.copy(typeIds = newIds)
            }
            ChartFilterType.CATEGORY -> {
                val newIds = widgetStatisticsIdsInteractor.getAllCategoryIds(
                    getCategoriesCache().map(Category::id).toSet(),
                )
                widgetData.copy(categoryIds = newIds)
            }
            ChartFilterType.RECORD_TAG -> {
                val newIds = widgetStatisticsIdsInteractor.getAllTagIds(
                    getTagsCache().map(RecordTag::id).toSet(),
                )
                widgetData.copy(tagIds = newIds)
            }
        }
        updateTypesViewData()
    }

    private fun onSelectLastDaysClick() = viewModelScope.launch {
        DurationDialogParams(
            tag = LAST_DAYS_COUNT_TAG,
            value = DurationDialogParams.Value.Count(
                getCurrentLastDaysCount().toLong(),
            ),
            hideDisableButton = true,
        ).let(router::navigate)
    }

    private suspend fun getCurrentLastDaysCount(): Int {
        return (widgetData.rangeLength as? RangeLength.Last)?.days
            ?: prefsInteractor.getStatisticsWidgetLastDays(extra.widgetId)
    }

    private suspend fun initializeWidgetData() {
        if (initialized) return
        widgetData = prefsInteractor.getStatisticsWidget(extra.widgetId)
        initialized = true
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

    private suspend fun getActualFilteredIds(): List<Long> {
        return widgetStatisticsIdsInteractor.getActualFilteredIds(
            widgetData = widgetData,
            typeIds = { getTypesCache().map(RecordType::id).toSet() },
            categoryIds = { getCategoriesCache().map(Category::id).toSet() },
            tagIds = { getTagsCache().map(RecordTag::id).toSet() },
        ).toList()
    }

    private fun updateRecordTypesViewData() = viewModelScope.launch {
        val data = loadRecordTypesViewData()
        types.set(data)
    }

    private suspend fun loadRecordTypesViewData(): List<ViewHolderType> {
        return chartFilterViewDataInteractor.loadRecordTypesViewData(
            types = getTypesCache(),
            typeIdsFiltered = getActualFilteredIds(),
            isArchivedShown = isArchivedShown,
        )
    }

    private fun updateCategoriesViewData() = viewModelScope.launch {
        val data = loadCategoriesViewData()
        types.set(data)
    }

    private suspend fun loadCategoriesViewData(): List<ViewHolderType> {
        return chartFilterViewDataInteractor.loadCategoriesViewData(
            categories = getCategoriesCache(),
            categoryIdsFiltered = getActualFilteredIds(),
        )
    }

    private fun updateTagsViewData() = viewModelScope.launch {
        val data = loadTagsViewData()
        types.set(data)
    }

    private suspend fun loadTagsViewData(): List<ViewHolderType> {
        return chartFilterViewDataInteractor.loadTagsViewData(
            tags = getTagsCache(),
            types = getTypesCache(),
            recordTagsFiltered = getActualFilteredIds(),
            isArchivedShown = isArchivedShown,
        )
    }

    private fun updateTitle() = viewModelScope.launch {
        title.set(loadTitle())
    }

    private suspend fun loadTitle(): String {
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        return rangeTitleMapper.mapToTitle(
            rangeLength = widgetData.rangeLength,
            position = 0,
            startOfDayShift = startOfDayShift,
            firstDayOfWeek = firstDayOfWeek,
        )
    }

    private fun updateDoNotIncludeNewItems() = viewModelScope.launch {
        doNotIncludeNewItems.set(loadDoNotIncludeNewItems())
    }

    private fun loadDoNotIncludeNewItems(): Boolean {
        return widgetData.filteringType == WidgetDataFilterType.SELECT
    }

    companion object {
        private const val LAST_DAYS_COUNT_TAG = "widget_statistics_last_days_count_tag"
    }
}
