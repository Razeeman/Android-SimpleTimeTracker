package com.example.util.simpletimetracker.feature_main.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.feature_goals.view.GoalsFragment
import com.example.util.simpletimetracker.feature_records.view.RecordsContainerFragment
import com.example.util.simpletimetracker.feature_running_records.view.RunningRecordsFragment
import com.example.util.simpletimetracker.feature_settings.view.SettingsFragment
import com.example.util.simpletimetracker.feature_statistics.view.StatisticsContainerFragment

class MainContentAdapter(
    fragment: Fragment,
    tabs: List<NavigationTab>,
) : FragmentStateAdapter(fragment) {

    private val fragments = tabs.map {
        when (it) {
            is NavigationTab.RunningRecords -> lazy { RunningRecordsFragment.newInstance() }
            is NavigationTab.Records -> lazy { RecordsContainerFragment.newInstance() }
            is NavigationTab.Statistics -> lazy { StatisticsContainerFragment.newInstance() }
            is NavigationTab.Settings -> lazy { SettingsFragment.newInstance() }
            is NavigationTab.Goals -> lazy { GoalsFragment.newInstance() }
        }
    }

    override fun getItemCount(): Int =
        fragments.size

    override fun createFragment(position: Int): Fragment =
        fragments[position].value
}