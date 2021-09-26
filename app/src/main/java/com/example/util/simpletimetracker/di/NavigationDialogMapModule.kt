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
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.NavigationData
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
    @ScreenKey(Screen.STANDARD_DIALOG)
    fun standardDialog(): NavigationData {
        return NavigationData(
            R.id.standardDialogFragment,
            StandardDialogFragment::createBundle
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.DATE_TIME_DIALOG)
    fun dateTimeDialog(): NavigationData {
        return NavigationData(
            R.id.dateTimeDialog,
            DateTimeDialogFragment::createBundle
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.DURATION_DIALOG)
    fun durationDialog(): NavigationData {
        return NavigationData(
            R.id.durationDialog,
            DurationDialogFragment::createBundle
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.CHART_FILTER_DIALOG)
    fun chartFilterDialog(): NavigationData {
        return NavigationData(
            R.id.chartFilerDialogFragment,
            null
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.TYPES_FILTER_DIALOG)
    fun typesFilterDialog(): NavigationData {
        return NavigationData(
            R.id.typesFilterDialogFragment,
            TypesFilterDialogFragment::createBundle
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.CARD_SIZE_DIALOG)
    fun cardSizeDialog(): NavigationData {
        return NavigationData(
            R.id.cardSizeDialogFragment,
            null
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.CARD_ORDER_DIALOG)
    fun cardOrderDialog(): NavigationData {
        return NavigationData(
            R.id.cardOrderDialogFragment,
            CardOrderDialogFragment::createBundle
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.EMOJI_SELECTION_DIALOG)
    fun emojiSelectionDialog(): NavigationData {
        return NavigationData(
            R.id.emojiSelectionDialogFragment,
            EmojiSelectionDialogFragment::createBundle
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.ARCHIVE_DIALOG)
    fun archiveDialog(): NavigationData {
        return NavigationData(
            R.id.archiveDialogFragment,
            ArchiveDialogFragment::createBundle
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.RECORD_TAG_SELECTION_DIALOG)
    fun recordTagSelectionDialog(): NavigationData {
        return NavigationData(
            R.id.recordTagSelectionDialogFragment,
            RecordTagSelectionDialogFragment::createBundle
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.CSV_EXPORT_SETTINGS_DIALOG)
    fun csvExportSettingsDialog(): NavigationData {
        return NavigationData(
            R.id.csvExportSettingsDialogFragment,
            null
        )
    }
}