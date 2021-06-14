package com.example.util.simpletimetracker.feature_dialogs.cardOrder.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.extension.onItemMoved
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.cardOrder.viewModel.CardOrderViewModel
import com.example.util.simpletimetracker.navigation.params.CardOrderDialogParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.card_order_dialog_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class CardOrderDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<CardOrderViewModel>

    private val viewModel: CardOrderViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val recordTypesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createEmptyAdapterDelegate(),
            createRecordTypeAdapterDelegate(),
            createLoaderAdapterDelegate()
        )
    }
    private val extra: CardOrderDialogParams by lazy {
        arguments?.getParcelable(ARGS_PARAMS) ?: CardOrderDialogParams()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.card_order_dialog_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initDialog()
        initUi()
        initUx()
        initViewModel()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.onDismiss()
    }

    private fun initDialog() {
        setSkipCollapsed()
        setFullScreen()
    }

    private fun initUi() {
        rvCardOrderContainer.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = recordTypesAdapter
            itemAnimator = null
        }
    }

    private fun initUx() {
        rvCardOrderContainer.onItemMoved(
            onSelected = ::setItemSelected,
            onClear = ::setItemUnselected,
            onMoved = viewModel::onCardMoved
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

    private fun initViewModel(): Unit = with(viewModel) {
        extra = this@CardOrderDialogFragment.extra
        recordTypes.observe(viewLifecycleOwner, recordTypesAdapter::replace)
    }

    companion object {
        private const val ITEM_ALPHA_SELECTED = 0.7f
        private const val ITEM_ALPHA_DEFAULT = 1.0f

        private const val ITEM_SCALE_SELECTED = 1.1f
        private const val ITEM_SCALE_DEFAULT = 1.0f

        private const val ARGS_PARAMS = "args_card_order_params"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is CardOrderDialogParams -> putParcelable(ARGS_PARAMS, data)
            }
        }
    }
}