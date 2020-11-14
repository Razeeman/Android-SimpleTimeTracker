package com.example.util.simpletimetracker.feature_settings.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.interactor.NotificationInteractor
import com.example.util.simpletimetracker.core.provider.PackageNameProvider
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewData.CardOrderViewData
import com.example.util.simpletimetracker.navigation.Action
import com.example.util.simpletimetracker.navigation.Notification
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.FileChooserParams
import com.example.util.simpletimetracker.navigation.params.OpenMarketParams
import com.example.util.simpletimetracker.navigation.params.SendEmailParams
import com.example.util.simpletimetracker.navigation.params.StandardDialogParams
import com.example.util.simpletimetracker.navigation.params.ToastParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val settingsMapper: SettingsMapper,
    private val packageNameProvider: PackageNameProvider,
    private val notificationInteractor: NotificationInteractor
) : ViewModel() {

    val cardOrderViewData: LiveData<CardOrderViewData> by lazy {
        MutableLiveData<CardOrderViewData>().let { initial ->
            viewModelScope.launch {
                initial.value = loadCardOrderViewData()
            }
            initial
        }
    }

    val btnCardOrderManualVisibility: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getCardOrder() == CardOrder.MANUAL
            }
            initial
        }
    }

    val showUntrackedCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getShowUntrackedInRecords()
            }
            initial
        }
    }

    val allowMultitaskingCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getAllowMultitasking()
            }
            initial
        }
    }

    val showNotificationsCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getShowNotifications()
            }
            initial
        }
    }

    val darkModeCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getDarkMode()
            }
            initial
        }
    }

    val themeChanged: LiveData<Boolean> = MutableLiveData(false)

    fun onVisible() {
        // Need to update card order because it changes on card order dialog
        viewModelScope.launch {
            updateCardOrderViewData()
        }
    }

    fun onSaveClick() {
        router.execute(
            Action.CREATE_FILE,
            FileChooserParams(::onFileCreateError)
        )
    }

    fun onRestoreClick() {
        router.navigate(
            Screen.STANDARD_DIALOG,
            StandardDialogParams(
                tag = ALERT_DIALOG_TAG,
                message = resourceRepo.getString(R.string.settings_dialog_message),
                btnPositive = resourceRepo.getString(R.string.ok),
                btnNegative = resourceRepo.getString(R.string.cancel)
            )
        )
    }

    fun onExportCsvClick() {
        router.execute(
            Action.CREATE_CSV_FILE,
            FileChooserParams(::onFileCreateError)
        )
    }

    fun onRateClick() {
        router.execute(
            action = Action.OPEN_MARKET,
            data = OpenMarketParams(
                packageName = packageNameProvider.getPackageName()
            )
        )
    }

    fun onFeedbackClick() {
        router.execute(
            action = Action.SEND_EMAIL,
            data = SendEmailParams(
                email = resourceRepo.getString(R.string.support_email),
                subject = resourceRepo.getString(R.string.support_email_subject),
                chooserTitle = resourceRepo.getString(R.string.settings_email_chooser_title),
                notHandledCallback = { R.string.message_app_not_found.let(::showMessage) }
            )
        )
    }

    fun onRecordTypeOrderSelected(position: Int) {
        val newOrder = settingsMapper.toCardOrder(position)

        (btnCardOrderManualVisibility as MutableLiveData).value = newOrder == CardOrder.MANUAL

        viewModelScope.launch {
            if (newOrder == CardOrder.MANUAL) {
                openCardOrderDialog()
            } else {
                prefsInteractor.setCardOrder(newOrder)
            }
        }
    }

    fun onCardOrderManualClick() {
        openCardOrderDialog()
    }

    fun onShowUntrackedClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getShowUntrackedInRecords()
            prefsInteractor.setShowUntrackedInRecords(newValue)
            (showUntrackedCheckbox as MutableLiveData).value = newValue
        }
    }

    fun onAllowMultitaskingClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getAllowMultitasking()
            prefsInteractor.setAllowMultitasking(newValue)
            (allowMultitaskingCheckbox as MutableLiveData).value = newValue
        }
    }

    fun onShowNotificationsClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getShowNotifications()
            prefsInteractor.setShowNotifications(newValue)
            (showNotificationsCheckbox as MutableLiveData).value = newValue
            notificationInteractor.updateNotifications()
        }
    }

    fun onDarkModeClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getDarkMode()
            prefsInteractor.setDarkMode(newValue)
            (darkModeCheckbox as MutableLiveData).value = newValue
            (themeChanged as MutableLiveData).value = true
        }
    }

    fun onChangeCardSizeClick() {
        router.navigate(Screen.CARD_SIZE_DIALOG)
    }

    fun onEditCategoriesClick() {
        router.navigate(Screen.CATEGORIES)
    }

    fun onPositiveDialogClick(tag: String?) {
        when (tag) {
            ALERT_DIALOG_TAG -> router.execute(
                Action.OPEN_FILE,
                FileChooserParams(::onFileOpenError)
            )
        }
    }

    fun onThemeChanged() {
        (themeChanged as MutableLiveData).value = false
    }

    private fun openCardOrderDialog() {
        router.navigate(Screen.CARD_ORDER_DIALOG)
    }

    private fun onFileOpenError() {
        showMessage(R.string.settings_file_open_error)
    }

    private fun onFileCreateError() {
        showMessage(R.string.settings_file_create_error)
    }

    private fun showMessage(stringResId: Int) {
        stringResId
            .let(resourceRepo::getString)
            .let { router.show(Notification.TOAST, ToastParams(it)) }
    }

    private suspend fun updateCardOrderViewData() {
        (cardOrderViewData as MutableLiveData).value = loadCardOrderViewData()
    }

    private suspend fun loadCardOrderViewData(): CardOrderViewData {
        return prefsInteractor.getCardOrder().let(settingsMapper::toCardOrderViewData)
    }

    companion object {
        private const val ALERT_DIALOG_TAG = "alert_dialog_tag"
    }
}
