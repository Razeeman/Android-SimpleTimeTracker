package com.example.util.simpletimetracker.feature_records.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.util.simpletimetracker.feature_records.R
import com.example.util.simpletimetracker.feature_records.adapter.RecordsContainerAdapter
import kotlinx.android.synthetic.main.records_container_fragment.*

class RecordsContainerFragment : Fragment() {

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
        setupPager()

        btnRecordsContainerPrevious.setOnClickListener {
            pagerRecordsContainer.currentItem -= 1
        }

        btnRecordsContainerToday.setOnClickListener {
            pagerRecordsContainer.currentItem = RecordsContainerAdapter.FIRST
        }

        btnRecordsContainerNext.setOnClickListener {
            pagerRecordsContainer.currentItem += 1
        }
    }

    private fun setupPager() {
        val adapter = RecordsContainerAdapter(this)
        pagerRecordsContainer.apply {
            this.adapter = adapter
            currentItem = RecordsContainerAdapter.FIRST
            offscreenPageLimit = 2
            isUserInputEnabled = false
        }
    }

    companion object {
        fun newInstance() = RecordsContainerFragment()
    }
}