package com.example.util.simpletimetracker.feature_widget.quickSettings.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.QuickSettingsWidgetType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class WidgetQuickSettingsConfigureViewModel @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val widgetInteractor: WidgetInteractor,
) : ViewModel() {

    lateinit var extra: WidgetQuickSettingsConfigureExtra

    val handled: LiveData<Int> = MutableLiveData()

    fun onAllowMultitaskingClicked() {
        viewModelScope.launch {
            prefsInteractor.setQuickSettingsWidget(
                widgetId = extra.widgetId,
                data = QuickSettingsWidgetType.AllowMultitasking
            )
            widgetInteractor.updateQuickSettingsWidget(extra.widgetId)
            handled.set(extra.widgetId)
        }
    }

    fun onShowRecordTagSelectionClicked() {
        viewModelScope.launch {
            prefsInteractor.setQuickSettingsWidget(
                widgetId = extra.widgetId,
                data = QuickSettingsWidgetType.ShowRecordTagSelection
            )
            widgetInteractor.updateQuickSettingsWidget(extra.widgetId)
            handled.set(extra.widgetId)
        }
    }
}
