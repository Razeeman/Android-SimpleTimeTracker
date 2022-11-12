package com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.view

import com.example.util.simpletimetracker.feature_dialogs.databinding.CsvExportSettingsFragmentBinding as Binding
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DataExportSettingsDialogListener
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.viewModel.CsvExportSettingsViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingDialogParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CsvExportSettingsDialogFragment :
    BaseBottomSheetFragment<Binding>(),
    DateTimeDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<CsvExportSettingsViewModel>

    private val viewModel: CsvExportSettingsViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val params: DataExportSettingDialogParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = DataExportSettingDialogParams()
    )

    private var dialogListener: DataExportSettingsDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is DataExportSettingsDialogListener -> {
                dialogListener = context
                return
            }
            is AppCompatActivity -> {
                context.getAllFragments()
                    .firstOrNull { it is DataExportSettingsDialogListener && it.isResumed }
                    ?.let { dialogListener = it as? DataExportSettingsDialogListener }
            }
        }
    }

    override fun initDialog() {
        setSkipCollapsed()
    }

    override fun initUx(): Unit = with(binding) {
        fieldCsvExportSettingsTimeStarted.setOnClick(viewModel::onRangeStartClick)
        fieldCsvExportSettingsTimeEnded.setOnClick(viewModel::onRangeEndClick)
        btnCsvExportSettingsRange.setOnClick(viewModel::onExportRangeClick)
        btnCsvExportSettingsAll.setOnClick(viewModel::onExportAllClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        viewData.observe(viewLifecycleOwner) { viewData ->
            binding.tvCsvExportSettingsTimeStarted.text = viewData.rangeStartString
            binding.tvCsvExportSettingsTimeEnded.text = viewData.rangeEndString
        }
        dataExportSettingsResult.observeOnce(viewLifecycleOwner) { params ->
            dialogListener?.onDataExportSettingsSelected(params)
            dismiss()
        }
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: DataExportSettingDialogParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}