package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.feature_dialogs.archive.view.ArchiveDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.cardOrder.view.CardOrderDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.dateTime.DateTimeDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.duration.view.DurationDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.emojiSelection.view.EmojiSelectionDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.recordTagSelection.RecordTagSelectionDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.standard.StandardDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.typesFilter.view.TypesFilterDialogFragment
import com.example.util.simpletimetracker.navigation.bundleCreator.BundleCreator
import com.example.util.simpletimetracker.navigation.bundleCreator.bundleCreatorDelegate
import com.example.util.simpletimetracker.navigation.params.screen.ArchiveDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.CardOrderDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.CardSizeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.ChartFilterDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.CsvExportSettingDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.EmojiSelectionDialogParams
import com.example.util.simpletimetracker.navigation.NavigationData
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import com.example.util.simpletimetracker.navigation.params.screen.StandardDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.TypesFilterDialogParams
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
class NavigationDialogMapModule {

    @IntoMap
    @Provides
    @ClassKey(StandardDialogParams::class)
    fun standardDialog(): NavigationData {
        return NavigationData(
            R.id.standardDialogFragment,
            bundleCreatorDelegate(StandardDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ClassKey(DateTimeDialogParams::class)
    fun dateTimeDialog(): NavigationData {
        return NavigationData(
            R.id.dateTimeDialog,
            bundleCreatorDelegate(DateTimeDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ClassKey(DurationDialogParams::class)
    fun durationDialog(): NavigationData {
        return NavigationData(
            R.id.durationDialog,
            bundleCreatorDelegate(DurationDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ClassKey(ChartFilterDialogParams::class)
    fun chartFilterDialog(): NavigationData {
        return NavigationData(
            R.id.chartFilerDialogFragment,
            BundleCreator.empty()
        )
    }

    @IntoMap
    @Provides
    @ClassKey(TypesFilterDialogParams::class)
    fun typesFilterDialog(): NavigationData {
        return NavigationData(
            R.id.typesFilterDialogFragment,
            bundleCreatorDelegate(TypesFilterDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ClassKey(CardSizeDialogParams::class)
    fun cardSizeDialog(): NavigationData {
        return NavigationData(
            R.id.cardSizeDialogFragment,
            BundleCreator.empty()
        )
    }

    @IntoMap
    @Provides
    @ClassKey(CardOrderDialogParams::class)
    fun cardOrderDialog(): NavigationData {
        return NavigationData(
            R.id.cardOrderDialogFragment,
            bundleCreatorDelegate(CardOrderDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ClassKey(EmojiSelectionDialogParams::class)
    fun emojiSelectionDialog(): NavigationData {
        return NavigationData(
            R.id.emojiSelectionDialogFragment,
            bundleCreatorDelegate(EmojiSelectionDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ClassKey(ArchiveDialogParams.Activity::class)
    fun archiveDialogActivity(): NavigationData {
        return NavigationData(
            R.id.archiveDialogFragment,
            bundleCreatorDelegate(ArchiveDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ClassKey(ArchiveDialogParams.RecordTag::class)
    fun archiveDialogRecordTag(): NavigationData {
        return NavigationData(
            R.id.archiveDialogFragment,
            bundleCreatorDelegate(ArchiveDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ClassKey(RecordTagSelectionParams::class)
    fun recordTagSelectionDialog(): NavigationData {
        return NavigationData(
            R.id.recordTagSelectionDialogFragment,
            bundleCreatorDelegate(RecordTagSelectionDialogFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ClassKey(CsvExportSettingDialogParams::class)
    fun csvExportSettingsDialog(): NavigationData {
        return NavigationData(
            R.id.csvExportSettingsDialogFragment,
            BundleCreator.empty()
        )
    }
}