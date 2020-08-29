package com.example.util.simpletimetracker.feature_dialogs.cardOrder.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.cardOrder.adapter.CardOrderAdapter
import com.example.util.simpletimetracker.feature_dialogs.cardOrder.di.CardOrderComponentProvider
import com.example.util.simpletimetracker.feature_dialogs.cardOrder.viewModel.CardOrderViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.card_order_dialog_fragment.rvCardOrderContainer
import javax.inject.Inject

class CardOrderDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<CardOrderViewModel>

    private val viewModel: CardOrderViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private val recordTypesAdapter: CardOrderAdapter by lazy {
        CardOrderAdapter()
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
        return inflater.inflate(R.layout.card_order_dialog_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initDialog()
        initDi()
        initUi()
        initViewModel()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.onDismiss()
    }

    private fun initDialog() {
        dialog?.findViewById<FrameLayout>(R.id.design_bottom_sheet)?.let { bottomSheet ->
            behavior = BottomSheetBehavior.from(bottomSheet)
        }
        behavior?.apply {
            peekHeight = 0
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        // Dialog parent is R.id.design_bottom_sheet from android material.
        // It's a wrapper created around dialog to set bottom sheet behavior. By default it's created
        // with wrap_content height, so we replace it here.
        (view?.parent as? FrameLayout)?.apply {
            layoutParams?.height = CoordinatorLayout.LayoutParams.MATCH_PARENT
            requestLayout() // TODO necessary?
        }
    }

    private fun initDi() {
        (activity?.application as CardOrderComponentProvider)
            .cardOrderComponent
            ?.inject(this)
    }

    private fun initUi() {
        rvCardOrderContainer.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = recordTypesAdapter
        }
        ItemTouchHelper(callback()).attachToRecyclerView(rvCardOrderContainer)
    }

    private fun callback() =
        object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.DOWN or ItemTouchHelper.UP or ItemTouchHelper.START or ItemTouchHelper.END,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                viewModel.onCardMoved(viewHolder.adapterPosition, target.adapterPosition)
                recordTypesAdapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // DO nothing
            }
        }

    private fun initViewModel(): Unit = with(viewModel) {
        recordTypes.observe(viewLifecycleOwner, recordTypesAdapter::replace)
    }
}