package com.example.util.simpletimetracker.feature_tag_selection.view

import com.example.util.simpletimetracker.feature_tag_selection.databinding.RecordTagSelectionFragmentBinding as Binding
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.dialog.OnTagSelectedListener
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_tag_selection.viewModel.RecordTagSelectionViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecordTagSelectionFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: RecordTagSelectionViewModel by viewModels()

    private val adapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createHintAdapterDelegate(),
            createCategoryAdapterDelegate(viewModel::onCategoryClick),
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createEmptyAdapterDelegate(),
        )
    }
    private val params: RecordTagSelectionParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = RecordTagSelectionParams()
    )
    private val listeners: MutableList<OnTagSelectedListener> = mutableListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is OnTagSelectedListener -> {
                listeners.add(context)
            }
            is AppCompatActivity -> {
                context.getAllFragments()
                    .filter { it is OnTagSelectedListener }
                    .mapNotNull { it as? OnTagSelectedListener }
                    .let(listeners::addAll)
            }
        }
    }

    override fun initUi(): Unit = with(binding) {
        rvRecordTagSelectionList.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = this@RecordTagSelectionFragment.adapter
        }
    }

    override fun initUx() = with(binding) {
        btnRecordTagSelectionSave.setOnClick(viewModel::onSaveClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        viewData.observe(adapter::replace)
        saveButtonVisibility.observe(binding.btnRecordTagSelectionSave::visible::set)
        tagSelected.observe { onTagSelected() }
    }

    private fun onTagSelected() {
        listeners.forEach { it.onTagSelected() }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun newInstance(data: RecordTagSelectionParams) = RecordTagSelectionFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGS_PARAMS, data)
            }
        }
    }
}
