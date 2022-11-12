package com.example.util.simpletimetracker.feature_change_activity_filter.view

import com.example.util.simpletimetracker.feature_change_activity_filter.databinding.ChangeActivityFilterFragmentBinding as Binding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.ColorSelectionDialogListener
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setSharedTransitions
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.core.utils.setChooserColor
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorPaletteAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_change_activity_filter.viewModel.ChangeActivityFilterViewModel
import com.example.util.simpletimetracker.feature_views.extension.rotateDown
import com.example.util.simpletimetracker.feature_views.extension.rotateUp
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.ChangeActivityFilterParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangeActivityFilterFragment :
    BaseFragment<Binding>(),
    ColorSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding = Binding::inflate

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<ChangeActivityFilterViewModel>

    private val viewModel: ChangeActivityFilterViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val colorsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createColorAdapterDelegate(viewModel::onColorClick),
            createColorPaletteAdapterDelegate(viewModel::onColorPaletteClick),
        )
    }
    private val viewDataAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onTypeClick),
            createCategoryAdapterDelegate(viewModel::onCategoryClick),
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createEmptyAdapterDelegate()
        )
    }

    private val params: ChangeActivityFilterParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = ChangeActivityFilterParams.New
    )

    override fun initUi(): Unit = with(binding) {
        setPreview()

        setSharedTransitions(
            additionalCondition = { params !is ChangeActivityFilterParams.New },
            transitionName = (params as? ChangeActivityFilterParams.Change)?.transitionName.orEmpty(),
            sharedView = previewChangeActivityFilter
        )

        rvChangeActivityFilterColor.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = colorsAdapter
        }

        rvChangeActivityFilterType.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = viewDataAdapter
        }
    }

    override fun initUx() = with(binding) {
        etChangeActivityFilterName.doAfterTextChanged { viewModel.onNameChange(it.toString()) }
        fieldChangeActivityFilterColor.setOnClick(viewModel::onColorChooserClick)
        fieldChangeActivityFilterType.setOnClick(viewModel::onTypeChooserClick)
        btnChangeActivityFilterSave.setOnClick(viewModel::onSaveClick)
        btnChangeActivityFilterDelete.setOnClick(viewModel::onDeleteClick)
        buttonsChangeActivityFilterType.listener = viewModel::onFilterTypeClick
    }

    override fun initViewModel(): Unit = with(binding) {
        with(viewModel) {
            extra = params
            deleteIconVisibility.observeOnce(viewLifecycleOwner, btnChangeActivityFilterDelete::visible::set)
            saveButtonEnabled.observe(btnChangeActivityFilterSave::setEnabled)
            deleteButtonEnabled.observe(btnChangeActivityFilterDelete::setEnabled)
            filterPreview.observeOnce(viewLifecycleOwner, ::updateUi)
            filterPreview.observe(::updatePreview)
            colors.observe(colorsAdapter::replace)
            filterTypeViewData.observe(buttonsChangeActivityFilterType.adapter::replace)
            viewData.observe(viewDataAdapter::replace)
            flipColorChooser.observe { opened ->
                rvChangeActivityFilterColor.visible = opened
                fieldChangeActivityFilterColor.setChooserColor(opened)
                arrowChangeActivityFilterColor.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            flipTypesChooser.observe { opened ->
                containerChangeActivityFilterActivities.visible = opened
                fieldChangeActivityFilterType.setChooserColor(opened)
                arrowChangeActivityFilterType.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            keyboardVisibility.observe { visible ->
                if (visible) showKeyboard(etChangeActivityFilterName) else hideKeyboard()
            }
        }
    }

    override fun onColorSelected(colorInt: Int) {
        viewModel.onCustomColorSelected(colorInt)
    }

    private fun updateUi(item: ActivityFilterViewData) = with(binding) {
        etChangeActivityFilterName.setText(item.name)
        etChangeActivityFilterName.setSelection(item.name.length)
    }

    private fun setPreview() = (params as? ChangeActivityFilterParams.Change)?.preview?.run {
        with(binding.previewChangeActivityFilter) {
            itemName = name
            itemColor = color
        }
    }

    private fun updatePreview(item: ActivityFilterViewData) {
        with(binding.previewChangeActivityFilter) {
            itemName = item.name
            itemColor = item.color
        }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: ChangeActivityFilterParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}