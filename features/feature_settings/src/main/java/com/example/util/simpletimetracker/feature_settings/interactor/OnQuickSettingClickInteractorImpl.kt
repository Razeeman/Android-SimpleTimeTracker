package com.example.util.simpletimetracker.feature_settings.interactor

import com.example.util.simpletimetracker.core.repo.PermissionRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.widget.model.QuickSettingsWidgetType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.api.OnQuickSettingClickInteractor
import com.example.util.simpletimetracker.feature_settings.api.OnSettingChangedInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.ToastParams
import javax.inject.Inject

class OnQuickSettingClickInteractorImpl @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val onSettingChangedInteractor: OnSettingChangedInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
    private val permissionRepo: PermissionRepo,
    private val resourceRepo: ResourceRepo,
    private val router: Router,
) : OnQuickSettingClickInteractor {

    override suspend fun execute(type: QuickSettingsWidgetType) {
        when (type) {
            QuickSettingsWidgetType.AllowMultitasking -> {
                val newValue = !prefsInteractor.getAllowMultitasking()
                prefsInteractor.setAllowMultitasking(newValue)
                onSettingChangedInteractor.onAllowMultitaskingChange()
            }
            QuickSettingsWidgetType.ShowRecordTagSelection -> {
                val newValue = !prefsInteractor.getShowRecordTagSelection()
                prefsInteractor.setShowRecordTagSelection(newValue)
                externalViewsInteractor.onShowRecordTagSelectionChange()
            }
            QuickSettingsWidgetType.ShowTimerNotifications -> {
                val enable = !prefsInteractor.getShowNotifications()
                val permissionGranted = permissionRepo.areNotificationsEnabled()
                prefsInteractor.setShowNotifications(enable && permissionGranted)
                externalViewsInteractor.onShowTimerNotificationsChange()
                if (enable && !permissionGranted) {
                    // TODO SETTINGS show snack for shortcuts
                    val params = ToastParams(resourceRepo.getString(R.string.post_notifications))
                    router.show(params)
                }
            }
            QuickSettingsWidgetType.RetroactiveTracking -> {
                val newValue = !prefsInteractor.getRetroactiveTrackingMode()
                prefsInteractor.setRetroactiveTrackingMode(newValue)
                onSettingChangedInteractor.onRetroactiveTrackingModeChange()
            }
            QuickSettingsWidgetType.ShowCommentInput -> {
                val newValue = !prefsInteractor.getShowCommentInput()
                prefsInteractor.setShowCommentInput(newValue)
                externalViewsInteractor.onShowCommentInputChange()
            }
        }
    }
}
