package com.example.util.simpletimetracker.feature_change_category.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.transition.TransitionInflater
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.rotateDown
import com.example.util.simpletimetracker.core.extension.rotateUp
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.core.utils.BuildVersions
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.core.viewData.CategoryViewData
import com.example.util.simpletimetracker.feature_change_category.R
import com.example.util.simpletimetracker.feature_change_category.adapter.ChangeCategoryAdapter
import com.example.util.simpletimetracker.feature_change_category.di.ChangeCategoryComponentProvider
import com.example.util.simpletimetracker.feature_change_category.extra.ChangeCategoryExtra
import com.example.util.simpletimetracker.feature_change_category.viewModel.ChangeCategoryViewModel
import com.example.util.simpletimetracker.navigation.params.ChangeCategoryParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.change_category_fragment.*
import javax.inject.Inject

class ChangeCategoryFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<ChangeCategoryViewModel>

    private val viewModel: ChangeCategoryViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val colorsAdapter: ChangeCategoryAdapter by lazy {
        ChangeCategoryAdapter(viewModel::onColorClick)
    }
    private val params: ChangeCategoryParams by lazy {
        arguments?.getParcelable(ARGS_PARAMS) ?: ChangeCategoryParams()
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
        return inflater.inflate(R.layout.change_category_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initDialog()
        initDi()
        initUi()
        initUx()
        initViewModel()
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
        (activity?.application as ChangeCategoryComponentProvider)
            .changeCategoryComponent
            ?.inject(this)
    }

    private fun initUi() {
        if (BuildVersions.isLollipopOrHigher()) {
            sharedElementEnterTransition = TransitionInflater.from(context)
                .inflateTransition(android.R.transition.move)
        }

        ViewCompat.setTransitionName(
            previewChangeCategory,
            TransitionNames.CATEGORY + params.id
        )

        rvChangeCategoryColor.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = colorsAdapter
        }
    }

    private fun initUx() {
        etChangeCategoryName.doAfterTextChanged { viewModel.onNameChange(it.toString()) }
        fieldChangeCategoryColor.setOnClick(viewModel::onColorChooserClick)
        btnChangeCategorySave.setOnClick(viewModel::onSaveClick)
        btnChangeCategoryDelete.setOnClick(viewModel::onDeleteClick)
    }

    private fun initViewModel(): Unit = with(viewModel) {
        extra = ChangeCategoryExtra(params.id)
        deleteIconVisibility.observeOnce(viewLifecycleOwner, btnChangeCategoryDelete::visible::set)
        saveButtonEnabled.observe(viewLifecycleOwner, btnChangeCategorySave::setEnabled)
        deleteButtonEnabled.observe(viewLifecycleOwner, btnChangeCategoryDelete::setEnabled)
        categoryPreview.observeOnce(viewLifecycleOwner, ::updateUi)
        categoryPreview.observe(viewLifecycleOwner, ::updatePreview)
        colors.observe(viewLifecycleOwner, colorsAdapter::replace)
        flipColorChooser.observe(viewLifecycleOwner) { opened ->
            rvChangeCategoryColor.visible = opened
            arrowChangeCategoryColor.apply {
                if (opened) rotateDown() else rotateUp()
            }
        }
        keyboardVisibility.observe(viewLifecycleOwner) { visible ->
            if (visible) showKeyboard(etChangeCategoryName) else hideKeyboard()
        }
    }

    private fun updateUi(item: CategoryViewData) {
        etChangeCategoryName.setText(item.name)
        etChangeCategoryName.setSelection(item.name.length)
    }

    private fun updatePreview(item: CategoryViewData) {
        with(previewChangeCategory) {
            itemName = item.name
            itemColor = item.color
        }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is ChangeCategoryParams -> putParcelable(ARGS_PARAMS, data)
            }
        }
    }
}