package com.example.util.simpletimetracker.feature_dialogs.archive.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_dialogs.archive.interactor.ArchiveDialogViewDataInteractor
import com.example.util.simpletimetracker.navigation.params.screen.ArchiveDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveDialogViewModel @Inject constructor(
    private val archiveDialogViewDataInteractor: ArchiveDialogViewDataInteractor
) : ViewModel() {

    lateinit var extra: ArchiveDialogParams

    val viewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadViewData(extra)
            }
            initial
        }
    }

    private suspend fun loadViewData(params: ArchiveDialogParams): List<ViewHolderType> {
        return when (params) {
            is ArchiveDialogParams.Activity ->
                archiveDialogViewDataInteractor.getActivityViewData(params.id)
            is ArchiveDialogParams.RecordTag ->
                archiveDialogViewDataInteractor.getRecordTagViewData(params.id)
        }
    }
}
