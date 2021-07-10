package com.example.util.simpletimetracker.feature_dialogs.archive.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.ArchiveDialogListener
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.archive.adapter.createArchiveDialogButtonsAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.archive.adapter.createArchiveDialogInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.archive.adapter.createArchiveDialogTitleAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.archive.viewModel.ArchiveDialogViewModel
import com.example.util.simpletimetracker.navigation.params.ArchiveDialogParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_dialogs.databinding.ArchiveDialogFragmentBinding as Binding

@AndroidEntryPoint
class ArchiveDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<ArchiveDialogViewModel>

    private val viewModel: ArchiveDialogViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val adapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createRecordTypeAdapterDelegate(),
            createCategoryAdapterDelegate(),
            createArchiveDialogInfoAdapterDelegate(),
            createArchiveDialogTitleAdapterDelegate(),
            createArchiveDialogButtonsAdapterDelegate(
                onDeleteClick = ::onDeleteClick,
                onRestoreClick = ::onRestoreClick
            )
        )
    }
    private val params: ArchiveDialogParams by lazy {
        arguments?.getParcelable<ArchiveDialogParams>(ARGS_PARAMS)
            ?: ArchiveDialogParams.Activity(0)
    }
    private var archiveDialogListener: ArchiveDialogListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is ArchiveDialogListener -> {
                archiveDialogListener = context
                return
            }
            is AppCompatActivity -> {
                context.getAllFragments()
                    .firstOrNull { it is ArchiveDialogListener && it.isResumed }
                    ?.let { archiveDialogListener = it as? ArchiveDialogListener }
            }
        }
    }

    override fun initDialog() {
        setSkipCollapsed()
    }

    override fun initUi() {
        binding.rvArchiveDialogContent.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = this@ArchiveDialogFragment.adapter
        }
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        viewData.observe(adapter::replace)
    }

    private fun onDeleteClick() {
        GlobalScope.launch { archiveDialogListener?.onDeleteClick(params) }
        dismiss()
    }

    private fun onRestoreClick() {
        GlobalScope.launch { archiveDialogListener?.onRestoreClick(params) }
        dismiss()
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is ArchiveDialogParams -> putParcelable(ARGS_PARAMS, data)
            }
        }
    }
}