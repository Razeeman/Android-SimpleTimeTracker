package com.example.util.simpletimetracker.feature_widget.quickSettings.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.widget.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.widget.model.QuickSettingsWidgetType
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
        configure(QuickSettingsWidgetType.AllowMultitasking)
    }

    fun onShowRecordTagSelectionClicked() {
        configure(QuickSettingsWidgetType.ShowRecordTagSelection)
    }

    fun onShowTimerNotificationsClicked() {
        configure(QuickSettingsWidgetType.ShowTimerNotifications)
    }

    fun onRetroactiveTrackingClicked() {
        configure(QuickSettingsWidgetType.RetroactiveTracking)
    }

    fun onShowCommentInputClicked() {
        configure(QuickSettingsWidgetType.ShowCommentInput)
    }

    private fun configure(type: QuickSettingsWidgetType) {
        viewModelScope.launch {
            prefsInteractor.setQuickSettingsWidget(
                widgetId = extra.widgetId,
                data = type,
            )
            widgetInteractor.updateQuickSettingsWidget(extra.widgetId)
            handled.set(extra.widgetId)
        }
    }
}
