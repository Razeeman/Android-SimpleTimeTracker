package com.example.util.simpletimetracker.feature_main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.util.simpletimetracker.feature_records.view.RecordsFragment
import com.example.util.simpletimetracker.feature_running_records.view.RunningRecordsFragment

class MainContentAdapter(fragment: Fragment) :
    FragmentStatePagerAdapter(
        fragment.childFragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> RunningRecordsFragment.newInstance()
            1 -> RecordsFragment.newInstance()
            else -> RunningRecordsFragment.newInstance()
        }
    }

    override fun getCount(): Int {
        return 2 // TODO that's not nice
    }
}