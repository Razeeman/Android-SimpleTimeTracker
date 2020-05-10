package com.example.util.simpletimetracker.feature_main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {

    private val selectedColorFilter by lazy {
        BlendModeColorFilterCompat
            .createBlendModeColorFilterCompat(
                ContextCompat.getColor(requireContext(), R.color.black),
                BlendModeCompat.SRC_IN
            )
    }

    private val unselectedColorFilter by lazy {
        BlendModeColorFilterCompat
            .createBlendModeColorFilterCompat(
                ContextCompat.getColor(requireContext(), R.color.grey_400),
                BlendModeCompat.SRC_IN
            )
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
        setupPager()
    }

    private fun setupPager() {
        mainPager.adapter = MainContentAdapter(this)
        mainPager.offscreenPageLimit = 2

        TabLayoutMediator(mainTabs, mainPager) { tab, position ->
            when (position) {
                0 -> R.drawable.ic_tab_running_records
                1 -> R.drawable.ic_tab_records
                2 -> R.drawable.ic_tab_statistics
                else -> R.drawable.ic_unknown
            }.let(tab::setIcon)

            tab.icon?.colorFilter = if (position == 0) {
                selectedColorFilter
            } else {
                unselectedColorFilter
            }
        }.attach()

        mainTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Do nothing
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.icon?.colorFilter = unselectedColorFilter
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.icon?.colorFilter = selectedColorFilter
            }
        })
    }
}
