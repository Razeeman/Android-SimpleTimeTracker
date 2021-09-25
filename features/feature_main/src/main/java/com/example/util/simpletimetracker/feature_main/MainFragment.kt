package com.example.util.simpletimetracker.feature_main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.core.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.core.interactor.WidgetInteractor
import com.example.util.simpletimetracker.core.sharedViewModel.BackupViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
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

    private val selectedColorFilter by lazy {
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.colorTabSelected)
        var colorSelected = defaultColor

        runCatching {
            context?.theme?.obtainStyledAttributes(intArrayOf(R.attr.appTabSelectedColor))?.run {
                colorSelected = getColor(0, defaultColor)
                recycle()
            }
        }

        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            colorSelected,
            BlendModeCompat.SRC_IN
        )
    }

    private val unselectedColorFilter by lazy {
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.colorTabUnselected)
        var colorUnselected = defaultColor

        runCatching {
            context?.theme?.obtainStyledAttributes(intArrayOf(R.attr.appTabUnselectedColor))?.run {
                colorUnselected = getColor(0, defaultColor)
                recycle()
            }
        }

        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            colorUnselected,
            BlendModeCompat.SRC_IN
        )
    }

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
        GlobalScope.launch {
            notificationTypeInteractor.checkAndShowAll()
            widgetInteractor.updateWidgets()
        }
    }
}
