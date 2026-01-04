package com.example.util.simpletimetracker.feature_settings.customizeOptionsMenu

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.R
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_records.api.RecordsContainerOptionsListMapper
import com.example.util.simpletimetracker.feature_settings.views.SettingsCheckboxViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsCheckboxWithIconViewData
import com.example.util.simpletimetracker.feature_statistics.api.StatisticsContainerOptionsListMapper
import com.example.util.simpletimetracker.feature_statistics_detail.api.StatisticsDetailOptionsListMapper
import com.example.util.simpletimetracker.navigation.params.screen.CustomizeOptionsMenuDialogParams
import javax.inject.Inject

class CustomizeOptionsMenuViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val customizeOptionsMenuMapper: CustomizeOptionsMenuMapper,
    private val recordsContainerOptionsListMapper: RecordsContainerOptionsListMapper,
    private val statisticsContainerOptionsListMapper: StatisticsContainerOptionsListMapper,
    private val statisticsDetailOptionsListMapper: StatisticsDetailOptionsListMapper,
) {

    suspend fun execute(
        extra: CustomizeOptionsMenuDialogParams,
    ): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        val isDarkTheme = prefsInteractor.getDarkMode()
        val hiddenItems = prefsInteractor.getHiddenContainerOptions()
            .map(customizeOptionsMenuMapper::mapItemFromModel)

        val items = when (extra.from) {
            is CustomizeOptionsMenuDialogParams.From.Records ->
                recordsContainerOptionsListMapper.map(filterHidden = false)
            is CustomizeOptionsMenuDialogParams.From.Statistics ->
                statisticsContainerOptionsListMapper.map(filterHidden = false, RangeLength.Day)
            is CustomizeOptionsMenuDialogParams.From.DetailedStatistics ->
                statisticsDetailOptionsListMapper.map(filterHidden = false, RangeLength.Day)
        }
        result += items.mapIndexedNotNull { index, item ->
            val data = SettingsCheckboxViewData(
                block = customizeOptionsMenuMapper.mapItemToModel(item.id)
                    ?.let(customizeOptionsMenuMapper::mapBlockFromModel)
                    ?: return@mapIndexedNotNull null,
                title = item.text,
                subtitle = "",
                isChecked = item.id !in hiddenItems,
                dividerIsVisible = index.inc() < items.size,
                backgroundIsVisible = false,
            )
            SettingsCheckboxWithIconViewData(
                data = data,
                iconResId = item.icon,
                iconColor = resourceRepo.getThemedAttr(R.attr.appInactiveColor, isDarkTheme),
            )
        }

        return result
    }
}