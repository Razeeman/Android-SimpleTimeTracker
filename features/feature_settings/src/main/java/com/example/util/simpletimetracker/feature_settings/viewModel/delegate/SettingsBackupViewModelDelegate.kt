package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsBackupViewDataInteractor
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsBackupViewModelDelegate @Inject constructor(
    private val settingsBackupViewDataInteractor: SettingsBackupViewDataInteractor,
) : ViewModelDelegate() {

    private var parent: SettingsParent? = null
    private var isCollapsed: Boolean = true

    fun init(parent: SettingsParent) {
        this.parent = parent
    }

    suspend fun getViewData(): List<ViewHolderType> {
        return settingsBackupViewDataInteractor.execute(
            isCollapsed = isCollapsed,
        )
    }

    fun onBlockClicked(block: SettingsBlock) {
        when (block) {
            SettingsBlock.BackupCollapse -> onCollapseClick()
            else -> {
                // Do nothing
            }
        }
    }

    private fun onCollapseClick() = delegateScope.launch {
        isCollapsed = isCollapsed.flip()
        parent?.updateContent()
    }
}