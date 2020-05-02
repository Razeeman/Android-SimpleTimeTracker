package com.example.util.simpletimetracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.TimeTrackerApp
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    private val adapter: MainAdapter = MainAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        rvMainContent.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@MainFragment.adapter
        }

        TimeTrackerApp.appComponent?.inject(viewModel)

        viewModel.getPeriods().observe(viewLifecycleOwner) {
            adapter.replace(it)
        }

        btnAdd.setOnClickListener {
            viewModel.add()
        }
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}
