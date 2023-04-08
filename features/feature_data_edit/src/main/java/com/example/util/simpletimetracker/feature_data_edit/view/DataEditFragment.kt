package com.example.util.simpletimetracker.feature_data_edit.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.feature_data_edit.viewModel.DataEditViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_data_edit.databinding.DataEditFragmentBinding as Binding

@AndroidEntryPoint
class DataEditFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: DataEditViewModel by viewModels()

    override fun initUi(): Unit = with(binding) {
        btnDataEditSelectRecords.setOnClick(throttle(viewModel::onSelectRecordsClick))
    }

    override fun initViewModel(): Unit = with(viewModel) {
        with(binding) {
            selectedRecordsCountViewData.observe(tvDataEditSelectedRecords::setText)
        }
    }
}
