package com.example.util.simpletimetracker.feature_dialogs.cardSize.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.extension.onProgressChanged
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.cardSize.adapter.CardSizeAdapter
import com.example.util.simpletimetracker.feature_dialogs.cardSize.di.CardSizeComponentProvider
import com.example.util.simpletimetracker.feature_dialogs.cardSize.viewModel.CardSizeViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.card_size_dialog_fragment.buttonsCardSize
import kotlinx.android.synthetic.main.card_size_dialog_fragment.rvCardSizeContainer
import kotlinx.android.synthetic.main.card_size_dialog_fragment.seekbarCardSize
import javax.inject.Inject

class CardSizeDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<CardSizeViewModel>

    private val viewModel: CardSizeViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private val recordTypesAdapter: CardSizeAdapter by lazy {
        CardSizeAdapter()
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
        return inflater.inflate(R.layout.card_size_dialog_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initDialog()
        initDi()
        initUi()
        initUx()
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
    }

    private fun initDi() {
        (activity?.application as CardSizeComponentProvider)
            .cardSizeComponent
            ?.inject(this)
    }

    private fun initUi() {
        rvCardSizeContainer.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = recordTypesAdapter
        }
    }

    private fun initUx() {
        seekbarCardSize.onProgressChanged(viewModel::onProgressChanged)
        buttonsCardSize.listener = viewModel::onButtonClick
    }

    private fun initViewModel(): Unit = with(viewModel) {
        recordTypes.observe(viewLifecycleOwner, recordTypesAdapter::replace)
        progress.observe(viewLifecycleOwner, seekbarCardSize::setProgress)
        buttonsViewData.observe(viewLifecycleOwner, buttonsCardSize.adapter::replace)
    }
}