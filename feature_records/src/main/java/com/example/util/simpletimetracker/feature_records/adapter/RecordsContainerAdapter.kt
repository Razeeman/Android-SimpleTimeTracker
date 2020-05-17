package com.example.util.simpletimetracker.feature_records.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.util.simpletimetracker.feature_records.view.RecordsFragment
import com.example.util.simpletimetracker.navigation.params.RecordsParams
import java.util.*

class RecordsContainerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int =
        Int.MAX_VALUE

    override fun createFragment(position: Int): Fragment {
        val shift = position - FIRST

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DATE, shift)
        }

        return RecordsFragment.newInstance(
            RecordsParams(
                rangeStart = calendar.timeInMillis,
                rangeEnd = calendar.apply { add(Calendar.DATE, 1) }.timeInMillis
            )
        )
    }

    companion object {
        const val FIRST = Int.MAX_VALUE / 2
    }
}