package com.example.util.simpletimetracker.core.sharedViewModel

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.AutomaticBackupRepo
import com.example.util.simpletimetracker.core.repo.PermissionRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.AutomaticBackupInteractor
import com.example.util.simpletimetracker.domain.interactor.BackupInteractor
import com.example.util.simpletimetracker.domain.interactor.CsvExportInteractor
import com.example.util.simpletimetracker.domain.interactor.IcsExportInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.resolver.ResultCode
import com.example.util.simpletimetracker.navigation.RequestCode
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.action.ActionParams
import com.example.util.simpletimetracker.navigation.params.action.CreateFileParams
import com.example.util.simpletimetracker.navigation.params.action.OpenFileParams
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.notification.ToastParams
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingsResult
import com.example.util.simpletimetracker.navigation.params.screen.StandardDialogParams
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class BackupViewModel @Inject constructor(
    automaticBackupRepo: AutomaticBackupRepo,
    private val resourceRepo: ResourceRepo,
    private val permissionRepo: PermissionRepo,
    private val router: Router,
    private val backupInteractor: BackupInteractor,
    private val csvExportInteractor: CsvExportInteractor,
    private val icsExportInteractor: IcsExportInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val automaticBackupInteractor: AutomaticBackupInteractor,
    private val timeMapper: TimeMapper,
) : ViewModel() {

    val automaticBackupCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = loadAutomaticBackupEnabled()
            }
            initial
        }
    }
    val automaticBackupLastSaveTime: LiveData<String> by lazy {
        MutableLiveData<String>().let { initial ->
            viewModelScope.launch {
                initial.value = loadAutomaticBackupLastSaveTime()
            }
            initial
        }
    }
    val progressVisibility: LiveData<Boolean> = MutableLiveData(false)
    val automaticBackupProgress: LiveData<Boolean> = automaticBackupRepo.inProgress

    private var dataExportSettingsResult: DataExportSettingsResult? = null

    fun onVisible() {
        viewModelScope.launch {
            checkForAutomaticBackupError()
            updateAutomaticBackupEnabled()
            updateAutomaticBackupLastSaveTime()
        }
    }

    fun onSaveClick() {
        requestFileWork(
            requestCode = RequestCode.REQUEST_CODE_CREATE_FILE,
            work = ::onSaveBackup,
            params = CreateFileParams(
                fileName = "stt_${getFileNameTimeStamp()}.backup",
                type = "application/x-binary",
                notHandledCallback = ::onFileCreateError
            )
        )
    }

    fun onAutomaticBackupClick() {
        if (automaticBackupCheckbox.value == true) {
            disableAutomaticBackup()
        } else {
            requestFileWork(
                requestCode = RequestCode.REQUEST_CODE_CREATE_FILE,
                work = ::onAutomaticBackup,
                params = CreateFileParams(
                    fileName = "stt_automatic.backup",
                    type = "application/x-binary",
                    notHandledCallback = ::onFileCreateError
                )
            )
        }
    }

    fun onRestoreClick() {
        router.navigate(
            StandardDialogParams(
                tag = ALERT_DIALOG_TAG,
                message = resourceRepo.getString(R.string.settings_dialog_message),
                btnPositive = resourceRepo.getString(R.string.ok),
                btnNegative = resourceRepo.getString(R.string.cancel)
            )
        )
    }

    fun onExportCsvClick() {
        router.navigate(DataExportSettingDialogParams(CSV_EXPORT_DIALOG_TAG))
    }

    fun onExportIcsClick() {
        router.navigate(DataExportSettingDialogParams(ICS_EXPORT_DIALOG_TAG))
    }

    fun onPositiveDialogClick(tag: String?) {
        when (tag) {
            ALERT_DIALOG_TAG -> requestFileWork(
                requestCode = RequestCode.REQUEST_CODE_OPEN_FILE,
                work = ::onRestoreBackup,
                params = OpenFileParams(::onFileOpenError),
            )
        }
    }

    fun onDataExportSettingsSelected(data: DataExportSettingsResult) {
        when (data.tag) {
            CSV_EXPORT_DIALOG_TAG -> {
                requestFileWork(
                    requestCode = RequestCode.REQUEST_CODE_CREATE_FILE,
                    work = ::onSaveCsvFile,
                    params = CreateFileParams(
                        fileName = "stt_records_${getFileNameTimeStamp()}.csv",
                        type = "text/csv",
                        notHandledCallback = ::onFileCreateError
                    ),
                )
            }
            ICS_EXPORT_DIALOG_TAG -> {
                requestFileWork(
                    requestCode = RequestCode.REQUEST_CODE_CREATE_FILE,
                    work = ::onSaveIcsFile,
                    params = CreateFileParams(
                        fileName = "stt_events_${getFileNameTimeStamp()}.ics",
                        type = "application/ics",
                        notHandledCallback = ::onFileCreateError
                    ),
                )
            }
        }
        dataExportSettingsResult = data
    }

    fun onFileWork() {
        viewModelScope.launch {
            checkForAutomaticBackupError()
            updateAutomaticBackupEnabled()
            updateAutomaticBackupLastSaveTime()
        }
    }

    private suspend fun checkForAutomaticBackupError() {
        if (prefsInteractor.getAutomaticBackupError()) {
            router.show(
                SnackBarParams(
                    message = resourceRepo.getString(R.string.message_automatic_backup_error),
                    duration = SnackBarParams.Duration.Indefinite,
                )
            )
            prefsInteractor.setAutomaticBackupError(false)
        }
    }

    private fun onSaveBackup(uriString: String?) {
        if (uriString == null) return
        executeFileWork(
            successMessageId = R.string.message_backup_saved,
            errorMessageId = R.string.message_save_error,
        ) {
            backupInteractor.saveBackupFile(uriString)
        }
    }

    private fun onAutomaticBackup(uriString: String?) {
        viewModelScope.launch {
            if (uriString == null) {
                updateAutomaticBackupEnabled()
                return@launch
            }
            prefsInteractor.setAutomaticBackupUri(uriString)
            permissionRepo.releasePersistableUriPermissions(uriString)
            if (permissionRepo.takePersistableUriPermission(uriString)) {
                automaticBackupInteractor.schedule()
            } else {
                prefsInteractor.setAutomaticBackupUri("")
                onFileCreateError()
            }
            updateAutomaticBackupEnabled()
            updateAutomaticBackupLastSaveTime()
        }
    }

    private fun disableAutomaticBackup() {
        viewModelScope.launch {
            prefsInteractor.setAutomaticBackupUri("")
            prefsInteractor.setAutomaticBackupError(false)
            automaticBackupInteractor.cancel()
            updateAutomaticBackupEnabled()
            updateAutomaticBackupLastSaveTime()
        }
    }

    private fun onRestoreBackup(uriString: String?) {
        if (uriString == null) return
        executeFileWork(
            successMessageId = R.string.message_backup_restored,
            errorMessageId = R.string.message_restore_error,
        ) {
            backupInteractor.restoreBackupFile(uriString)
        }
    }

    private fun onSaveCsvFile(uriString: String?) {
        if (uriString == null) return
        executeFileWork(
            successMessageId = R.string.message_export_complete,
            errorMessageId = R.string.message_export_error,
        ) {
            csvExportInteractor.saveCsvFile(
                uriString = uriString,
                range = getRange()
            )
        }
    }

    private fun onSaveIcsFile(uriString: String?) {
        if (uriString == null) return
        executeFileWork(
            successMessageId = R.string.message_export_complete,
            errorMessageId = R.string.message_export_error,
        ) {
            icsExportInteractor.saveIcsFile(
                uriString = uriString,
                range = getRange()
            )
        }
    }

    private fun requestFileWork(
        requestCode: String,
        work: (uriString: String?) -> Unit,
        params: ActionParams,
    ) {
        router.setResultListener(requestCode) { result ->
            work(result as? String)
        }
        router.execute(params)
    }

    private fun executeFileWork(
        @StringRes successMessageId: Int,
        @StringRes errorMessageId: Int,
        doWork: suspend () -> ResultCode,
    ) = viewModelScope.launch {
        showProgress(true)

        val resultCode = doWork()
        if (resultCode == ResultCode.SUCCESS) {
            successMessageId
        } else {
            errorMessageId
        }.let(::showMessage)

        showProgress(false)
    }

    private fun onFileOpenError() {
        showMessage(R.string.settings_file_open_error)
    }

    private fun onFileCreateError() {
        showMessage(R.string.settings_file_create_error)
    }

    private fun showMessage(stringResId: Int) {
        val params = ToastParams(message = resourceRepo.getString(stringResId))
        router.show(params)
    }

    private fun showProgress(isVisible: Boolean) {
        (progressVisibility as MutableLiveData).value = isVisible
    }

    private fun getFileNameTimeStamp(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    }

    private fun getRange(): Range? {
        return dataExportSettingsResult?.range?.let {
            Range(
                timeStarted = it.rangeStart,
                timeEnded = it.rangeEnd,
            )
        }
    }

    private suspend fun updateAutomaticBackupEnabled() {
        val data = loadAutomaticBackupEnabled()
        automaticBackupCheckbox.set(data)
    }

    private suspend fun loadAutomaticBackupEnabled(): Boolean {
        return prefsInteractor.getAutomaticBackupUri().isNotEmpty()
    }

    private suspend fun updateAutomaticBackupLastSaveTime() {
        val data = loadAutomaticBackupLastSaveTime()
        automaticBackupLastSaveTime.set(data)
    }

    private suspend fun loadAutomaticBackupLastSaveTime(): String {
        return if (loadAutomaticBackupEnabled()) {
            val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()

            prefsInteractor.getAutomaticBackupLastSaveTime()
                .takeUnless { it == 0L }
                ?.let { timeMapper.formatDateTimeYear(it, useMilitaryTime) }
                ?.let { resourceRepo.getString(R.string.settings_automatic_backup_last_save) + " " + it }
                .orEmpty()
        } else {
            ""
        }
    }

    companion object {
        private const val CSV_EXPORT_DIALOG_TAG = "csv_export_dialog_tag"
        private const val ICS_EXPORT_DIALOG_TAG = "ics_export_dialog_tag"
        private const val ALERT_DIALOG_TAG = "alert_dialog_tag"
    }
}