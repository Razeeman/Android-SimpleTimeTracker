package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.LanguageInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewData.DarkModeViewData
import com.example.util.simpletimetracker.feature_settings.viewData.LanguageViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ArchiveParams
import com.example.util.simpletimetracker.navigation.params.screen.CategoriesParams
import com.example.util.simpletimetracker.navigation.params.screen.DataEditParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsMainViewModelDelegate @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val languageInteractor: LanguageInteractor,
    private val settingsMapper: SettingsMapper,
    private val widgetInteractor: WidgetInteractor,
) : ViewModelDelegate() {

    val allowMultitaskingCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getAllowMultitasking() }
    val darkModeViewData: LiveData<DarkModeViewData>
        by lazySuspend { loadDarkModeViewData() }
    val languageViewData: LiveData<LanguageViewData>
        by lazySuspend { loadLanguageViewData() }
    val themeChanged: SingleLiveEvent<Boolean> = SingleLiveEvent()

    fun onVisible() {
        delegateScope.launch {
            // Update can come from quick settings widget
            allowMultitaskingCheckbox.set(prefsInteractor.getAllowMultitasking())

            // Update can come from system settings
            updateLanguageViewData()
        }
    }

    fun onEditCategoriesClick() {
        router.navigate(CategoriesParams)
    }

    fun onArchiveClick() {
        router.navigate(ArchiveParams)
    }

    fun onDataEditClick() {
        router.navigate(DataEditParams)
    }

    fun onAllowMultitaskingClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getAllowMultitasking()
            prefsInteractor.setAllowMultitasking(newValue)
            widgetInteractor.updateWidgets(listOf(WidgetType.QUICK_SETTINGS))
            allowMultitaskingCheckbox.set(newValue)
        }
    }

    fun onDarkModeSelected(position: Int) {
        delegateScope.launch {
            val currentMode = prefsInteractor.getSelectedDarkMode()
            val newMode = settingsMapper.toDarkMode(position)
            if (newMode == currentMode) return@launch

            prefsInteractor.setDarkMode(newMode)
            updateDarkModeViewData()
            themeChanged.set(true)
        }
    }

    fun onLanguageSelected(position: Int) {
        val newLanguage = settingsMapper.toLanguage(position)
        languageInteractor.setLanguage(newLanguage)
        updateLanguageViewData()
        router.restartApp()
    }

    private suspend fun updateDarkModeViewData() {
        darkModeViewData.set(loadDarkModeViewData())
    }

    private suspend fun loadDarkModeViewData(): DarkModeViewData {
        return prefsInteractor.getSelectedDarkMode()
            .let(settingsMapper::toDarkModeViewData)
    }

    private fun updateLanguageViewData() {
        languageViewData.set(loadLanguageViewData())
    }

    private fun loadLanguageViewData(): LanguageViewData {
        return languageInteractor.getCurrentLanguage()
            .let(settingsMapper::toLanguageViewData)
    }
}