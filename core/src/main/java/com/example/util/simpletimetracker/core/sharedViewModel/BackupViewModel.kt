package com.example.util.simpletimetracker.core.sharedViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.AutomaticBackupRepo
import com.example.util.simpletimetracker.core.repo.AutomaticExportRepo
import com.example.util.simpletimetracker.core.repo.DataEditRepo
import com.example.util.simpletimetracker.core.repo.PermissionRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_settings_views.SettingsBlock
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.interactor.AutomaticBackupInteractor
import com.example.util.simpletimetracker.domain.interactor.AutomaticExportInteractor
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
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingsResult
import com.example.util.simpletimetracker.navigation.params.screen.HelpDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.StandardDialogParams
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.launch

class BackupViewModel @Inject constructor(
    private val dataEditRepo: DataEditRepo,
    private val automaticBackupRepo: AutomaticBackupRepo,
    private val automaticExportRepo: AutomaticExportRepo,
    private val resourceRepo: ResourceRepo,
    private val permissionRepo: PermissionRepo,
    private val router: Router,
    private val backupInteractor: BackupInteractor,
    private val csvExportInteractor: CsvExportInteractor,
    private val icsExportInteractor: IcsExportInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val automaticBackupInteractor: AutomaticBackupInteractor,
    private val automaticExportInteractor: AutomaticExportInteractor,
    private val timeMapper: TimeMapper,
) : ViewModel() {

    val requestScreenUpdate: LiveData<Unit> = SingleLiveEvent()
    val progressVisibility: MediatorLiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(automaticBackupRepo.inProgress) { updateProgress() }
        addSource(automaticExportRepo.inProgress) { updateProgress() }
        addSource(dataEditRepo.inProgress) { updateProgress() }
    }

    private var dataExportSettingsResult: DataExportSettingsResult? = null

    fun onVisible() {
        viewModelScope.launch {
            checkForAutomaticBackupError()
        }
    }

    fun onBlockClicked(block: SettingsBlock) {
        when (block) {
            SettingsBlock.BackupSave -> onSaveClick()
            SettingsBlock.BackupAutomatic -> onAutomaticBackupClick()
            SettingsBlock.BackupRestore -> onRestoreClick()
            SettingsBlock.ExportSpreadsheet -> onExportCsvClick()
            SettingsBlock.ExportSpreadsheetAutomatic -> onAutomaticExportClick()
            SettingsBlock.ExportSpreadsheetImport -> onImportCsvClick()
            SettingsBlock.ExportSpreadsheetImportHint -> onImportCsvHelpClick()
            SettingsBlock.ExportIcs -> onExportIcsClick()
            else -> {
                // Do nothing
            }
        }
    }

    fun onPositiveDialogClick(tag: String?) {
        when (tag) {
            ALERT_DIALOG_TAG -> requestFileWork(
                requestCode = RequestCode.REQUEST_CODE_OPEN_FILE,
                work = ::onRestoreBackup,
                params = OpenFileParams(
                    type = FILE_TYPE_BIN_OPEN,
                    notHandledCallback = ::onFileOpenError,
                ),
            )
            CSV_IMPORT_ALERT_DIALOG_TAG -> requestFileWork(
                requestCode = RequestCode.REQUEST_CODE_OPEN_FILE,
                work = ::onImportCsvFile,
                params = OpenFileParams(
                    type = FILE_TYPE_CSV_OPEN,
                    notHandledCallback = ::onFileOpenError,
                ),
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
                        type = FILE_TYPE_CSV,
                        notHandledCallback = ::onFileCreateError,
                    ),
                )
            }
            ICS_EXPORT_DIALOG_TAG -> {
                requestFileWork(
                    requestCode = RequestCode.REQUEST_CODE_CREATE_FILE,
                    work = ::onSaveIcsFile,
                    params = CreateFileParams(
                        fileName = "stt_events_${getFileNameTimeStamp()}.ics",
                        type = FILE_TYPE_ICS,
                        notHandledCallback = ::onFileCreateError,
                    ),
                )
            }
        }
        dataExportSettingsResult = data
    }

    fun onFileWork() {
        viewModelScope.launch {
            checkForAutomaticBackupError()
            requestScreenUpdate.set(Unit)
        }
    }

    private fun onSaveClick() {
        requestFileWork(
            requestCode = RequestCode.REQUEST_CODE_CREATE_FILE,
            work = ::onSaveBackup,
            params = CreateFileParams(
                fileName = "stt_${getFileNameTimeStamp()}.backup",
                type = FILE_TYPE_BIN,
                notHandledCallback = ::onFileCreateError,
            ),
        )
    }

    private fun onAutomaticBackupClick() = viewModelScope.launch {
        if (loadAutomaticBackupEnabled()) {
            disableAutomaticBackup()
        } else {
            requestFileWork(
                requestCode = RequestCode.REQUEST_CODE_CREATE_FILE,
                work = ::onAutomaticBackup,
                params = CreateFileParams(
                    fileName = "stt_automatic.backup",
                    type = FILE_TYPE_BIN,
                    notHandledCallback = ::onFileCreateError,
                ),
            )
        }
    }

    private fun onAutomaticExportClick() = viewModelScope.launch {
        if (loadAutomaticExportEnabled()) {
            disableAutomaticExport()
        } else {
            requestFileWork(
                requestCode = RequestCode.REQUEST_CODE_CREATE_FILE,
                work = ::onAutomaticExport,
                params = CreateFileParams(
                    fileName = "stt_records_automatic.csv",
                    type = FILE_TYPE_CSV,
                    notHandledCallback = ::onFileCreateError,
                ),
            )
        }
    }

    private fun onRestoreClick() {
        router.navigate(
            StandardDialogParams(
                tag = ALERT_DIALOG_TAG,
                message = resourceRepo.getString(R.string.settings_dialog_message),
                btnPositive = resourceRepo.getString(R.string.ok),
                btnNegative = resourceRepo.getString(R.string.cancel),
            ),
        )
    }

    private fun onExportCsvClick() {
        router.navigate(DataExportSettingDialogParams(CSV_EXPORT_DIALOG_TAG))
    }

    private fun onImportCsvClick() {
        router.navigate(
            StandardDialogParams(
                tag = CSV_IMPORT_ALERT_DIALOG_TAG,
                message = resourceRepo.getString(R.string.archive_deletion_alert),
                btnPositive = resourceRepo.getString(R.string.ok),
                btnNegative = resourceRepo.getString(R.string.cancel),
            ),
        )
    }

    private fun onImportCsvHelpClick() {
        HelpDialogParams(
            title = resourceRepo.getString(R.string.settings_import_csv),
            text = resourceRepo.getString(R.string.settings_import_csv_help),
        ).let(router::navigate)
    }

    private fun onExportIcsClick() {
        router.navigate(DataExportSettingDialogParams(ICS_EXPORT_DIALOG_TAG))
    }

    private suspend fun checkForAutomaticBackupError() {
        val automaticBackupError = prefsInteractor.getAutomaticBackupError()
        val automaticExportError = prefsInteractor.getAutomaticExportError()

        if (automaticBackupError || automaticExportError) {
            val backupString = resourceRepo.getString(R.string.message_automatic_backup_error)
                .takeIf { automaticBackupError }
            val exportString = resourceRepo.getString(R.string.message_automatic_export_error)
                .takeIf { automaticExportError }
            val hint = resourceRepo.getString(R.string.message_automatic_error_hint)
            val message = listOfNotNull(backupString, exportString, hint)
                .joinToString(separator = " ")

            router.show(
                SnackBarParams(
                    message = message,
                    duration = SnackBarParams.Duration.Indefinite,
                ),
            )
        }

        if (automaticBackupError) prefsInteractor.setAutomaticBackupError(false)
        if (automaticExportError) prefsInteractor.setAutomaticExportError(false)
    }

    private fun onSaveBackup(uriString: String?) {
        if (uriString == null) return
        executeFileWork {
            backupInteractor.saveBackupFile(uriString)
        }
    }

    private fun onAutomaticBackup(uriString: String?) {
        viewModelScope.launch {
            if (uriString == null) {
                requestScreenUpdate.set(Unit)
                return@launch
            }

            val exportUri = prefsInteractor.getAutomaticExportUri()
            permissionRepo.releasePersistableUriPermissions(exportUri)

            if (permissionRepo.takePersistableUriPermission(uriString)) {
                prefsInteractor.setAutomaticBackupUri(uriString)
                automaticBackupInteractor.schedule()
            } else {
                prefsInteractor.setAutomaticBackupUri("")
                onFileCreateError()
            }

            requestScreenUpdate.set(Unit)
        }
    }

    private fun onAutomaticExport(uriString: String?) {
        viewModelScope.launch {
            if (uriString == null) {
                requestScreenUpdate.set(Unit)
                return@launch
            }

            val backupUri = prefsInteractor.getAutomaticBackupUri()
            permissionRepo.releasePersistableUriPermissions(backupUri)

            if (permissionRepo.takePersistableUriPermission(uriString)) {
                prefsInteractor.setAutomaticExportUri(uriString)
                automaticExportInteractor.schedule()
            } else {
                prefsInteractor.setAutomaticExportUri("")
                onFileCreateError()
            }

            requestScreenUpdate.set(Unit)
        }
    }

    private fun disableAutomaticBackup() {
        viewModelScope.launch {
            prefsInteractor.setAutomaticBackupUri("")
            prefsInteractor.setAutomaticBackupError(false)
            automaticBackupInteractor.cancel()
            requestScreenUpdate.set(Unit)
        }
    }

    private fun disableAutomaticExport() {
        viewModelScope.launch {
            prefsInteractor.setAutomaticExportUri("")
            prefsInteractor.setAutomaticExportError(false)
            automaticExportInteractor.cancel()
            requestScreenUpdate.set(Unit)
        }
    }

    private fun onRestoreBackup(uriString: String?) {
        if (uriString == null) return
        executeFileWork {
            backupInteractor.restoreBackupFile(uriString)
        }
    }

    private fun onSaveCsvFile(uriString: String?) {
        if (uriString == null) return
        executeFileWork {
            csvExportInteractor.saveCsvFile(
                uriString = uriString,
                range = getRange(),
            )
        }
    }

    private fun onImportCsvFile(uriString: String?) {
        if (uriString == null) return
        executeFileWork {
            csvExportInteractor.importCsvFile(uriString)
        }
    }

    private fun onSaveIcsFile(uriString: String?) {
        if (uriString == null) return
        executeFileWork {
            icsExportInteractor.saveIcsFile(
                uriString = uriString,
                range = getRange(),
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
        doWork: suspend () -> ResultCode,
    ) = viewModelScope.launch {
        progressVisibility.set(true)

        val resultCode = doWork()
        resultCode.message.let(::showMessage)

        progressVisibility.set(false)
    }

    private fun onFileOpenError() {
        showMessage(resourceRepo.getString(R.string.settings_file_open_error))
    }

    private fun onFileCreateError() {
        showMessage(resourceRepo.getString(R.string.settings_file_create_error))
    }

    private fun showMessage(string: String) {
        val params = SnackBarParams(message = string)
        router.show(params)
    }

    private fun updateProgress() {
        val visible = dataEditRepo.inProgress.value.orFalse() ||
            automaticBackupRepo.inProgress.value.orFalse() ||
            automaticExportRepo.inProgress.value.orFalse()

        progressVisibility.set(visible)
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

    private suspend fun getLastSaveString(timestamp: Long): String {
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()

        return timestamp
            .takeUnless { it == 0L }
            ?.let { timeMapper.formatDateTimeYear(it, useMilitaryTime) }
            ?.let { resourceRepo.getString(R.string.settings_automatic_last_save) + " " + it }
            .orEmpty()
    }

    private suspend fun loadAutomaticBackupEnabled(): Boolean {
        return prefsInteractor.getAutomaticBackupUri().isNotEmpty()
    }

    private suspend fun loadAutomaticExportEnabled(): Boolean {
        return prefsInteractor.getAutomaticExportUri().isNotEmpty()
    }

    companion object {
        private const val CSV_EXPORT_DIALOG_TAG = "csv_export_dialog_tag"
        private const val ICS_EXPORT_DIALOG_TAG = "ics_export_dialog_tag"
        private const val ALERT_DIALOG_TAG = "alert_dialog_tag"
        private const val CSV_IMPORT_ALERT_DIALOG_TAG = "csv_import_alert_dialog_tag"

        private const val FILE_TYPE_BIN = "application/x-binary"
        private const val FILE_TYPE_BIN_OPEN = "application/*"
        private const val FILE_TYPE_CSV = "text/csv"
        private const val FILE_TYPE_CSV_OPEN = "text/*"
        private const val FILE_TYPE_ICS = "application/ics"
    }
}