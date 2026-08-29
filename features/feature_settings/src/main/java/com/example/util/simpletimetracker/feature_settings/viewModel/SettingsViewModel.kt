package com.example.util.simpletimetracker.feature_settings.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.domain.darkMode.interactor.ThemeChangedInteractor
import com.example.util.simpletimetracker.domain.statistics.interactor.SettingsDataUpdateInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsViewModelDelegatesProvider
import com.example.util.simpletimetracker.feature_settings.model.OptionsContent
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsUiDelegated
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsParent
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.SettingsOptionsParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val router: Router,
    private val delegatesList: SettingsViewModelDelegatesProvider,
    private val settingsDataUpdateInteractor: SettingsDataUpdateInteractor,
    private val themeChangedInteractor: ThemeChangedInteractor,
) : BaseViewModel(),
    SettingsUiDelegated by delegatesList,
    SettingsParent {

    // Avoids declaration clash.
    private val additionalDelegate by delegatesList::additionalDelegate
    private val mainDelegate by delegatesList::mainDelegate

    val content: LiveData<List<ViewHolderType>> by lazySuspend { loadContent() }
    val optionsContent: LiveData<List<ViewHolderType>> = MutableLiveData()
    val resetScreen: SingleLiveEvent<Unit> = SingleLiveEvent()
    val keepScreenOnCheckbox: LiveData<Boolean> by additionalDelegate::keepScreenOnCheckbox
    val themeChanged: SingleLiveEvent<Boolean> by mainDelegate::themeChanged

    private var activeOptionsContent: OptionsContent? = null

    init {
        delegatesList.delegates.forEach { it.init(this) }
        subscribeToUpdates()
    }

    override fun onCleared() {
        delegatesList.clear()
        super.onCleared()
    }

    fun onVisible() {
        // Update can come from quick settings widget.
        // Update can come from system settings.
        // Need to update card order because it changes on card order dialog.
        // Update after day changes.
        viewModelScope.launch { updateContent() }
    }

    fun onTabReselected(tab: NavigationTab?) {
        if (tab is NavigationTab.Settings) resetScreen.set(Unit)
    }

    fun onResetScreen() = viewModelScope.launch {
        delegatesList.collapse()
        updateContent()
    }

    fun onThemeChanged() = viewModelScope.launch {
        themeChangedInteractor.send()
    }

    override suspend fun updateContent() {
        content.set(loadContent())
        optionsContent.set(loadOptionsContent())
    }

    override fun openOptions(content: OptionsContent) {
        viewModelScope.launch {
            activeOptionsContent = content
            optionsContent.set(loadOptionsContent())
            router.navigate(SettingsOptionsParams)
        }
    }

    private fun subscribeToUpdates() = viewModelScope.launch {
        settingsDataUpdateInteractor.dataUpdated.collect { updateContent() }
    }

    private suspend fun loadContent(): List<ViewHolderType> {
        return delegatesList.loadContent()
    }

    private suspend fun loadOptionsContent(): List<ViewHolderType> {
        val content = activeOptionsContent ?: return emptyList()
        return delegatesList.loadOptionsContent(content)
    }
}
