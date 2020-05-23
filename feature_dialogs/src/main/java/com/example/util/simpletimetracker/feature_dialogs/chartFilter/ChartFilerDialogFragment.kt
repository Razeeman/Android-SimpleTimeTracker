package com.example.util.simpletimetracker.feature_dialogs.chartFilter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.feature_dialogs.R
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.chart_filter_dialog_fragment.*
import javax.inject.Inject

class ChartFilerDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<ChartFilterViewModel>

    private val viewModel: ChartFilterViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private val recordTypesAdapter: ChartFilterAdapter by lazy {
        ChartFilterAdapter(viewModel::onRecordTypeClick)
    }

    private var behavior: BottomSheetBehavior<View>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.chart_filter_dialog_fragment,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO move to base
        initDialog()
        initDi()
        initUi()
        initUx()
        initViewModel()
    }

    private fun initDialog() {
        dialog?.findViewById<FrameLayout>(R.id.design_bottom_sheet)?.let { bottomSheet ->
            behavior = BottomSheetBehavior.from<View>(bottomSheet)
        }
        behavior?.apply {
            peekHeight = 0
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun initDi() {
        (activity?.application as ChartFilterComponentProvider)
            .chartFilterComponent
            ?.inject(this)
    }

    private fun initUi() {
        rvChartFilterContainer.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = recordTypesAdapter
        }
    }

    private fun initUx() {
        // TODO
    }

    private fun initViewModel(): Unit = with(viewModel) {
        recordTypes.observe(viewLifecycleOwner, recordTypesAdapter::replace)
    }
}