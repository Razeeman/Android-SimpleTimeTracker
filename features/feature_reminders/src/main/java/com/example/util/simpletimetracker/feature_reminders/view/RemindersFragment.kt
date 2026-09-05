package com.example.util.simpletimetracker.feature_reminders.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.button.createButtonAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.header.createHeaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_reminders.adapter.createReminderAdapterDelegate
import com.example.util.simpletimetracker.feature_reminders.adapter.createActivityReminderAdapterDelegate
import com.example.util.simpletimetracker.feature_reminders.viewModel.RemindersViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_reminders.databinding.RemindersFragmentBinding as Binding

@AndroidEntryPoint
class RemindersFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.rvRemindersList }

    private val viewModel: RemindersViewModel by viewModels()

    private val remindersAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createHeaderAdapterDelegate(),
            createActivityReminderAdapterDelegate(
                onItemClick = throttle(viewModel::onActivityReminderClick),
            ),
            createButtonAdapterDelegate(
                onClick = throttle(viewModel::onAddClick),
            ),
            createReminderAdapterDelegate(
                onItemClick = throttle(viewModel::onReminderClick),
                onEnabledClick = viewModel::onEnabledClick,
            ),
        )
    }

    override fun initUi(): Unit = with(binding) {
        rvRemindersList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = remindersAdapter
        }
    }

    override fun initViewModel(): Unit = with(viewModel) {
        viewData.observe(remindersAdapter::replace)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }
}
