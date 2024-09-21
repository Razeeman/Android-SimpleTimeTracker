package com.example.util.simpletimetracker.feature_settings.interactor

import com.example.util.simpletimetracker.core.provider.ApplicationDataProvider
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.views.SettingsBottomViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsTextViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsTextWithIconViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsTopViewData
import javax.inject.Inject

class SettingsRatingViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val applicationDataProvider: ApplicationDataProvider,
) {

    fun execute(
        debugUnlocked: Boolean,
    ): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        result += SettingsTopViewData(
            block = SettingsBlock.RatingTop,
        )

        result += SettingsTextWithIconViewData(
            data = SettingsTextViewData(
                block = SettingsBlock.RateUs,
                title = resourceRepo.getString(R.string.settings_rate),
                subtitle = resourceRepo.getString(R.string.settings_rate_description),
            ),
            iconResId = R.drawable.star_border,
            iconColor = resourceRepo.getColor(R.color.amber_400),
        )

        result += SettingsTextWithIconViewData(
            data = SettingsTextViewData(
                block = SettingsBlock.SupportDevelopment,
                title = resourceRepo.getString(R.string.settings_support_development),
                subtitle = resourceRepo.getString(R.string.settings_support_development_hint),
            ),
            iconResId = R.drawable.favorite_border,
            iconColor = resourceRepo.getColor(R.color.orange_400),
        )

        result += SettingsTextViewData(
            block = SettingsBlock.Feedback,
            title = resourceRepo.getString(R.string.settings_feedback),
            subtitle = resourceRepo.getString(R.string.settings_feedback_description),
        )

        result += SettingsTextViewData(
            block = SettingsBlock.Version,
            title = resourceRepo.getString(R.string.settings_version),
            subtitle = loadVersionName(),
            dividerIsVisible = debugUnlocked,
        )

        if (debugUnlocked) {
            result += SettingsTextViewData(
                block = SettingsBlock.DebugMenu,
                title = resourceRepo.getString(R.string.debug_menu),
                subtitle = "",
                dividerIsVisible = false,
            )
        }

        result += SettingsBottomViewData(
            block = SettingsBlock.RatingBottom,
        )

        return result
    }

    private fun loadVersionName(): String {
        return applicationDataProvider.getAppVersion()
    }
}