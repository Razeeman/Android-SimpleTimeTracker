package com.example.util.simpletimetracker.feature_dialogs.typesFilter.view

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseBottomSheetBindingFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.TypesFilterDialogListener
import com.example.util.simpletimetracker.core.extension.blockContentScroll
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.typesFilter.viewModel.TypesFilterViewModel
import com.example.util.simpletimetracker.navigation.params.TypesFilterDialogParams
import com.example.util.simpletimetracker.navigation.params.TypesFilterParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_dialogs.databinding.TypesFilterDialogFragmentBinding as Binding

@AndroidEntryPoint
class TypesFilterDialogFragment : BaseBottomSheetBindingFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<TypesFilterViewModel>

    private val viewModel: TypesFilterViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private val adapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createHintAdapterDelegate(),
            createDividerAdapterDelegate(),
            createRecordTypeAdapterDelegate(viewModel::onRecordTypeClick),
            createCategoryAdapterDelegate(viewModel::onCategoryClick)
        )
    }

    private var typesFilterDialogListener: TypesFilterDialogListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is TypesFilterDialogListener -> {
                typesFilterDialogListener = context
                return
            }
            is AppCompatActivity -> {
                context.getAllFragments()
                    .firstOrNull { it is TypesFilterDialogListener && it.isResumed }
                    ?.let { typesFilterDialogListener = it as? TypesFilterDialogListener }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        typesFilterDialogListener?.onTypesFilterDismissed()
        super.onDismiss(dialog)
    }

    override fun initDialog() {
        setSkipCollapsed()
        setFullScreen()
        blockContentScroll(binding.rvTypesFilterContainer)
    }

    override fun initUi() {
        binding.rvTypesFilterContainer.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = this@TypesFilterDialogFragment.adapter
        }
    }

    override fun initUx(): Unit = with(binding) {
        btnTypesFilterShowAll.setOnClick(viewModel::onShowAllClick)
        btnTypesFilterHideAll.setOnClick(viewModel::onHideAllClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = arguments?.getParcelable(ARGS_PARAMS) ?: TypesFilterParams()
        viewData.observe(adapter::replace)
        typesFilter.observe { typesFilterDialogListener?.onTypesFilterSelected(it) }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is TypesFilterDialogParams -> {
                    putParcelable(ARGS_PARAMS, data.filter)
                }
            }
        }
    }
}