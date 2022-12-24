package com.example.util.simpletimetracker.feature_main.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private var notificationTypeInteractor: NotificationTypeInteractor,
    private var widgetInteractor: WidgetInteractor,
) : ViewModel() {

    val initialize: Unit by lazy { syncState() }

    private fun syncState() {
        viewModelScope.launch {
            notificationTypeInteractor.updateNotifications()
            widgetInteractor.updateWidgets()
        }
    }
}
