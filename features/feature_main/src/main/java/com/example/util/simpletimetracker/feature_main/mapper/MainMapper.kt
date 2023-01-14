package com.example.util.simpletimetracker.feature_main.mapper

import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.core.utils.SHORTCUT_NAVIGATION_RECORDS
import com.example.util.simpletimetracker.core.utils.SHORTCUT_NAVIGATION_SETTINGS
import com.example.util.simpletimetracker.core.utils.SHORTCUT_NAVIGATION_STATISTICS
import com.example.util.simpletimetracker.feature_main.R
import javax.inject.Inject

class MainMapper @Inject constructor() {

    fun mapPositionToTab(position: Int): NavigationTab? {
        return when (position) {
            0 -> NavigationTab.RunningRecords
            1 -> NavigationTab.Records
            2 -> NavigationTab.Statistics
            3 -> NavigationTab.Settings
            else -> null
        }
    }

    fun mapTabToPosition(tab: NavigationTab): Int {
        return when (tab) {
            NavigationTab.RunningRecords -> 0
            NavigationTab.Records -> 1
            NavigationTab.Statistics -> 2
            NavigationTab.Settings -> 3
        }
    }

    fun mapNavigationToTab(value: String): NavigationTab? {
        return when (value) {
            SHORTCUT_NAVIGATION_RECORDS -> NavigationTab.Records
            SHORTCUT_NAVIGATION_STATISTICS -> NavigationTab.Statistics
            SHORTCUT_NAVIGATION_SETTINGS -> NavigationTab.Settings
            else -> null
        }
    }

    @DrawableRes fun mapToIcon(tab: NavigationTab?): Int {
        return when (tab) {
            NavigationTab.RunningRecords -> R.drawable.ic_tab_running_records
            NavigationTab.Records -> R.drawable.ic_tab_records
            NavigationTab.Statistics -> R.drawable.ic_tab_statistics
            NavigationTab.Settings -> R.drawable.ic_tab_settings
            else -> R.drawable.unknown
        }
    }
}