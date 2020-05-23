package com.example.util.simpletimetracker.feature_records.view

import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.setOnLongClick
import com.example.util.simpletimetracker.feature_records.R
import com.example.util.simpletimetracker.feature_records.adapter.RecordsContainerAdapter
import com.example.util.simpletimetracker.feature_records.di.RecordsComponentProvider
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsContainerViewModel
import kotlinx.android.synthetic.main.records_container_fragment.*

class RecordsContainerFragment : BaseFragment(R.layout.records_container_fragment) {

    private val viewModel: RecordsContainerViewModel by viewModels()

    override fun initDi() {
        val component = (activity?.application as RecordsComponentProvider)
            .recordsComponent

        component?.inject(viewModel)
    }

    override fun initUi() {
        setupPager()
    }

    override fun initUx() {
        btnRecordAdd.setOnClick(viewModel::onRecordAddClick)
        btnRecordsContainerPrevious.setOnClick(viewModel::onPreviousClick)
        btnRecordsContainerNext.setOnClick(viewModel::onNextClick)
        btnRecordsContainerToday.setOnLongClick(viewModel::onTodayClick)
    }

    override fun initViewModel() {
        viewModel.title.observe(viewLifecycleOwner, ::updateTitle)
        viewModel.position.observe(viewLifecycleOwner) {
            pagerRecordsContainer.currentItem = it + RecordsContainerAdapter.FIRST
        }
    }

    private fun setupPager() {
        val adapter = RecordsContainerAdapter(this)
        pagerRecordsContainer.apply {
            this.adapter = adapter
            offscreenPageLimit = 2
            isUserInputEnabled = false
        }
    }

    private fun updateTitle(title: String) {
        btnRecordsContainerToday.text = title
    }

    companion object {
        fun newInstance() = RecordsContainerFragment()
    }
}