package com.example.util.simpletimetracker.feature_main.view

import com.example.util.simpletimetracker.feature_main.databinding.MainFragmentBinding as Binding
import android.graphics.ColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.extension.getThemedAttr
import com.example.util.simpletimetracker.core.sharedViewModel.MainTabsViewModel
import com.example.util.simpletimetracker.core.utils.SHORTCUT_NAVIGATION_KEY
import com.example.util.simpletimetracker.feature_main.R
import com.example.util.simpletimetracker.feature_main.adapter.MainContentAdapter
import com.example.util.simpletimetracker.feature_main.mapper.MainMapper
import com.example.util.simpletimetracker.feature_main.viewModel.MainViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<MainViewModel>

    @Inject
    lateinit var mainTabsViewModelFactory: BaseViewModelFactory<MainTabsViewModel>

    @Inject
    lateinit var mainMapper: MainMapper

    private val viewModel: MainViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val mainTabsViewModel: MainTabsViewModel by viewModels(
        ownerProducer = { activity as AppCompatActivity },
        factoryProducer = { mainTabsViewModelFactory }
    )

    private val selectedColorFilter by lazy { getColorFilter(R.attr.appTabSelectedColor) }
    private val unselectedColorFilter by lazy { getColorFilter(R.attr.appTabUnselectedColor) }

    override fun initUi() {
        setupPager()
        checkForShortcutNavigation()
    }

    override fun initViewModel() {
        viewModel.initialize
    }

    private fun setupPager() = with(binding) {
        mainPager.adapter = MainContentAdapter(this@MainFragment)
        mainPager.offscreenPageLimit = 3 // Same as number of pages to avoid recreating.

        TabLayoutMediator(mainTabs, mainPager) { tab, position ->
            position
                .let(mainMapper::mapPositionToTab)
                .let(mainMapper::mapToIcon)
                .let(tab::setIcon)

            tab.icon?.colorFilter = if (position == 0) {
                selectedColorFilter
            } else {
                unselectedColorFilter
            }
        }.attach()

        mainTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab?.position
                    ?.let(mainMapper::mapPositionToTab)
                    ?.let(mainTabsViewModel::onTabReselected)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.icon?.colorFilter = unselectedColorFilter
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.icon?.colorFilter = selectedColorFilter
            }
        })
    }

    private fun checkForShortcutNavigation() = with(binding) {
        activity?.intent?.extras
            ?.getString(SHORTCUT_NAVIGATION_KEY)
            ?.let(mainMapper::mapNavigationToTab)
            ?.let(mainMapper::mapTabToPosition)
            ?.let { mainPager.setCurrentItem(it, true) }
    }

    private fun getColorFilter(@AttrRes attrRes: Int): ColorFilter? {
        return BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            requireContext().getThemedAttr(attrRes),
            BlendModeCompat.SRC_IN
        )
    }
}
