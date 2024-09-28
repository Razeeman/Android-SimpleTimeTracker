package com.example.util.simpletimetracker.ui

import androidx.lifecycle.MediatorLiveData
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.repo.AutomaticBackupRepo
import com.example.util.simpletimetracker.core.repo.AutomaticExportRepo
import com.example.util.simpletimetracker.core.repo.DataEditRepo
import com.example.util.simpletimetracker.core.repo.FileWorkRepo
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsFileWorkDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val dataEditRepo: DataEditRepo,
    private val automaticBackupRepo: AutomaticBackupRepo,
    private val automaticExportRepo: AutomaticExportRepo,
    private val fileWorkRepo: FileWorkRepo,
    private val settingsFileWorkDelegate: SettingsFileWorkDelegate,
) : BaseViewModel() {

    val progressVisibility: MediatorLiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(automaticBackupRepo.inProgress) { updateProgress() }
        addSource(automaticExportRepo.inProgress) { updateProgress() }
        addSource(dataEditRepo.inProgress) { updateProgress() }
        addSource(fileWorkRepo.inProgress) { updateProgress() }
    }

    fun onVisible() {
        settingsFileWorkDelegate.onAppVisible()
    }

    private fun updateProgress() {
        val visible = dataEditRepo.inProgress.value.orFalse() ||
            automaticBackupRepo.inProgress.value.orFalse() ||
            automaticExportRepo.inProgress.value.orFalse() ||
            fileWorkRepo.inProgress.value.orFalse()

        progressVisibility.set(visible)
        // Here to check that if automatic update finishes with error while app is opened.
        settingsFileWorkDelegate.onFileWork()
    }
}
