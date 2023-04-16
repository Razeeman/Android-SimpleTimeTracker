package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.feature_data_edit.dialog.DataEditTagSelectionDialogFragment
import com.example.util.simpletimetracker.feature_data_edit.dialog.DataEditTypeSelectionDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.archive.view.ArchiveDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.cardOrder.view.CardOrderDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.colorSelection.view.ColorSelectionDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.view.CsvExportSettingsDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.customRangeSelection.view.CustomRangeSelectionDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.dateTime.DateTimeDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.duration.view.DurationDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.emojiSelection.view.EmojiSelectionDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.helpDialog.HelpDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.recordTagSelection.RecordTagSelectionDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.standard.StandardDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.typesFilter.view.TypesFilterDialogFragment
import com.example.util.simpletimetracker.navigation.NavigationData
import com.example.util.simpletimetracker.navigation.bundleCreator.BundleCreator
import com.example.util.simpletimetracker.navigation.bundleCreator.bundleCreatorDelegate
import com.example.util.simpletimetracker.navigation.params.screen.ArchiveDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.CardOrderDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.CardSizeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.ChartFilterDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.ColorSelectionDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.CustomRangeSelectionParams
import com.example.util.simpletimetracker.navigation.params.screen.DataEditTagSelectionDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DataEditTypeSelectionDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DefaultTypesSelectionDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.EmojiSelectionDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.HelpDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import com.example.util.simpletimetracker.navigation.params.screen.StandardDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.TypesFilterDialogParams
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
class NavigationDialogMapModule {

    @IntoMap
    @Provides
    @ScreenKey(StandardDialogParams::class)
    fun standardDialog(): NavigationData {
        return NavigationData(
            R.id.standardDialogFragment,
            bundleCreatorDelegate(StandardDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(DateTimeDialogParams::class)
    fun dateTimeDialog(): NavigationData {
        return NavigationData(
            R.id.dateTimeDialog,
            bundleCreatorDelegate(DateTimeDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(DurationDialogParams::class)
    fun durationDialog(): NavigationData {
        return NavigationData(
            R.id.durationDialog,
            bundleCreatorDelegate(DurationDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(ChartFilterDialogParams::class)
    fun chartFilterDialog(): NavigationData {
        return NavigationData(
            R.id.chartFilerDialogFragment,
            BundleCreator.empty()
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(DefaultTypesSelectionDialogParams::class)
    fun defaultTypesSelectionDialog(): NavigationData {
        return NavigationData(
            R.id.defaultTypesSelectionDialogFragment,
            BundleCreator.empty()
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(TypesFilterDialogParams::class)
    fun typesFilterDialog(): NavigationData {
        return NavigationData(
            R.id.typesFilterDialogFragment,
            bundleCreatorDelegate(TypesFilterDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(CardSizeDialogParams::class)
    fun cardSizeDialog(): NavigationData {
        return NavigationData(
            R.id.cardSizeDialogFragment,
            BundleCreator.empty()
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(CardOrderDialogParams::class)
    fun cardOrderDialog(): NavigationData {
        return NavigationData(
            R.id.cardOrderDialogFragment,
            bundleCreatorDelegate(CardOrderDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(EmojiSelectionDialogParams::class)
    fun emojiSelectionDialog(): NavigationData {
        return NavigationData(
            R.id.emojiSelectionDialogFragment,
            bundleCreatorDelegate(EmojiSelectionDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(ColorSelectionDialogParams::class)
    fun colorSelectionDialog(): NavigationData {
        return NavigationData(
            R.id.colorSelectionDialogFragment,
            bundleCreatorDelegate(ColorSelectionDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(ArchiveDialogParams.Activity::class)
    fun archiveDialogActivity(): NavigationData {
        return NavigationData(
            R.id.archiveDialogFragment,
            bundleCreatorDelegate(ArchiveDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(ArchiveDialogParams.RecordTag::class)
    fun archiveDialogRecordTag(): NavigationData {
        return NavigationData(
            R.id.archiveDialogFragment,
            bundleCreatorDelegate(ArchiveDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(RecordTagSelectionParams::class)
    fun recordTagSelectionDialog(): NavigationData {
        return NavigationData(
            R.id.recordTagSelectionDialogFragment,
            bundleCreatorDelegate(RecordTagSelectionDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(DataExportSettingDialogParams::class)
    fun csvExportSettingsDialog(): NavigationData {
        return NavigationData(
            R.id.csvExportSettingsDialogFragment,
            bundleCreatorDelegate(CsvExportSettingsDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(CustomRangeSelectionParams::class)
    fun customRangeSelectionDialog(): NavigationData {
        return NavigationData(
            R.id.customRangeSelectionDialogFragment,
            bundleCreatorDelegate(CustomRangeSelectionDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(HelpDialogParams::class)
    fun helpDialog(): NavigationData {
        return NavigationData(
            R.id.helpDialogFragment,
            bundleCreatorDelegate(HelpDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(DataEditTypeSelectionDialogParams::class)
    fun dataEditTypeSelectionDialog(): NavigationData {
        return NavigationData(
            R.id.dataEditTypeSelectionDialogFragment,
            BundleCreator.empty()
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(DataEditTagSelectionDialogParams::class)
    fun dataEditTagSelectionDialog(): NavigationData {
        return NavigationData(
            R.id.dataEditTagSelectionDialogFragment,
            bundleCreatorDelegate(DataEditTagSelectionDialogFragment::createBundle)
        )
    }
}