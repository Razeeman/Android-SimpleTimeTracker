package com.example.util.simpletimetracker.feature_main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.util.simpletimetracker.feature_records.view.RecordsContainerFragment
import com.example.util.simpletimetracker.feature_running_records.view.RunningRecordsFragment
import com.example.util.simpletimetracker.feature_statistics.view.StatisticsContainerFragment

class MainContentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val fragments = listOf(
        lazy { RunningRecordsFragment.newInstance() },
        lazy { RecordsContainerFragment.newInstance() },
        lazy { StatisticsContainerFragment.newInstance() }
    )

    override fun getItemCount(): Int =
        fragments.size

    override fun createFragment(position: Int): Fragment =
        fragments.getOrNull(position)?.value ?: fragments.first().value
}