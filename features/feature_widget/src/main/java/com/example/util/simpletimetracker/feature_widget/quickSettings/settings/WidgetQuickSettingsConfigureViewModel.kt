package com.example.util.simpletimetracker.feature_widget.quickSettings.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.widget.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.widget.model.QuickSettingsWidgetType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
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

    val content: List<ViewHolderType> = loadViewData()

    fun onItemClick(item: WidgetQuickSettingsViewData) {
        configure(item.type)
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

    private fun loadViewData(): List<ViewHolderType> {
        return listOf(
            WidgetQuickSettingsViewData(
                type = QuickSettingsWidgetType.AllowMultitasking,
                title = R.string.settings_allow_multitasking,
                subtitle = R.string.settings_allow_multitasking_hint,
            ),
            WidgetQuickSettingsViewData(
                type = QuickSettingsWidgetType.ShowRecordTagSelection,
                title = R.string.settings_show_record_tag_selection,
                subtitle = R.string.settings_show_record_tag_selection_hint,
            ),
            WidgetQuickSettingsViewData(
                type = QuickSettingsWidgetType.ShowCommentInput,
                title = R.string.settings_show_comment_input,
                subtitle = R.string.settings_show_comment_input_hint,
            ),
            WidgetQuickSettingsViewData(
                type = QuickSettingsWidgetType.ShowTimerNotifications,
                title = R.string.settings_show_notifications,
                subtitle = R.string.settings_show_notifications_hint,
            ),
            WidgetQuickSettingsViewData(
                type = QuickSettingsWidgetType.RetroactiveTracking,
                title = R.string.settings_retroactive_tracking_mode,
                subtitle = R.string.settings_retroactive_tracking_mode_hint,
            ),
        )
    }
}
