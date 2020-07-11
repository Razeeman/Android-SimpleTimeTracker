package com.example.util.simpletimetracker.feature_settings.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.navigation.Action
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.FileChooserParams
import com.example.util.simpletimetracker.navigation.params.StandardDialogParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor
) : ViewModel() {

    val sortRecordTypes: LiveData<Boolean> by lazy {
        return@lazy MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getSortRecordTypesByColor()
            }
            initial
        }
    }

    fun onSaveClick() {
        router.navigate(
            Screen.CREATE_FILE,
            FileChooserParams(::onFileOpenError)
        )
    }

    fun onRestoreClick() {
        router.navigate(
            Screen.STANDARD_DIALOG,
            StandardDialogParams(
                tag = ALERT_DIALOG_TAG,
                message = resourceRepo.getString(R.string.settings_dialog_message),
                btnPositive = resourceRepo.getString(R.string.ok),
                btnNegative = resourceRepo.getString(R.string.cancel)
            )
        )
    }

    fun onRateClick() {
        router.execute(Action.GOOGLE_PLAY)
    }

    fun onFeedbackClick() {
        router.execute(Action.EMAIL)
    }

    fun onRecordTypeSortClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getSortRecordTypesByColor()
            prefsInteractor.setSortRecordTypesByColor(newValue)
            (sortRecordTypes as MutableLiveData).value = newValue
        }
    }

    fun onPositiveDialogClick(tag: String?) {
        when (tag) {
            ALERT_DIALOG_TAG -> router.navigate(
                Screen.OPEN_FILE,
                FileChooserParams(::onFileOpenError)
            )
        }
    }

    private fun onFileOpenError() {
        showMessage(R.string.settings_file_error)
    }

    private fun showMessage(stringResId: Int) {
        stringResId.let(resourceRepo::getString).let(router::showSystemMessage)
    }

    companion object {
        private const val ALERT_DIALOG_TAG = "alert_dialog_tag"
    }
}
