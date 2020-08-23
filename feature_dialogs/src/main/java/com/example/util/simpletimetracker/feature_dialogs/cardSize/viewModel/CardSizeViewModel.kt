package com.example.util.simpletimetracker.feature_dialogs.cardSize.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.DeviceRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import kotlinx.coroutines.launch
import javax.inject.Inject

class CardSizeViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val resourceRepo: ResourceRepo,
    private val deviceRepo: DeviceRepo
) : ViewModel() {

    val recordTypes: LiveData<List<ViewHolderType>> by lazy {
        updateRecordTypes()
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

    private val screenWidth: Int by lazy {
        deviceRepo.getScreenWidthInDp()
    }
    private val minCardWidth: Int by lazy {
        resourceRepo.getDimenInDp(R.dimen.record_type_card_min_width)
    }

    private var types: List<RecordType> = emptyList()
    private var currentProgress: Int = 0

    fun onProgressChanged(progress: Int) {
        currentProgress = progress
        updateRecordTypes()
    }

    fun onDismiss() {
        // TODO save progress in prefs
    }

    private fun progressToWidth(): Int {
        // TODO different interpolation
        return minCardWidth + (screenWidth - minCardWidth) * currentProgress / 100
    }

    private fun updateRecordTypes() = viewModelScope.launch {
        if (types.isEmpty()) types = loadRecordTypes()
        (recordTypes as MutableLiveData).value = types
            .map { type -> recordTypeViewDataMapper.map(type, progressToWidth()) }
    }

    private suspend fun loadRecordTypes(): List<RecordType> {
        return recordTypeInteractor.getAll()
            .filter { !it.hidden }
    }
}
