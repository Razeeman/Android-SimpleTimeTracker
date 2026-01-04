package com.example.util.simpletimetracker.feature_settings.customizeOptionsMenu

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.base.ContainerOptionsModel
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordsContainerUpdateInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.navigation.params.screen.CustomizeOptionsMenuDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizeOptionsMenuViewModel @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val customizeOptionsMenuMapper: CustomizeOptionsMenuMapper,
    private val customizeOptionsMenuViewDataInteractor: CustomizeOptionsMenuViewDataInteractor,
    private val recordsContainerUpdateInteractor: RecordsContainerUpdateInteractor,
) : BaseViewModel() {

    lateinit var extra: CustomizeOptionsMenuDialogParams

    val content: LiveData<List<ViewHolderType>> by lazySuspend { loadContent() }

    fun onBlockClicked(block: SettingsBlock) = viewModelScope.launch {
        val currentHiddenItems = prefsInteractor.getHiddenContainerOptions()
        val model = customizeOptionsMenuMapper.mapBlockToModel(block) ?: return@launch
        val newItems = currentHiddenItems.addOrRemove(model)
        prefsInteractor.setHiddenContainerOptions(newItems)
        updateContent()
        when (model) {
            is ContainerOptionsModel.Records -> {
                recordsContainerUpdateInteractor.sendDateSelectorUpdate()
            }
            // Updated on visible.
            is ContainerOptionsModel.Statistics -> Unit
            // Updated on screen open.
            is ContainerOptionsModel.DetailedStatistics -> Unit
        }
    }

    private suspend fun updateContent() {
        content.set(loadContent())
    }

    private suspend fun loadContent(): List<ViewHolderType> {
        return customizeOptionsMenuViewDataInteractor.execute(extra)
    }
}
