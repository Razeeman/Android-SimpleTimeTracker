package com.example.util.simpletimetracker.feature_dialogs.optionsDialog

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.feature_dialogs.api.OptionsListDialogListener
import com.example.util.simpletimetracker.core.extension.findListeners
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.optionsList.OptionsListViewData
import com.example.util.simpletimetracker.feature_base_adapter.optionsList.createOptionsListAdapterDelegate
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_dialogs.databinding.OptionsListDialogFragmentBinding as Binding

@AndroidEntryPoint
class OptionsListDialogFragment :
    BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: OptionsListViewModel by viewModels()

    private val contentAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createOptionsListAdapterDelegate(::onItemClick),
        )
    }

    private val params: OptionsListParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = OptionsListParams.Empty,
    )
    private var listeners: List<OptionsListDialogListener> = emptyList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listeners = context.findListeners()
        listeners.forEach { it.onOptionsDialogOpened() }
    }

    override fun initDialog() {
        setSkipCollapsed()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listeners.forEach { it.onOptionsDialogClosed() }
    }

    override fun initUi(): Unit = with(binding) {
        rvOptionsList.apply {
            val manager = LinearLayoutManager(context)
            layoutManager = manager
            adapter = contentAdapter
        }
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        state.observe(contentAdapter::replace)
    }

    private fun onItemClick(item: OptionsListViewData) {
        fun onClick(item: OptionsListViewData) {
            val id = (item.id as? OptionsListItemId)?.id ?: return
            listeners.forEach { it.onOptionsItemClick(id) }
        }
        router.back()
        item.let(throttle(::onClick))
    }

    companion object {
        fun createBundle(data: OptionsListParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}