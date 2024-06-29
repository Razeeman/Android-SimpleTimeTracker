package com.example.util.simpletimetracker.feature_main.provider

import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.core.utils.SHORTCUT_NAVIGATION_RECORDS
import com.example.util.simpletimetracker.core.utils.SHORTCUT_NAVIGATION_SETTINGS
import com.example.util.simpletimetracker.core.utils.SHORTCUT_NAVIGATION_STATISTICS
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_main.R
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class MainTabsProvider @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
) {

    val tabsList: List<NavigationTab> by lazy { loadTabsList() }
    val mainTab: NavigationTab by lazy { loadMainTab() }

    fun mapPositionToTab(position: Int): NavigationTab? {
        return tabsList.getOrNull(position)
    }

    fun mapTabToPosition(tab: NavigationTab): Int {
        return tabsList.indexOf(tab).takeUnless { it == -1 }.orZero()
    }

    @DrawableRes
    fun mapPositionToIcon(position: Int): Int {
        return position
            .let(::mapPositionToTab)
            .let(::mapToIcon)
    }

    fun mapNavigationToPosition(value: String): Int? {
        return mapNavigationToTab(value)?.let(::mapTabToPosition)
    }

    @DrawableRes
    fun mapToIcon(tab: NavigationTab?): Int {
        if (tab == null) return R.drawable.unknown

        return when (tab) {
            NavigationTab.RunningRecords -> R.drawable.tab_running_records
            NavigationTab.Records -> R.drawable.tab_records
            NavigationTab.Statistics -> R.drawable.tab_statistics
            NavigationTab.Settings -> R.drawable.tab_settings
            NavigationTab.Goals -> R.drawable.tab_goals
        }
    }

    private fun mapNavigationToTab(value: String): NavigationTab? {
        return when (value) {
            SHORTCUT_NAVIGATION_RECORDS -> NavigationTab.Records
            SHORTCUT_NAVIGATION_STATISTICS -> NavigationTab.Statistics
            SHORTCUT_NAVIGATION_SETTINGS -> NavigationTab.Settings
            else -> null
        }
    }

    private fun loadTabsList(): List<NavigationTab> {
        val showGoals = runBlocking { prefsInteractor.getShowGoalsSeparately() }

        return listOfNotNull(
            NavigationTab.RunningRecords,
            NavigationTab.Records,
            NavigationTab.Goals.takeIf { showGoals },
            NavigationTab.Statistics,
            NavigationTab.Settings,
        )
    }

    private fun loadMainTab(): NavigationTab {
        return runBlocking {
            NavigationTab.RunningRecords
        }
    }
}