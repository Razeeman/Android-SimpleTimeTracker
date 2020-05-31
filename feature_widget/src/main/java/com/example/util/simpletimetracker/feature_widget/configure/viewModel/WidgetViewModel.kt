package com.example.util.simpletimetracker.feature_widget.configure.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.feature_widget.configure.extra.WidgetExtra
import com.example.util.simpletimetracker.feature_widget.configure.mapper.WidgetViewDataMapper
import com.example.util.simpletimetracker.feature_widget.configure.viewData.WidgetRecordTypeViewData
import kotlinx.coroutines.launch
import javax.inject.Inject

class WidgetViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val widgetViewDataMapper: WidgetViewDataMapper
) : ViewModel() {

    lateinit var extra: WidgetExtra

    val recordTypes: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadRecordTypesViewData() }
            initial
        }
    }
    val handled: LiveData<Int> = MutableLiveData()

    fun onRecordTypeClick(item: WidgetRecordTypeViewData) {
        viewModelScope.launch {
            prefsInteractor.setWidget(extra.widgetId, item.id)
            widgetInteractor.updateWidget(extra.widgetId)
            (handled as MutableLiveData).value = extra.widgetId
        }
    }

    private suspend fun loadRecordTypesViewData(): List<ViewHolderType> {
        return recordTypeInteractor.getAll()
            .filter { !it.hidden }
            .map(widgetViewDataMapper::map)
            .takeUnless { it.isEmpty() }
            ?: listOf(widgetViewDataMapper.mapToEmpty())
    }
}
