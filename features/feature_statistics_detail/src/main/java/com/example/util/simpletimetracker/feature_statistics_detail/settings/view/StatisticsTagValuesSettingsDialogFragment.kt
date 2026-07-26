package com.example.util.simpletimetracker.feature_statistics_detail.settings.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.extension.blockContentScroll
import com.example.util.simpletimetracker.core.extension.findListener
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_settings.views.getSettingsAdapterDelegates
import com.example.util.simpletimetracker.feature_statistics_detail.settings.dialog.StatisticsTagValuesSettingsDialogListener
import com.example.util.simpletimetracker.feature_statistics_detail.settings.viewModel.StatisticsTagValuesSettingsViewModel
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsTagValuesSettingsParams
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsTagValuesSettingsFragmentBinding as Binding

@AndroidEntryPoint
class StatisticsTagValuesSettingsDialogFragment :
    BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: StatisticsTagValuesSettingsViewModel by viewModels()
    private val params: StatisticsTagValuesSettingsParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS,
        default = StatisticsTagValuesSettingsParams.Empty,
    )
    private var listener: StatisticsTagValuesSettingsDialogListener? = null

    private val contentAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            *getSettingsAdapterDelegates(
                onBlockClicked = viewModel::onBlockClicked,
                onSpinnerPositionSelected = { _, _ -> },
            ).toTypedArray(),
        )
    }

    override fun initDialog() {
        setSkipCollapsed()
        blockContentScroll(binding.rvStatisticsTagValuesSettingsContent)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context.findListener<StatisticsTagValuesSettingsDialogListener>()
    }

    override fun initUi() = with(binding) {
        rvStatisticsTagValuesSettingsContent.adapter = contentAdapter
        rvStatisticsTagValuesSettingsContent.itemAnimator = null
    }

    override fun initViewModel() {
        viewModel.initialize(params)
        viewModel.content.observe(contentAdapter::replaceAsNew)
        viewModel.settingsChanged.observe {
            listener?.onStatisticsTagValuesSettingsChanged(it)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    companion object {
        fun createBundle(data: StatisticsTagValuesSettingsParams) = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}
