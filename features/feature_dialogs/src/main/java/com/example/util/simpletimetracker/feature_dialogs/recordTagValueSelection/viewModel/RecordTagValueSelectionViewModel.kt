package com.example.util.simpletimetracker.feature_dialogs.recordTagValueSelection.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_dialogs.recordTagValueSelection.interactor.RecordTagValueSelectionViewDataInteractor
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagValueSelectionParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordTagValueSelectionViewModel @Inject constructor(
    private val recordTagValueSelectionViewDataInteractor: RecordTagValueSelectionViewDataInteractor,
) : ViewModel() {

    lateinit var extra: RecordTagValueSelectionParams

    val viewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = loadViewData(fromCommentChange = false)
                keyboardVisibility.set(true)
            }
            initial
        }
    }
    val keyboardVisibility: LiveData<Boolean> = MutableLiveData()
    val onDataSelected: LiveData<Double> = MutableLiveData()

    private var newValue: Double? = null

    fun onValueChange(valueText: String) {
        val value = valueText.toDoubleOrNull()?.takeIf { it.isFinite() }
        if (value != newValue) {
            newValue = value
            updateViewData(fromCommentChange = true)
        }
    }

    fun onKeyboardButtonClick() {
        sendResult()
    }

    fun onSaveClick() {
        sendResult()
    }

    private fun sendResult() {
        newValue?.let { onDataSelected.set(it) }
    }

    @Suppress("SameParameterValue")
    private fun updateViewData(
        fromCommentChange: Boolean = false,
    ) = viewModelScope.launch {
        val data = loadViewData(
            fromCommentChange = fromCommentChange,
        )
        viewData.set(data)
    }

    private fun loadViewData(
        fromCommentChange: Boolean,
    ): List<ViewHolderType> {
        return recordTagValueSelectionViewDataInteractor.loadViewData(
            value = newValue,
            fromCommentChange = fromCommentChange,
        )
    }
}
