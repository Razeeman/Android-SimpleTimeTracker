package com.example.util.simpletimetracker.feature_data_edit.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.feature_data_edit.dialog.DataEditTypeSelectionDialogListener
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeActivityState
import com.example.util.simpletimetracker.feature_data_edit.viewModel.DataEditViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_data_edit.databinding.DataEditFragmentBinding as Binding

@AndroidEntryPoint
class DataEditFragment :
    BaseFragment<Binding>(),
    DataEditTypeSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: DataEditViewModel by viewModels()

    override fun initUi() = with(binding) {
        btnDataEditSelectRecords.setOnClick(throttle(viewModel::onSelectRecordsClick))
    }

    override fun initUx() = with(binding) {
        checkboxDataEditChangeActivity.setOnClick(viewModel::onChangeActivityClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        with(binding) {
            selectedRecordsCountViewData.observe(tvDataEditSelectedRecords::setText)
            changeActivityCheckbox.observe {
                when (it) {
                    is DataEditChangeActivityState.Disabled -> {
                        checkboxDataEditChangeActivity.isChecked = false
                        viewDataEditChangeActivityPreview.isVisible = false
                    }
                    is DataEditChangeActivityState.Enabled -> {
                        checkboxDataEditChangeActivity.isChecked = true
                        viewDataEditChangeActivityPreview.isVisible = true
                        viewDataEditChangeActivityPreview.apply {
                            itemColor = it.viewData.color
                            itemIcon = it.viewData.iconId
                            itemIconColor = it.viewData.iconColor
                            itemIconAlpha = it.viewData.iconAlpha
                            itemName = it.viewData.name
                        }
                    }
                }
            }
        }
    }

    override fun onResume() = with(binding) {
        super.onResume()
        checkboxDataEditChangeActivity.jumpDrawablesToCurrentState()
        checkboxDataEditChangeComment.jumpDrawablesToCurrentState()
        checkboxDataEditAddTag.jumpDrawablesToCurrentState()
        checkboxDataEditRemoveTag.jumpDrawablesToCurrentState()
    }

    override fun onTypeSelected(typeId: Long) {
        viewModel.onTypeSelected(typeId)
    }
}
