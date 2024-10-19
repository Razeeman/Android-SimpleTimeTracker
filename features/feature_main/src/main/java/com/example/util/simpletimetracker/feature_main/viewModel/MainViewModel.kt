package com.example.util.simpletimetracker.feature_main.viewModel

import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.allowDiskRead
import com.example.util.simpletimetracker.domain.interactor.UpdateExternalViewsInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) : BaseViewModel() {

    val initialize: Unit by lazy { syncState() }

    private fun syncState() {
        allowDiskRead { viewModelScope }.launch {
            externalViewsInteractor.onAppStart()
        }
    }
}
