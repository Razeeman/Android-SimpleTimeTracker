package com.example.util.simpletimetracker.feature_statistics.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.util.simpletimetracker.feature_statistics.view.StatisticsFragment
import com.example.util.simpletimetracker.navigation.params.StatisticsParams
import java.util.*

class StatisticsContainerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int =
        Int.MAX_VALUE

    override fun createFragment(position: Int): Fragment {
        val range = getRange(position)
        return StatisticsFragment.newInstance(
            StatisticsParams(
                rangeStart = range.first,
                rangeEnd = range.second
            )
        )
    }

    private fun getRange(position: Int): Pair<Long, Long> {
        val shift = position - FIRST
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DATE, shift)
        }
        val rangeStart = calendar.timeInMillis
        val rangeEnd = calendar.apply { add(Calendar.DATE, 1) }.timeInMillis

        return rangeStart to rangeEnd
    }

    companion object {
        const val FIRST = Int.MAX_VALUE / 2
    }
}