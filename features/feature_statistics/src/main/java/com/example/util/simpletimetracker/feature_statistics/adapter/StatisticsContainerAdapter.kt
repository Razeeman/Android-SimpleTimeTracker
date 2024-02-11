package com.example.util.simpletimetracker.feature_statistics.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.util.simpletimetracker.feature_statistics.view.StatisticsFragment
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsParams

class StatisticsContainerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    // "Infinite" pager.
    override fun getItemCount(): Int =
        Int.MAX_VALUE

    override fun createFragment(position: Int): Fragment {
        val shift = position - FIRST
        return StatisticsFragment.newInstance(
            StatisticsParams(shift = shift),
        )
    }

    companion object {
        // First page is at the center of range.
        const val FIRST = Int.MAX_VALUE / 2
    }
}