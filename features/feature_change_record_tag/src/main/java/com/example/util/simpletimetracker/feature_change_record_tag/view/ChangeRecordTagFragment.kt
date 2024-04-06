package com.example.util.simpletimetracker.feature_change_record_tag.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.dialog.ColorSelectionDialogListener
import com.example.util.simpletimetracker.core.dialog.TypesSelectionDialogListener
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setSharedTransitions
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.core.view.UpdateViewChooserState
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorPaletteAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.createHintBigAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record_tag.R
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagTypeChooserState
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagTypeChooserState.State
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagTypeChooserState.State.Closed
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagTypeChooserState.State.Color
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagTypeChooserState.State.Type
import com.example.util.simpletimetracker.feature_change_record_tag.viewModel.ChangeRecordTagViewModel
import com.example.util.simpletimetracker.feature_views.extension.setCompoundDrawableWithIntrinsicBounds
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromScreen
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_change_record_tag.databinding.ChangeRecordTagFragmentBinding as Binding

@AndroidEntryPoint
class ChangeRecordTagFragment :
    BaseFragment<Binding>(),
    ColorSelectionDialogListener,
    TypesSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: ChangeRecordTagViewModel by viewModels()

    private val colorsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createColorAdapterDelegate(viewModel::onColorClick),
            createColorPaletteAdapterDelegate(viewModel::onColorPaletteClick),
        )
    }
    private val typesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onTypeClick),
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createHintBigAdapterDelegate(),
        )
    }

    private val params: ChangeTagData by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = ChangeTagData.New(),
    )

    override fun initUi(): Unit = with(binding) {
        setPreview()

        setSharedTransitions(
            additionalCondition = { params !is ChangeTagData.New },
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
        btnChangeRecordTagSelectActivity.setOnClick(viewModel::onSelectActivityClick)
        btnChangeRecordTagSave.setOnClick(viewModel::onSaveClick)
        btnChangeRecordTagDelete.setOnClick(viewModel::onDeleteClick)
    }

    override fun initViewModel(): Unit = with(binding) {
        with(viewModel) {
            extra = params
            deleteIconVisibility.observeOnce(viewLifecycleOwner, btnChangeRecordTagDelete::visible::set)
            saveButtonEnabled.observe(btnChangeRecordTagSave::setEnabled)
            deleteButtonEnabled.observe(btnChangeRecordTagDelete::setEnabled)
            iconColorSourceSelected.observe(::updateIconColorSourceSelected)
            preview.observeOnce(viewLifecycleOwner, ::updateUi)
            preview.observe(::updatePreview)
            colors.observe(colorsAdapter::replace)
            types.observe(typesAdapter::replace)
            chooserState.observe(::updateChooserState)
            keyboardVisibility.observe { visible ->
                if (visible) showKeyboard(etChangeRecordTagName) else hideKeyboard()
            }
        }
    }

    override fun onColorSelected(colorInt: Int) {
        viewModel.onCustomColorSelected(colorInt)
    }

    override fun onTypesSelected(typeIds: List<Long>, tag: String?) {
        viewModel.onTypesSelected(typeIds, tag)
    }

    private fun updateUi(item: CategoryViewData) = with(binding) {
        etChangeRecordTagName.setText(item.name)
        etChangeRecordTagName.setSelection(item.name.length)
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

    private fun updateChooserState(state: ChangeRecordTagTypeChooserState) = with(binding) {
        updateChooser<Color>(
            state = state,
            chooserData = rvChangeRecordTagColor,
            chooserView = fieldChangeRecordTagColor,
            chooserArrow = arrowChangeRecordTagColor,
        )
        updateChooser<Type>(
            state = state,
            chooserData = rvChangeRecordTagType,
            chooserView = fieldChangeRecordTagType,
            chooserArrow = arrowChangeRecordTagType,
        )

        val isClosed = state.current is Closed
        inputChangeRecordTagName.isVisible = isClosed
        btnChangeRecordTagSelectActivity.isVisible = isClosed

        // Chooser fields
        fieldChangeRecordTagColor.isVisible = isClosed || state.current is Color
        fieldChangeRecordTagType.isVisible = isClosed || state.current is Type
    }

    private fun updateIconColorSourceSelected(selected: Boolean) = with(binding) {
        val drawable = R.drawable.spinner_check_mark
            .takeIf { selected }.orZero()
        btnChangeRecordTagSelectActivity
            .setCompoundDrawableWithIntrinsicBounds(right = drawable)
    }

    private inline fun <reified T : State> updateChooser(
        state: ChangeRecordTagTypeChooserState,
        chooserData: View,
        chooserView: CardView,
        chooserArrow: View,
    ) {
        UpdateViewChooserState.updateChooser<State, T, Closed>(
            stateCurrent = state.current,
            statePrevious = state.previous,
            chooserData = chooserData,
            chooserView = chooserView,
            chooserArrow = chooserArrow,
        )
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: ChangeRecordTagFromScreen): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data.params)
        }
    }
}