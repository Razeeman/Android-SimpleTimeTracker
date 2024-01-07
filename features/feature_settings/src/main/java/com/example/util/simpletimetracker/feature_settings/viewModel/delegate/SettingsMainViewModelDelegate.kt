package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.LanguageInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
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

    val themeChanged: SingleLiveEvent<Boolean> = SingleLiveEvent()

    private var parent: SettingsParent? = null

    fun init(parent: SettingsParent) {
        this.parent = parent
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
            parent?.updateContent()
        }
    }

    fun onDarkModeSelected(position: Int) {
        delegateScope.launch {
            val currentMode = prefsInteractor.getSelectedDarkMode()
            val newMode = settingsMapper.toDarkMode(position)
            if (newMode == currentMode) return@launch

            prefsInteractor.setDarkMode(newMode)
            parent?.updateContent()
            themeChanged.set(true)
        }
    }

    fun onLanguageSelected(position: Int) {
        delegateScope.launch {
            val newLanguage = settingsMapper.toLanguage(position)
            languageInteractor.setLanguage(newLanguage)
            parent?.updateContent()
            router.restartApp()
        }
    }
}