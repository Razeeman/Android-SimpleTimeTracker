package com.example.util.simpletimetracker.feature_running_records

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.feature_running_records.di.RunningRecordsComponentProvider
import kotlinx.android.synthetic.main.running_records_fragment.*

class RunningRecordsFragment : Fragment() {

    private val viewModel: RunningRecordsViewModel by viewModels()

    private val adapter: RunningRecordsAdapter = RunningRecordsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.running_records_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        rvMainContent.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@RunningRecordsFragment.adapter
        }

        (activity?.application as RunningRecordsComponentProvider)
            .provideRunningRecordsComponent()
            ?.inject(viewModel)

        viewModel.records.observe(viewLifecycleOwner) {
            adapter.replace(it)
        }

        btnAdd.setOnClickListener {
            viewModel.add()
        }
        btnClear.setOnClickListener {
            viewModel.clear()
        }
    }

    companion object {
        fun newInstance() = RunningRecordsFragment()
    }
}
