package com.example.util.simpletimetracker.feature_archive.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.feature_archive.R
import com.example.util.simpletimetracker.feature_archive.interactor.ArchiveViewDataInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ArchiveDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.StandardDialogParams
import com.example.util.simpletimetracker.navigation.params.notification.ToastParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class ArchiveViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val archiveViewDataInteractor: ArchiveViewDataInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor
) : ViewModel() {

    val viewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadViewData()
            }
            initial
        }
    }

    fun onRecordTypeClick(item: RecordTypeViewData) {
        router.navigate(
            data = ArchiveDialogParams.Activity(item.id)
        )
    }

    fun onCategoryClick(item: CategoryViewData) {
        if (item is CategoryViewData.Record) router.navigate(
            data = ArchiveDialogParams.RecordTag(item.id)
        )
    }

    fun onDeleteClick(params: ArchiveDialogParams?) {
        viewModelScope.launch {
            if (params == null) return@launch

            router.navigate(
                StandardDialogParams(
                    tag = ALERT_DIALOG_TAG,
                    data = params,
                    message = resourceRepo.getString(R.string.archive_deletion_alert),
                    btnPositive = resourceRepo.getString(R.string.archive_dialog_delete),
                    btnNegative = resourceRepo.getString(R.string.cancel)
                )
            )
        }
    }

    fun onRestoreClick(params: ArchiveDialogParams?) {
        viewModelScope.launch {
            var message = ""

            when (params) {
                is ArchiveDialogParams.Activity -> {
                    recordTypeInteractor.restore(params.id)
                    message = resourceRepo.getString(R.string.archive_activity_restored)
                }
                is ArchiveDialogParams.RecordTag -> {
                    recordTagInteractor.restore(params.id)
                    message = resourceRepo.getString(R.string.archive_tag_restored)
                }
            }

            updateViewData()
            router.show(ToastParams(message))
        }
    }

    fun onPositiveDialogClick(tag: String?, data: Any?) {
        if (tag == ALERT_DIALOG_TAG && data is ArchiveDialogParams) {
            onDelete(data)
        }
    }

    private fun onDelete(params: ArchiveDialogParams?) {
        viewModelScope.launch {
            var message = ""

            when (params) {
                is ArchiveDialogParams.Activity -> {
                    recordInteractor.removeByType(params.id)
                    recordTypeCategoryInteractor.removeAllByType(params.id)
                    recordTagInteractor.removeByType(params.id)
                    recordTypeInteractor.remove(params.id)
                    message = resourceRepo.getString(R.string.archive_activity_deleted)
                }
                is ArchiveDialogParams.RecordTag -> {
                    runningRecordInteractor.removeTag(params.id)
                    recordInteractor.removeTag(params.id)
                    recordTagInteractor.remove(params.id)
                    message = resourceRepo.getString(R.string.archive_tag_deleted)
                }
            }

            updateViewData()
            router.show(ToastParams(message))
        }
    }

    private suspend fun updateViewData() {
        val data = loadViewData()
        viewData.set(data)
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        return archiveViewDataInteractor.getViewData()
    }

    companion object {
        private const val ALERT_DIALOG_TAG = "alert_dialog_tag"
    }
}
