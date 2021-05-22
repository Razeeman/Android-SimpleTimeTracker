package com.example.util.simpletimetracker.feature_dialogs.typesFilter.view

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.TypesFilterDialogListener
import com.example.util.simpletimetracker.core.extension.addOnScrollListenerAdapter
import com.example.util.simpletimetracker.core.extension.behavior
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.typesFilter.di.TypesFilterComponentProvider
import com.example.util.simpletimetracker.feature_dialogs.typesFilter.viewModel.TypesFilterViewModel
import com.example.util.simpletimetracker.navigation.params.TypesFilterDialogParams
import com.example.util.simpletimetracker.navigation.params.TypesFilterParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.types_filter_dialog_fragment.*
import javax.inject.Inject

class TypesFilterDialogFragment : BottomSheetDialogFragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.types_filter_dialog_fragment,
            container,
            false
        )
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

    private fun initDialog() {
        setSkipCollapsed()
        setFullScreen()
    }

    private fun initDi() {
        (activity?.application as TypesFilterComponentProvider)
            .typesFilterComponent
            ?.inject(this)
    }

    private fun initUi() {
        rvTypesFilterContainer.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = this@TypesFilterDialogFragment.adapter
        }
    }

    private fun initUx() {
        btnTypesFilterShowAll.setOnClick(viewModel::onShowAllClick)
        btnTypesFilterHideAll.setOnClick(viewModel::onHideAllClick)
        checkContentScroll()
    }

    private fun initViewModel(): Unit = with(viewModel) {
        extra = arguments?.getParcelable(ARGS_PARAMS) ?: TypesFilterParams()
        viewData.observe(viewLifecycleOwner, adapter::replace)
        typesFilter.observe(viewLifecycleOwner) { typesFilterDialogListener?.onTypesFilterSelected(it) }
    }

    // Disable sheet swipe on content scroll to avoid accidentally closing payment sheet when scrolling items.
    private fun checkContentScroll() {
        rvTypesFilterContainer.addOnScrollListenerAdapter(
            onScrolled = { _, _, dy ->
                if (dy != 0) behavior?.isDraggable = false
            },
            onScrollStateChanged = { _, newState ->
                if (newState == RecyclerView.SCROLL_STATE_IDLE) behavior?.isDraggable = true
            }
        )
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