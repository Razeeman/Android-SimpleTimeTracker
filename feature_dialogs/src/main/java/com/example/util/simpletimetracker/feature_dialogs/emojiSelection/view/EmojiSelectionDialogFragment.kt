package com.example.util.simpletimetracker.feature_dialogs.emojiSelection.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.emoji.createEmojiAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.EmojiSelectionDialogListener
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.emojiSelection.di.EmojiSelectionComponentProvider
import com.example.util.simpletimetracker.feature_dialogs.emojiSelection.viewModel.EmojiSelectionViewModel
import com.example.util.simpletimetracker.navigation.params.EmojiSelectionDialogParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.emoji_selection_dialog_fragment.*
import javax.inject.Inject

class EmojiSelectionDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<EmojiSelectionViewModel>

    private val viewModel: EmojiSelectionViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private val adapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createEmojiAdapterDelegate(viewModel::onEmojiClick)
        )
    }

    private val params: EmojiSelectionDialogParams by lazy {
        arguments?.getParcelable(ARGS_PARAMS) ?: EmojiSelectionDialogParams()
    }
    private var emojiSelectionDialogListener: EmojiSelectionDialogListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.emoji_selection_dialog_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initDialog()
        initDi()
        initUi()
        initUx()
        initViewModel()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is EmojiSelectionDialogListener -> {
                emojiSelectionDialogListener = context
                return
            }
            is AppCompatActivity -> {
                context.getAllFragments()
                    .firstOrNull { it is EmojiSelectionDialogListener && it.isResumed }
                    ?.let { emojiSelectionDialogListener = it as? EmojiSelectionDialogListener }
            }
        }
    }

    private fun initDialog() {
        setSkipCollapsed()
    }

    private fun initDi() {
        (activity?.application as EmojiSelectionComponentProvider)
            .emojiSelectionComponent
            ?.inject(this)
    }

    private fun initUi() {
        rvEmojiSelectionContainer.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = this@EmojiSelectionDialogFragment.adapter
        }
    }

    private fun initUx() {
        // Do nothing
    }

    private fun initViewModel(): Unit = with(viewModel) {
        extra = params
        icons.observe(viewLifecycleOwner, adapter::replace)
        iconSelected.observe(viewLifecycleOwner, ::onEmojiSelected)
    }

    private fun onEmojiSelected(emojiText: String) {
        emojiSelectionDialogListener?.onEmojiSelected(emojiText)
        dismiss()
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is EmojiSelectionDialogParams -> putParcelable(ARGS_PARAMS, data)
            }
        }
    }
}