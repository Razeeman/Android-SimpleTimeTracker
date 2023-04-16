package com.example.util.simpletimetracker.feature_data_edit.dialog

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.extension.blockContentScroll
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_data_edit.databinding.DataEditTypeSelectionDialogFragmentBinding as Binding

@AndroidEntryPoint
class DataEditTypeSelectionDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: DataEditTypeSelectionViewModel by viewModels()

    private val adapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createHintAdapterDelegate(),
            createRecordTypeAdapterDelegate(::dismiss),
        )
    }

    private var listener: DataEditTypeSelectionDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is DataEditTypeSelectionDialogListener -> {
                listener = context
                return
            }
            is AppCompatActivity -> {
                context.getAllFragments()
                    .firstOrNull { it is DataEditTypeSelectionDialogListener && it.isResumed }
                    ?.let { listener = it as? DataEditTypeSelectionDialogListener }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        listener?.onDismissed()
        super.onDismiss(dialog)
    }

    override fun initDialog() {
        setSkipCollapsed()
        setFullScreen()
        blockContentScroll(binding.rvDataEditTypeSelectionContainer)
    }

    override fun initUi(): Unit = with(binding) {
        rvDataEditTypeSelectionContainer.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = this@DataEditTypeSelectionDialogFragment.adapter
        }
    }

    override fun initViewModel(): Unit = with(viewModel) {
        viewData.observe(adapter::replace)
    }

    private fun dismiss(item: RecordTypeViewData) {
        listener?.onTypeSelected(item.id)
        dismiss()
    }
}