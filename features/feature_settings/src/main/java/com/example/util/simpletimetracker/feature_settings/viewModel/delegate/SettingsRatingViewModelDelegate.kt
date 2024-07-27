package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.provider.ApplicationDataProvider
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsRatingViewDataInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.action.OpenMarketParams
import com.example.util.simpletimetracker.navigation.params.action.SendEmailParams
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.DebugMenuDialogParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsRatingViewModelDelegate @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val applicationDataProvider: ApplicationDataProvider,
    private val settingsRatingViewDataInteractor: SettingsRatingViewDataInteractor,
) : ViewModelDelegate() {

    private var parent: SettingsParent? = null
    private var debugUnlocked = false
    private var debugClicksCount: Int = 0

    fun init(parent: SettingsParent) {
        this.parent = parent
    }

    fun onHidden() {
        debugClicksCount = 0
    }

    fun getViewData(): List<ViewHolderType> {
        return settingsRatingViewDataInteractor.execute(debugUnlocked)
    }

    fun onBlockClicked(block: SettingsBlock) {
        when (block) {
            SettingsBlock.RateUs -> onRateClick()
            SettingsBlock.Feedback -> onFeedbackClick()
            SettingsBlock.Version -> onVersionClick()
            SettingsBlock.DebugMenu -> onDebugMenuClick()
            else -> {
                // Do nothing
            }
        }
    }

    private fun onRateClick() {
        router.execute(
            OpenMarketParams(packageName = applicationDataProvider.getPackageName()),
        )
    }

    private fun onFeedbackClick() {
        router.execute(
            data = SendEmailParams(
                email = resourceRepo.getString(R.string.support_email),
                subject = resourceRepo.getString(R.string.support_email_subject),
                chooserTitle = resourceRepo.getString(R.string.settings_email_chooser_title),
                notHandledCallback = { R.string.message_app_not_found.let(::showMessage) },
            ),
        )
    }

    private fun onVersionClick() {
        debugClicksCount += 1
        if (debugClicksCount >= DEBUG_CLICKS_TO_UNLOCK) {
            debugUnlocked = true
            delegateScope.launch { parent?.updateContent() }
        }
    }

    private fun onDebugMenuClick() {
        router.navigate(DebugMenuDialogParams)
    }

    private fun showMessage(stringResId: Int) {
        val params = SnackBarParams(message = resourceRepo.getString(stringResId))
        router.show(params)
    }

    companion object {
        private const val DEBUG_CLICKS_TO_UNLOCK = 5
    }
}