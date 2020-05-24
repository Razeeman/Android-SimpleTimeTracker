package com.example.util.simpletimetracker.feature_main

import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : BaseFragment(R.layout.main_fragment) {

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

    override fun initUi() {
        setupPager()
    }

    private fun setupPager() {
        mainPager.adapter = MainContentAdapter(this)
        mainPager.offscreenPageLimit = 3

        TabLayoutMediator(mainTabs, mainPager) { tab, position ->
            when (position) {
                0 -> R.drawable.ic_tab_running_records
                1 -> R.drawable.ic_tab_records
                2 -> R.drawable.ic_tab_statistics
                3 -> R.drawable.ic_tab_settings
                else -> R.drawable.unknown
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
