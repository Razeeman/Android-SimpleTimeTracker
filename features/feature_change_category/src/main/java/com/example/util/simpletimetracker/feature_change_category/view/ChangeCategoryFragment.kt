package com.example.util.simpletimetracker.feature_change_category.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.transition.TransitionInflater
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.feature_views.extension.rotateDown
import com.example.util.simpletimetracker.feature_views.extension.rotateUp
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.core.utils.BuildVersions
import com.example.util.simpletimetracker.core.utils.setFlipChooserColor
import com.example.util.simpletimetracker.feature_change_category.viewModel.ChangeCategoryViewModel
import com.example.util.simpletimetracker.navigation.params.ChangeCategoryParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_change_category.databinding.ChangeCategoryFragmentBinding as Binding

@AndroidEntryPoint
class ChangeCategoryFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding = Binding::inflate

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<ChangeCategoryViewModel>

    private val viewModel: ChangeCategoryViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val colorsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createColorAdapterDelegate(viewModel::onColorClick)
        )
    }
    private val typesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onTypeClick),
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createEmptyAdapterDelegate()
        )
    }

    // TODO by delegate?
    private val params: ChangeCategoryParams by lazy {
        arguments?.getParcelable<ChangeCategoryParams>(ARGS_PARAMS) ?: ChangeCategoryParams.New
    }

    override fun initUi(): Unit = with(binding) {
        setPreview()

        // TODO move to utils
        if (BuildVersions.isLollipopOrHigher() && params !is ChangeCategoryParams.New) {
            sharedElementEnterTransition = TransitionInflater.from(context)
                .inflateTransition(android.R.transition.move)
        }

        val transitionName: String = (params as? ChangeCategoryParams.Change)?.transitionName.orEmpty()
        ViewCompat.setTransitionName(previewChangeCategory, transitionName)

        rvChangeCategoryColor.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = colorsAdapter
        }

        rvChangeCategoryType.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = typesAdapter
        }
    }

    override fun initUx() = with(binding) {
        etChangeCategoryName.doAfterTextChanged { viewModel.onNameChange(it.toString()) }
        fieldChangeCategoryColor.setOnClick(viewModel::onColorChooserClick)
        fieldChangeCategoryType.setOnClick(viewModel::onTypeChooserClick)
        btnChangeCategorySave.setOnClick(viewModel::onSaveClick)
        btnChangeCategoryDelete.setOnClick(viewModel::onDeleteClick)
    }

    override fun initViewModel(): Unit = with(binding) {
        with(viewModel) {
            extra = params
            deleteIconVisibility.observeOnce(viewLifecycleOwner, btnChangeCategoryDelete::visible::set)
            saveButtonEnabled.observe(btnChangeCategorySave::setEnabled)
            deleteButtonEnabled.observe(btnChangeCategoryDelete::setEnabled)
            categoryPreview.observeOnce(viewLifecycleOwner, ::updateUi)
            categoryPreview.observe(::updatePreview)
            colors.observe(colorsAdapter::replace)
            types.observe(typesAdapter::replace)
            flipColorChooser.observe { opened ->
                rvChangeCategoryColor.visible = opened
                setFlipChooserColor(fieldChangeCategoryColor, opened)
                arrowChangeCategoryColor.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            flipTypesChooser.observe { opened ->
                rvChangeCategoryType.visible = opened
                setFlipChooserColor(fieldChangeCategoryType, opened)
                arrowChangeCategoryType.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            keyboardVisibility.observe { visible ->
                if (visible) showKeyboard(etChangeCategoryName) else hideKeyboard()
            }
        }
    }

    private fun updateUi(item: CategoryViewData) = with(binding) {
        etChangeCategoryName.setText(item.name)
        etChangeCategoryName.setSelection(item.name.length)
    }

    private fun setPreview() = (params as? ChangeCategoryParams.Change)?.preview?.run {
        with(binding.previewChangeCategory) {
            itemName = name
            itemColor = color
        }
    }

    private fun updatePreview(item: CategoryViewData) {
        with(binding.previewChangeCategory) {
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