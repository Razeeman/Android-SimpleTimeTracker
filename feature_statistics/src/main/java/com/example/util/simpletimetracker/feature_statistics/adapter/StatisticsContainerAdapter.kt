package com.example.util.simpletimetracker.feature_statistics.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.util.simpletimetracker.feature_statistics.view.StatisticsFragment
import com.example.util.simpletimetracker.feature_statistics.viewData.RangeLength
import com.example.util.simpletimetracker.navigation.params.StatisticsParams
import java.util.Calendar

class StatisticsContainerAdapter(
    fragment: Fragment,
    private val rangeLength: RangeLength
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return if (rangeLength == RangeLength.ALL) 1 else Int.MAX_VALUE
    }

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
        val rangeStart: Long
        val rangeEnd: Long
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        when (rangeLength) {
            RangeLength.DAY -> {
                calendar.add(Calendar.DATE, shift)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.DATE, 1) }.timeInMillis
            }
            RangeLength.WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.add(Calendar.DATE, shift * 7)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.DATE, 7) }.timeInMillis
            }
            RangeLength.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.add(Calendar.MONTH, shift)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.MONTH, 1) }.timeInMillis
            }
            RangeLength.ALL -> {
                rangeStart = 0L
                rangeEnd = 0L
            }
        }

        return rangeStart to rangeEnd
    }

    companion object {
        const val FIRST = Int.MAX_VALUE / 2
    }
}