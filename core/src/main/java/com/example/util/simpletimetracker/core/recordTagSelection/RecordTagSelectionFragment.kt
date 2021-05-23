package com.example.util.simpletimetracker.core.recordTagSelection

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.navigation.params.RecordTagSelectionParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.record_tag_selection_fragment.*
import javax.inject.Inject

class RecordTagSelectionFragment : BaseFragment(R.layout.record_tag_selection_fragment) {

    interface OnTagSelectedListener {
        fun onTagSelected()
    }

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<RecordTagSelectionViewModel>

    private val viewModel: RecordTagSelectionViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private val adapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createHintAdapterDelegate(),
            createCategoryAdapterDelegate(viewModel::onCategoryClick)
        )
    }
    private val params: RecordTagSelectionParams by lazy {
        arguments?.getParcelable(ARGS_PARAMS) ?: RecordTagSelectionParams()
    }
    var listener: OnTagSelectedListener? = null

    override fun initDi() {
        (activity?.application as RecordTagSelectionComponentProvider)
            .recordTagSelectionComponent
            ?.inject(this)
    }

    override fun initUi() {
        rvRecordTagSelectionList.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = this@RecordTagSelectionFragment.adapter
        }
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        viewData.observe(viewLifecycleOwner, adapter::replace)
        tagSelected.observe(viewLifecycleOwner, ::onTagSelected)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onTagSelected(unused: Unit) {
        listener?.onTagSelected()
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun newInstance(data: RecordTagSelectionParams) =
            RecordTagSelectionFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARGS_PARAMS, data)
                }
            }
    }
}
