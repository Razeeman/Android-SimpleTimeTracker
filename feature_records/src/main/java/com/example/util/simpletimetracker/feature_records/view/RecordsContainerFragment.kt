package com.example.util.simpletimetracker.feature_records.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.feature_records.R
import com.example.util.simpletimetracker.feature_records.adapter.RecordsContainerAdapter
import com.example.util.simpletimetracker.feature_records.di.RecordsComponentProvider
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsContainerViewModel
import kotlinx.android.synthetic.main.records_container_fragment.*

class RecordsContainerFragment : Fragment() {

    private val viewModel: RecordsContainerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.records_container_fragment,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity?.application as RecordsComponentProvider)
            .recordsComponent?.inject(viewModel)

        setupPager()

        viewModel.title.observe(viewLifecycleOwner) {
            updateTitle(it)
        }

        viewModel.position.observe(viewLifecycleOwner) {
            pagerRecordsContainer.currentItem = it + RecordsContainerAdapter.FIRST
        }

        btnRecordAdd.setOnClickListener {
            viewModel.onRecordAddClick()
        }

        btnRecordsContainerPrevious.setOnClickListener {
            viewModel.onPreviousClick()
        }

        btnRecordsContainerToday.setOnClickListener {
            viewModel.onTodayClick()
        }

        btnRecordsContainerNext.setOnClickListener {
            viewModel.onNextClick()
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