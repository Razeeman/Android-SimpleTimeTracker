package com.example.util.simpletimetracker.feature_main.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.allowDiskRead
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
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
    private val prefsInteractor: PrefsInteractor,
) : BaseViewModel() {

    val initialize: Unit by lazy { syncState() }
    val isNavBatAtTheBottom: LiveData<Boolean> by lazySuspend { loadIsNavBatAtTheBottom() }

    private fun syncState() {
        allowDiskRead { viewModelScope }.launch {
            notificationTypeInteractor.updateNotifications()
            widgetInteractor.updateWidgets()
            wearInteractor.update()
        }
    }

    private suspend fun loadIsNavBatAtTheBottom(): Boolean {
        return prefsInteractor.getIsNavBarAtTheBottom()
    }
}
