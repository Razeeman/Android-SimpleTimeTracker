package com.example.util.simpletimetracker.feature_dialogs.cardOrder.view

import com.example.util.simpletimetracker.feature_dialogs.databinding.CardOrderDialogFragmentBinding as Binding
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.extension.onItemMoved
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.cardOrder.viewModel.CardOrderViewModel
import com.example.util.simpletimetracker.navigation.params.screen.CardOrderDialogParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CardOrderDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: CardOrderViewModel by viewModels()

    private val recordTypesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createEmptyAdapterDelegate(),
            createRecordTypeAdapterDelegate(),
            createLoaderAdapterDelegate(),
        )
    }
    private val extra: CardOrderDialogParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = CardOrderDialogParams(),
    )

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.onDismiss(recordTypesAdapter.currentList)
    }

    override fun initDialog() {
        setSkipCollapsed()
        setFullScreen()
    }

    override fun initUi() {
        binding.rvCardOrderContainer.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = recordTypesAdapter
        }
    }

    override fun initUx() {
        binding.rvCardOrderContainer.onItemMoved(
            onSelected = ::setItemSelected,
            onClear = ::setItemUnselected,
        )
    }

    private fun setItemSelected(viewHolder: RecyclerView.ViewHolder?) = viewHolder?.run {
        itemView.alpha = ITEM_ALPHA_SELECTED
        itemView.scaleX = ITEM_SCALE_SELECTED
        itemView.scaleY = ITEM_SCALE_SELECTED
    } ?: Unit

    private fun setItemUnselected(viewHolder: RecyclerView.ViewHolder) = viewHolder.run {
        itemView.alpha = ITEM_ALPHA_DEFAULT
        itemView.scaleX = ITEM_SCALE_DEFAULT
        itemView.scaleY = ITEM_SCALE_DEFAULT
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = this@CardOrderDialogFragment.extra
        recordTypes.observe(recordTypesAdapter::replace)
    }

    companion object {
        private const val ITEM_ALPHA_SELECTED = 0.7f
        private const val ITEM_ALPHA_DEFAULT = 1.0f

        private const val ITEM_SCALE_SELECTED = 1.1f
        private const val ITEM_SCALE_DEFAULT = 1.0f

        private const val ARGS_PARAMS = "args_card_order_params"

        fun createBundle(data: CardOrderDialogParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}