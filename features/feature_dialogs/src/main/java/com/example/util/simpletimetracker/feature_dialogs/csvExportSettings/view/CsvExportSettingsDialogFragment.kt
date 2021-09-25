package com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.CsvExportSettingsDialogListener
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.viewModel.CsvExportSettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_dialogs.databinding.CsvExportSettingsFragmentBinding as Binding

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

    private var dialogListener: CsvExportSettingsDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is CsvExportSettingsDialogListener -> {
                dialogListener = context
                return
            }
            is AppCompatActivity -> {
                context.getAllFragments()
                    .firstOrNull { it is CsvExportSettingsDialogListener && it.isResumed }
                    ?.let { dialogListener = it as? CsvExportSettingsDialogListener }
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
        viewData.observe(viewLifecycleOwner) { viewData ->
            binding.tvCsvExportSettingsTimeStarted.text = viewData.rangeStartString
            binding.tvCsvExportSettingsTimeEnded.text = viewData.rangeEndString
        }
        csvExportSettingsParams.observeOnce(viewLifecycleOwner) { params ->
            dialogListener?.onCsvExportSettingsSelected(params)
            dismiss()
        }
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }
}