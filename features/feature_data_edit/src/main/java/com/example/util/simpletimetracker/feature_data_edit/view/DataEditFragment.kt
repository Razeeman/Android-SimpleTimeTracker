package com.example.util.simpletimetracker.feature_data_edit.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.feature_data_edit.viewModel.DataEditViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_data_edit.databinding.DataEditFragmentBinding as Binding

@AndroidEntryPoint
class DataEditFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: DataEditViewModel by viewModels()

    override fun initUi(): Unit = with(binding) {
        // TODO
    }

    override fun initViewModel(): Unit = with(viewModel) {
        // TODO
    }
}
