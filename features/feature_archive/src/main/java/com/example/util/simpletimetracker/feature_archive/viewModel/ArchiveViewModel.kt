package com.example.util.simpletimetracker.feature_archive.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.feature_archive.R
import com.example.util.simpletimetracker.feature_archive.interactor.ArchiveViewDataInteractor
import com.example.util.simpletimetracker.feature_archive.viewData.ArchiveViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.ArchiveDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.StandardDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val archiveViewDataInteractor: ArchiveViewDataInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
) : ViewModel() {

    val viewData: LiveData<ArchiveViewData> by lazy {
        return@lazy MutableLiveData<ArchiveViewData>().let { initial ->
            viewModelScope.launch {
                initial.value = ArchiveViewData(
                    items = listOf(LoaderViewData()),
                    showHint = false,
                )
                initial.value = loadViewData()
            }
            initial
        }
    }

    fun onRecordTypeClick(item: RecordTypeViewData) {
        router.navigate(
            data = ArchiveDialogParams.Activity(item.id),
        )
    }

    fun onCategoryClick(item: CategoryViewData) {
        if (item is CategoryViewData.Record) {
            router.navigate(data = ArchiveDialogParams.RecordTag(item.id))
        }
    }

    fun onDeleteClick(params: ArchiveDialogParams?) {
        if (params == null) return

        router.navigate(
            StandardDialogParams(
                tag = ALERT_DIALOG_TAG,
                data = params,
                message = resourceRepo.getString(R.string.archive_deletion_alert),
                btnPositive = resourceRepo.getString(R.string.archive_dialog_delete),
                btnNegative = resourceRepo.getString(R.string.cancel),
            ),
        )
    }

    fun onRestoreClick(params: ArchiveDialogParams) {
        viewModelScope.launch {
            val message: String = when (params) {
                is ArchiveDialogParams.Activity -> {
                    recordTypeInteractor.restore(params.id)
                    resourceRepo.getString(R.string.archive_activity_restored)
                }
                is ArchiveDialogParams.RecordTag -> {
                    recordTagInteractor.restore(params.id)
                    resourceRepo.getString(R.string.archive_tag_restored)
                }
            }

            notificationTypeInteractor.updateNotifications()
            updateViewData()
            showMessage(message)
        }
    }

    fun onPositiveDialogClick(tag: String?, data: Any?) {
        if (tag == ALERT_DIALOG_TAG && data is ArchiveDialogParams) {
            onDelete(data)
        }
    }

    private fun onDelete(params: ArchiveDialogParams) {
        viewModelScope.launch {
            val message = when (params) {
                is ArchiveDialogParams.Activity -> {
                    recordTypeInteractor.remove(params.id)
                    val runningRecordIds = runningRecordInteractor.getAll().map { it.id }
                    notificationGoalTimeInteractor.cancel(RecordTypeGoal.IdData.Type(params.id))
                    notificationGoalTimeInteractor.checkAndReschedule(runningRecordIds + params.id)
                    widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
                    resourceRepo.getString(R.string.archive_activity_deleted)
                }
                is ArchiveDialogParams.RecordTag -> {
                    recordTagInteractor.remove(params.id)
                    resourceRepo.getString(R.string.archive_tag_deleted)
                }
            }

            updateViewData()
            showMessage(message)
        }
    }

    private fun showMessage(string: String) {
        val params = SnackBarParams(
            message = string,
            duration = SnackBarParams.Duration.Short,
            margins = SnackBarParams.Margins(
                bottom = resourceRepo.getDimenInDp(R.dimen.button_height),
            ),
        )
        router.show(params)
    }

    private suspend fun updateViewData() {
        val data = loadViewData()
        viewData.set(data)
    }

    private suspend fun loadViewData(): ArchiveViewData {
        return archiveViewDataInteractor.getViewData()
    }

    companion object {
        private const val ALERT_DIALOG_TAG = "alert_dialog_tag"
    }
}
