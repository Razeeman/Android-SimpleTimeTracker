package com.example.util.simpletimetracker.feature_settings.interactor

import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.model.OptionsContent
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsUiDelegated
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsAdditionalViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsBackupViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsContributorsViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsDisplayViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsExportViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsMainViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsNotificationsViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsRatingViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsTranslatorsViewModelDelegate
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingsResult
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import javax.inject.Inject

class SettingsViewModelDelegatesProvider @Inject constructor(
    val mainDelegate: SettingsMainViewModelDelegate,
    val additionalDelegate: SettingsAdditionalViewModelDelegate,
    ratingDelegate: SettingsRatingViewModelDelegate,
    notificationsDelegate: SettingsNotificationsViewModelDelegate,
    displayDelegate: SettingsDisplayViewModelDelegate,
    backupDelegate: SettingsBackupViewModelDelegate,
    exportDelegate: SettingsExportViewModelDelegate,
    translatorsDelegate: SettingsTranslatorsViewModelDelegate,
    contributorsDelegate: SettingsContributorsViewModelDelegate,
) : SettingsUiDelegated {

    val delegates: List<SettingsDelegate> = listOf(
        mainDelegate,
        ratingDelegate,
        notificationsDelegate,
        displayDelegate,
        additionalDelegate,
        backupDelegate,
        exportDelegate,
        translatorsDelegate,
        contributorsDelegate,
    )

    override fun onHidden() =
        delegates.forEach { it.onHidden() }

    override fun onBlockClicked(block: SettingsBlock) =
        delegates.forEach { it.onBlockClicked(block) }

    override fun onSpinnerPositionSelected(block: SettingsBlock, position: Int) =
        delegates.forEach { it.onSpinnerPositionSelected(block, position) }

    override fun onPositiveClick(tag: String?) =
        delegates.forEach { it.onPositiveClick(tag) }

    override fun onDurationSet(tag: String?, duration: Long) =
        delegates.forEach { it.onDurationSet(tag, duration) }

    override fun onDurationDisabled(tag: String?) =
        delegates.forEach { it.onDurationDisabled(tag) }

    override fun onDateTimeSet(timestamp: Long, tag: String?) =
        delegates.forEach { it.onDateTimeSet(timestamp, tag) }

    override fun onDataExportSettingsSelected(data: DataExportSettingsResult) =
        delegates.forEach { it.onDataExportSettingsSelected(data) }

    override fun onTypesSelected(typeIds: List<Long>, tag: String) =
        delegates.forEach { it.onTypesSelected(typeIds, tag) }

    override fun onOptionsItemClick(id: OptionsListParams.Item.Id) =
        delegates.forEach { it.onOptionsItemClick(id) }

    override fun onDayOfWeekClicked(block: SettingsBlock, data: DayOfWeekViewData) =
        delegates.forEach { it.onDayOfWeekClicked(block, data) }

    fun clear() =
        delegates.forEach { (it as? ViewModelDelegate)?.clear() }

    fun collapse() =
        delegates.forEach { it.collapse() }

    suspend fun loadContent(): List<ViewHolderType> {
        val order: List<SettingsDelegate.Key> = listOf(
            SettingsMainViewModelDelegate,
            SettingsRatingViewModelDelegate,
            SettingsNotificationsViewModelDelegate,
            SettingsDisplayViewModelDelegate,
            SettingsAdditionalViewModelDelegate,
            SettingsBackupViewModelDelegate,
            SettingsExportViewModelDelegate,
            SettingsTranslatorsViewModelDelegate,
            SettingsContributorsViewModelDelegate,
        )
        val viewData = delegates.map { it.getViewData() }.associateBy { it.key }
        return order.mapNotNull { key -> viewData[key]?.data }.flatten()
    }

    suspend fun loadOptionsContent(content: OptionsContent): List<ViewHolderType> {
        return delegates.map { it.getSheetViewData(content) }.firstOrNull { !it.isNullOrEmpty() }.orEmpty()
    }
}