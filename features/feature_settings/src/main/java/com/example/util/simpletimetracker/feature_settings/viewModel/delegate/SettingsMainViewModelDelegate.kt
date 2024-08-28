package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.LanguageInteractor
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsMainViewDataInteractor
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ArchiveParams
import com.example.util.simpletimetracker.navigation.params.screen.CategoriesParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsMainViewModelDelegate @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val languageInteractor: LanguageInteractor,
    private val settingsMapper: SettingsMapper,
    private val widgetInteractor: WidgetInteractor,
    private val settingsMainViewDataInteractor: SettingsMainViewDataInteractor,
) : ViewModelDelegate() {

    val themeChanged: SingleLiveEvent<Boolean> = SingleLiveEvent()

    private var parent: SettingsParent? = null

    fun init(parent: SettingsParent) {
        this.parent = parent
    }

    suspend fun getViewData(): List<ViewHolderType> {
        return settingsMainViewDataInteractor.execute()
    }

    fun onBlockClicked(block: SettingsBlock) {
        when (block) {
            SettingsBlock.Categories -> onEditCategoriesClick()
            SettingsBlock.Archive -> onArchiveClick()
            SettingsBlock.AllowMultitasking -> onAllowMultitaskingClicked()
            else -> {
                // Do nothing
            }
        }
    }

    fun onSpinnerPositionSelected(block: SettingsBlock, position: Int) {
        when (block) {
            SettingsBlock.DarkMode -> onDarkModeSelected(position)
            SettingsBlock.Language -> onLanguageSelected(position)
            else -> {
                // Do nothing
            }
        }
    }

    private fun onEditCategoriesClick() {
        router.navigate(CategoriesParams)
    }

    private fun onArchiveClick() {
        router.navigate(ArchiveParams)
    }

    private fun onAllowMultitaskingClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getAllowMultitasking()
            prefsInteractor.setAllowMultitasking(newValue)
            widgetInteractor.updateWidgets(listOf(WidgetType.QUICK_SETTINGS))
            parent?.updateContent()
        }
    }

    private fun onDarkModeSelected(position: Int) {
        delegateScope.launch {
            val currentMode = prefsInteractor.getSelectedDarkMode()
            val newMode = settingsMapper.toDarkMode(position)
            if (newMode == currentMode) return@launch

            prefsInteractor.setDarkMode(newMode)
            parent?.updateContent()
            themeChanged.set(true)
        }
    }

    private fun onLanguageSelected(position: Int) {
        delegateScope.launch {
            val newLanguage = settingsMapper.toLanguage(position)
            languageInteractor.setLanguage(newLanguage)
            parent?.updateContent()
            router.restartApp()
        }
    }
}