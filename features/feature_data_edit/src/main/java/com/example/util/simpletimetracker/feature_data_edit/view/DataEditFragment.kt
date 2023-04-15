package com.example.util.simpletimetracker.feature_data_edit.view

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.feature_data_edit.dialog.DataEditTypeSelectionDialogListener
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeActivityState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeButtonState
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
        btnDataEditChange.setOnClick(throttle(viewModel::onChangeClick))
    }

    override fun initViewModel(): Unit = with(viewModel) {
        with(binding) {
            selectedRecordsCountViewData.observe(tvDataEditSelectedRecords::setText)
            changeActivityState.observe(::setChangeActivityState)
            changeButtonState.observe(::setChangeButtonState)
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

    private fun setChangeActivityState(
        state: DataEditChangeActivityState,
    ) = with(binding) {
        when (state) {
            is DataEditChangeActivityState.Disabled -> {
                checkboxDataEditChangeActivity.isChecked = false
                viewDataEditChangeActivityPreview.isVisible = false
            }
            is DataEditChangeActivityState.Enabled -> {
                checkboxDataEditChangeActivity.isChecked = true
                viewDataEditChangeActivityPreview.isVisible = true
                viewDataEditChangeActivityPreview.apply {
                    itemColor = state.viewData.color
                    itemIcon = state.viewData.iconId
                    itemIconColor = state.viewData.iconColor
                    itemIconAlpha = state.viewData.iconAlpha
                    itemName = state.viewData.name
                }
            }
        }
    }

    private fun setChangeButtonState(
        state: DataEditChangeButtonState
    ) = with(binding) {
        btnDataEditChange.isEnabled = state.enabled
        btnDataEditChange.backgroundTintList = ColorStateList.valueOf(state.backgroundTint)
    }
}
