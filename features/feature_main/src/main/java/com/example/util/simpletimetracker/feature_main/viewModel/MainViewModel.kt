package com.example.util.simpletimetracker.feature_main.viewModel

import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.allowDiskRead
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.WearInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val wearInteractor: WearInteractor,
) : BaseViewModel() {

    val initialize: Unit by lazy { syncState() }

    private fun syncState() {
        allowDiskRead { viewModelScope }.launch {
            notificationTypeInteractor.updateNotifications()
            widgetInteractor.updateWidgets()
            wearInteractor.update()
        }
    }
}
