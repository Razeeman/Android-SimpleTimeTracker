package com.example.util.simpletimetracker.feature_change_record_tag.view

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
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.core.utils.setChooserColor
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorPaletteAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagTypeSetupViewData
import com.example.util.simpletimetracker.feature_change_record_tag.viewModel.ChangeRecordTagViewModel
import com.example.util.simpletimetracker.feature_views.extension.rotateDown
import com.example.util.simpletimetracker.feature_views.extension.rotateUp
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_change_record_tag.databinding.ChangeRecordTagFragmentBinding as Binding

@AndroidEntryPoint
class ChangeRecordTagFragment :
    BaseFragment<Binding>(),
    ColorSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<ChangeRecordTagViewModel>

    private val viewModel: ChangeRecordTagViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val colorsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createColorAdapterDelegate(viewModel::onColorClick),
            createColorPaletteAdapterDelegate(viewModel::onColorPaletteClick),
        )
    }
    private val typesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createEmptyAdapterDelegate(),
            createRecordTypeAdapterDelegate(viewModel::onTypeClick)
        )
    }

    private val params: ChangeTagData by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = ChangeTagData.New
    )

    override fun initUi(): Unit = with(binding) {
        setPreview()

        setSharedTransitions(
            transitionName = (params as? ChangeTagData.Change)?.transitionName.orEmpty(),
            sharedView = previewChangeRecordTag,
        )

        rvChangeRecordTagColor.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = colorsAdapter
        }

        rvChangeRecordTagType.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = typesAdapter
        }
    }

    override fun initUx() = with(binding) {
        etChangeRecordTagName.doAfterTextChanged { viewModel.onNameChange(it.toString()) }
        fieldChangeRecordTagColor.setOnClick(viewModel::onColorChooserClick)
        fieldChangeRecordTagType.setOnClick(viewModel::onTypeChooserClick)
        btnChangeRecordTagSave.setOnClick(viewModel::onSaveClick)
        btnChangeRecordTagDelete.setOnClick(viewModel::onDeleteClick)
        buttonsChangeRecordTagType.listener = viewModel::onTagTypeClick
    }

    override fun initViewModel(): Unit = with(binding) {
        with(viewModel) {
            extra = params
            deleteIconVisibility.observeOnce(viewLifecycleOwner, btnChangeRecordTagDelete::visible::set)
            tagTypeSetupViewData.observe(::setTagTypeSetup)
            saveButtonEnabled.observe(btnChangeRecordTagSave::setEnabled)
            deleteButtonEnabled.observe(btnChangeRecordTagDelete::setEnabled)
            typeSelectionVisibility.observe(groupChangeRecordTagTypeSelection::visible::set)
            preview.observeOnce(viewLifecycleOwner, ::updateUi)
            preview.observe(::updatePreview)
            tagTypeViewData.observe(buttonsChangeRecordTagType.adapter::replace)
            colors.observe(colorsAdapter::replace)
            types.observe(typesAdapter::replace)
            flipColorChooser.observe { opened ->
                rvChangeRecordTagColor.visible = opened
                fieldChangeRecordTagColor.setChooserColor(opened)
                arrowChangeRecordTagColor.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            flipTypesChooser.observe { opened ->
                rvChangeRecordTagType.visible = opened
                fieldChangeRecordTagType.setChooserColor(opened)
                arrowChangeRecordTagType.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            keyboardVisibility.observe { visible ->
                if (visible) showKeyboard(etChangeRecordTagName) else hideKeyboard()
            }
        }
    }

    override fun onColorSelected(colorInt: Int) {
        viewModel.onCustomColorSelected(colorInt)
    }

    private fun updateUi(item: CategoryViewData) = with(binding) {
        etChangeRecordTagName.setText(item.name)
        etChangeRecordTagName.setSelection(item.name.length)
    }

    private fun setTagTypeSetup(data: ChangeRecordTagTypeSetupViewData) = with(binding) {
        fieldChangeRecordTagColor.visible = data.colorChooserVisibility
        fieldChangeRecordTagType.visible = data.typesChooserVisibility
        tvChangeRecordTagTypeHint.text = data.hint
    }

    private fun setPreview() = (params as? ChangeTagData.Change)?.preview?.run {
        with(binding.previewChangeRecordTag) {
            itemName = name
            itemColor = color
            icon?.let {
                itemIconVisible = true
                itemIcon = it.toViewData()
            } ?: run {
                itemIconVisible = false
            }
        }
    }

    private fun updatePreview(item: CategoryViewData.Record) {
        with(binding.previewChangeRecordTag) {
            itemName = item.name
            itemColor = item.color
            item.icon?.let {
                itemIconVisible = true
                itemIcon = it
            } ?: run {
                itemIconVisible = false
            }
        }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: ChangeRecordTagParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data.data)
        }
    }
}