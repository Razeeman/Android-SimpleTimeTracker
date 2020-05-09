package com.example.util.simpletimetracker.feature_main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {

    private val adapter: MainContentAdapter by lazy {
        MainContentAdapter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // TODO replace with ViewPager2
        mainPager.adapter = adapter
        mainTabs.setupWithViewPager(mainPager)
        (0..mainTabs.tabCount).forEach { index ->
            when (index) {
                0 -> R.drawable.ic_tab_running_records
                1 -> R.drawable.ic_tab_records
                2 -> R.drawable.ic_tab_statistics
                else -> R.drawable.ic_unknown
            }.let { iconId ->
                mainTabs.getTabAt(index)?.setIcon(iconId)
            }
        }
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}
