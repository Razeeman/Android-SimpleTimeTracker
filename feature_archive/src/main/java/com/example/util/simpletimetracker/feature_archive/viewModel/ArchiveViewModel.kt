package com.example.util.simpletimetracker.feature_archive.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.category.CategoryViewData
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.TagType
import com.example.util.simpletimetracker.feature_archive.R
import com.example.util.simpletimetracker.feature_archive.dialog.ArchiveDialogParams
import com.example.util.simpletimetracker.feature_archive.interactor.ArchiveViewDataInteractor
import com.example.util.simpletimetracker.navigation.Notification
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.ToastParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class ArchiveViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val archiveViewDataInteractor: ArchiveViewDataInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor
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
            screen = Screen.ARCHIVE_DIALOG,
            data = ArchiveDialogParams.Activity(item.id)
        )
    }

    fun onCategoryClick(item: CategoryViewData) {
        if (item.type == TagType.RECORD) router.navigate(
            screen = Screen.ARCHIVE_DIALOG,
            data = ArchiveDialogParams.RecordTag(item.id)
        )
    }

    fun onDeleteClick(params: ArchiveDialogParams?) {
        // TODO show confirmation and delete
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
            router.show(
                notification = Notification.TOAST,
                data = ToastParams(message)
            )
        }
    }

    private suspend fun updateViewData() {
        val data = loadViewData()
        viewData.set(data)
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        return archiveViewDataInteractor.getViewData()
    }
}
