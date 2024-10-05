package com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.dialog.DataExportSettingsDialogListener
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.extension.findListener
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.createFilterAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.viewData.CsvExportSettingsViewData
import com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.viewModel.CsvExportSettingsViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingsResult
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_dialogs.databinding.CsvExportSettingsFragmentBinding as Binding

@AndroidEntryPoint
class CsvExportSettingsDialogFragment :
    BaseBottomSheetFragment<Binding>(),
    DateTimeDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: CsvExportSettingsViewModel by viewModels()

    private val filterSelectionAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createFilterAdapterDelegate(
                onClick = viewModel::onFilterClick,
                onRemoveClick = {},
            ),
        )
    }
    private val params: DataExportSettingDialogParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = DataExportSettingDialogParams.Empty,
    )

    private var dialogListener: DataExportSettingsDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dialogListener = context.findListener()
    }

    override fun initDialog() {
        setSkipCollapsed()
    }

    override fun initUi() {
        binding.rvCsvExportSettingsFilters.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = filterSelectionAdapter
        }
    }

    override fun initUx(): Unit = with(binding) {
        etCsvExportSettingsFileName.doAfterTextChanged { viewModel.onNameChange(it.toString()) }
        fieldCsvExportSettingsTimeStarted.setOnClick(viewModel::onRangeStartClick)
        fieldCsvExportSettingsTimeEnded.setOnClick(viewModel::onRangeEndClick)
        btnCsvExportSettingsRange.setOnClick(viewModel::onExportClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        viewData.observe(::updateViewDataState)
        dataExportSettingsResult.observeOnce(viewLifecycleOwner, ::onResult)
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    private fun updateViewDataState(
        viewData: CsvExportSettingsViewData,
    ) = with(binding) {
        if (etCsvExportSettingsFileName.text.toString() != viewData.fileName) {
            etCsvExportSettingsFileName.setText(viewData.fileName)
        }
        inputCsvExportSettingsFileName.hint = viewData.fileNameHint
        etCsvExportSettingsFileName.setTextColor(viewData.fileNameTextColor)
        filterSelectionAdapter.replace(viewData.filters)
        tvCsvExportSettingsTimeStarted.text = viewData.rangeStartString
        tvCsvExportSettingsTimeStarted.setTextColor(viewData.textColor)
        tvCsvExportSettingsTimeEnded.text = viewData.rangeEndString
        tvCsvExportSettingsTimeEnded.setTextColor(viewData.textColor)
    }

    private fun onResult(
        params: DataExportSettingsResult,
    ) {
        dialogListener?.onDataExportSettingsSelected(params)
        dismiss()
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: DataExportSettingDialogParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}