package com.example.util.simpletimetracker.feature_dialogs.duration.viewModel

import androidx.lifecycle.ViewModel
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_dialogs.duration.extra.DurationPickerExtra
import javax.inject.Inject

class DurationPickerViewModel @Inject constructor(
    private val prefsInteractor: PrefsInteractor
) : ViewModel() {

    lateinit var extra: DurationPickerExtra

    fun onTextChanged(text: String) {

    }

    fun onSaveClick() {

    }

    fun onDisableClick() {

    }
}
