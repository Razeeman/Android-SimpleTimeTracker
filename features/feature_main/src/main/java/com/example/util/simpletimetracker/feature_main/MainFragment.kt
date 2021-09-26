package com.example.util.simpletimetracker.feature_main

import android.graphics.ColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.extension.getThemedAttr
import com.example.util.simpletimetracker.core.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.core.interactor.WidgetInteractor
import com.example.util.simpletimetracker.core.sharedViewModel.BackupViewModel
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_main.databinding.MainFragmentBinding as Binding

@AndroidEntryPoint
class MainFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var backupViewModelFactory: BaseViewModelFactory<BackupViewModel>

    @Inject
    lateinit var notificationTypeInteractor: NotificationTypeInteractor

    @Inject
    lateinit var widgetInteractor: WidgetInteractor

    private val backupViewModel: BackupViewModel by viewModels(
        ownerProducer = { activity as AppCompatActivity },
        factoryProducer = { backupViewModelFactory }
    )

    private val selectedColorFilter by lazy { getColorFilter(R.attr.appTabSelectedColor) }
    private val unselectedColorFilter by lazy { getColorFilter(R.attr.appTabUnselectedColor) }

    override fun initUi() {
        syncState()
        setupPager()
    }

    override fun initViewModel() = with(binding) {
        backupViewModel.progressVisibility.observe(mainProgress::visible::set)
    }

    private fun setupPager() = with(binding) {
        mainPager.adapter = MainContentAdapter(this@MainFragment)
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

    private fun syncState() {
        viewLifecycleOwner.lifecycleScope.launch {
            notificationTypeInteractor.checkAndShowAll()
            widgetInteractor.updateWidgets()
        }
    }

    private fun getColorFilter(@AttrRes attrRes: Int): ColorFilter? {
        return BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            requireContext().getThemedAttr(attrRes),
            BlendModeCompat.SRC_IN
        )
    }
}
