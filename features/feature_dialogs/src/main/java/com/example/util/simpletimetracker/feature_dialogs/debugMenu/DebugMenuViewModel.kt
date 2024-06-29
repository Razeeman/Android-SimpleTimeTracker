package com.example.util.simpletimetracker.feature_dialogs.debugMenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebugMenuViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
) : ViewModel() {

    fun onResetPrefsClicked() {
        viewModelScope.launch {
            prefsInteractor.clear()
            router.restartApp()
        }
    }

    fun onResetHideDefaultTypesClick() {
        viewModelScope.launch {
            prefsInteractor.clearDefaultTypesHidden()
            showSuccessMessage()
        }
    }

    fun onResetPomodoroSettingsClick() {
        viewModelScope.launch {
            prefsInteractor.clearPomodoroSettingsClick()
            showSuccessMessage()
        }
    }

    private fun showSuccessMessage() {
        router.show(
            SnackBarParams(
                message = resourceRepo.getString(R.string.debug_menu_hide_message),
                duration = SnackBarParams.Duration.Short,
                inDialog = true,
            ),
        )
    }
}
