package com.example.util.simpletimetracker.feature_widget.grid.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.widget.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.widget.model.GridWidgetData
import com.example.util.simpletimetracker.domain.widget.model.WidgetDataFilterType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.feature_widget.common.WidgetGetActualFilteredIdsInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetGridSettingsViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val widgetGetActualFilteredIdsInteractor: WidgetGetActualFilteredIdsInteractor,
) : BaseViewModel() {

    lateinit var extra: WidgetGridSettingsExtra

    val types: LiveData<List<ViewHolderType>> by lazySuspend {
        initializeWidgetData()
        updateTypesViewData()
        listOf(LoaderViewData())
    }
    val doNotIncludeNewItems: LiveData<Boolean> by lazySuspend {
        initializeWidgetData()
        loadDoNotIncludeNewItems()
    }
    val handled: LiveData<Int> = MutableLiveData()

    private var recordTypesCache: List<RecordType>? = null
    private var initialized: Boolean = false

    private var widgetData: GridWidgetData = GridWidgetData(
        typeIds = emptySet(),
        filteringType = WidgetDataFilterType.FILTER,
    )

    fun onRecordTypeClick(item: RecordTypeViewData) = viewModelScope.launch {
        val oldIds = widgetData.typeIds.toMutableList()
        widgetData = widgetData.copy(
            typeIds = oldIds.apply { addOrRemove(item.id) }.toSet(),
        )
        updateTypesViewData()
    }

    fun onShowAllClick() = viewModelScope.launch {
        when (widgetData.filteringType) {
            WidgetDataFilterType.FILTER -> removeAllIds()
            WidgetDataFilterType.SELECT -> addAllIds()
        }
    }

    fun onHideAllClick() = viewModelScope.launch {
        when (widgetData.filteringType) {
            WidgetDataFilterType.FILTER -> addAllIds()
            WidgetDataFilterType.SELECT -> removeAllIds()
        }
    }

    fun onDoNotIncludeNewItemsClick() = viewModelScope.launch {
        val newState = when (widgetData.filteringType) {
            WidgetDataFilterType.FILTER -> WidgetDataFilterType.SELECT
            WidgetDataFilterType.SELECT -> WidgetDataFilterType.FILTER
        }

        // Revert all ids.
        val newTypeIds = getTypesCache().map(RecordType::id).toSet()
            .filter { it !in widgetData.typeIds }.toSet()

        widgetData = widgetData.copy(
            typeIds = newTypeIds,
            filteringType = newState,
        )
        updateDoNotIncludeNewItems()
        updateTypesViewData()
    }

    fun onSaveClick() {
        viewModelScope.launch {
            prefsInteractor.setGridWidgetData(extra.widgetId, widgetData)
            widgetInteractor.updateGridWidget(extra.widgetId)
            (handled as MutableLiveData).value = extra.widgetId
        }
    }

    private fun removeAllIds() = viewModelScope.launch {
        widgetData = widgetData.copy(typeIds = emptySet())
        updateTypesViewData()
    }

    private fun addAllIds() = viewModelScope.launch {
        val newIds = getTypesCache().map(RecordType::id).toSet()
        widgetData = widgetData.copy(typeIds = newIds)
        updateTypesViewData()
    }

    private suspend fun initializeWidgetData() {
        if (initialized) return
        widgetData = prefsInteractor.getGridWidgetData(extra.widgetId)
        initialized = true
    }

    private suspend fun getTypesCache(): List<RecordType> {
        return recordTypesCache ?: run {
            recordTypeInteractor.getAll()
                .filter { !it.hidden }
                .also { recordTypesCache = it }
        }
    }

    private fun updateTypesViewData() = viewModelScope.launch {
        types.set(loadTypesViewData())
    }

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val typeIdsFiltered = widgetGetActualFilteredIdsInteractor.execute(
            filterType = widgetData.filteringType,
            widgetItemIds = widgetData.typeIds,
            allItemIds = getTypesCache().map(RecordType::id).toSet(),
        )

        return getTypesCache()
            .map { type ->
                recordTypeViewDataMapper.mapFiltered(
                    recordType = type,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme,
                    isFiltered = type.id in typeIdsFiltered,
                    checkState = GoalCheckmarkView.CheckState.HIDDEN,
                    isComplete = false,
                )
            }
            .takeUnless { it.isEmpty() }
            ?: recordTypeViewDataMapper.mapToEmpty()
    }

    private fun updateDoNotIncludeNewItems() = viewModelScope.launch {
        doNotIncludeNewItems.set(loadDoNotIncludeNewItems())
    }

    private fun loadDoNotIncludeNewItems(): Boolean {
        return widgetData.filteringType == WidgetDataFilterType.SELECT
    }
}
