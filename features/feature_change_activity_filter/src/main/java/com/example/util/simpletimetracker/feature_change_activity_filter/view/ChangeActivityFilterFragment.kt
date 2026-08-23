package com.example.util.simpletimetracker.feature_change_activity_filter.view

import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.feature_dialogs.api.ColorSelectionDialogListener
import com.example.util.simpletimetracker.core.extension.addOnBackPressedListener
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setSharedTransitions
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.core.view.ViewChooserStateDelegate
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorFavouriteAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorPaletteAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_change_activity_filter.R
import com.example.util.simpletimetracker.feature_change_activity_filter.viewData.ChangeActivityFilterChooserState.Closed
import com.example.util.simpletimetracker.feature_change_activity_filter.viewData.ChangeActivityFilterChooserState.Color
import com.example.util.simpletimetracker.feature_change_activity_filter.viewData.ChangeActivityFilterChooserState.Type
import com.example.util.simpletimetracker.feature_change_activity_filter.viewData.ChangeActivityFilterTypesViewData
import com.example.util.simpletimetracker.feature_change_activity_filter.viewModel.ChangeActivityFilterViewModel
import com.example.util.simpletimetracker.feature_views.extension.animateColor
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.ChangeActivityFilterParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_change_activity_filter.databinding.ChangeActivityFilterFragmentBinding as Binding

@AndroidEntryPoint
class ChangeActivityFilterFragment :
    BaseFragment<Binding>(),
    ColorSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.root }

    private val viewModel: ChangeActivityFilterViewModel by viewModels()

    private val colorsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createColorAdapterDelegate(viewModel::onColorClick),
            createColorPaletteAdapterDelegate(viewModel::onColorPaletteClick),
            createColorFavouriteAdapterDelegate(viewModel::onColorFavouriteClick),
            createHintAdapterDelegate(),
        )
    }
    private val viewDataAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onTypeClick),
            createCategoryAdapterDelegate(viewModel::onCategoryClick),
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createEmptyAdapterDelegate(),
        )
    }
    private var typeColorAnimator: ValueAnimator? = null
    private val colorPreviewGradient = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.LEFT_RIGHT
    }

    private val params: ChangeActivityFilterParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = ChangeActivityFilterParams.New,
    )

    override fun initUi(): Unit = with(binding) {
        setPreview()

        setSharedTransitions(
            additionalCondition = { params !is ChangeActivityFilterParams.New },
            transitionName = (params as? ChangeActivityFilterParams.Change)?.transitionName.orEmpty(),
            sharedView = previewChangeActivityFilter,
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

    override fun initUx(): Unit = with(binding) {
        etChangeActivityFilterName.doAfterTextChanged { viewModel.onNameChange(it.toString()) }
        fieldChangeActivityFilterColor.setOnClick(viewModel::onColorChooserClick)
        fieldChangeActivityFilterType.setOnClick(viewModel::onTypeChooserClick)
        btnChangeActivityFilterSave.setOnClick(viewModel::onSaveClick)
        btnChangeActivityFilterDelete.setOnClick(viewModel::onDeleteClick)
        buttonsChangeActivityFilterType.listener = viewModel::onFilterTypeClick
        addOnBackPressedListener(action = viewModel::onBackPressed)
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
            filterTypeViewData.observe(buttonsChangeActivityFilterType::replace)
            viewData.observe(::updateTypes)
            chooserState.observe(::updateChooserState)
            keyboardVisibility.observe { visible ->
                if (visible) showKeyboard(etChangeActivityFilterName) else hideKeyboard()
            }
        }
    }

    override fun onDestroy() {
        typeColorAnimator?.cancel()
        super.onDestroy()
    }

    override fun onColorSelected(tag: String, colorInt: Int) {
        viewModel.onCustomColorSelected(tag = tag, colorInt = colorInt)
    }

    private fun updateUi(item: ActivityFilterViewData) = with(binding) {
        etChangeActivityFilterName.setText(item.name)
        etChangeActivityFilterName.setSelection(item.name.length)
    }

    private fun setPreview() {
        with(binding.previewChangeActivityFilter) {
            (params as? ChangeActivityFilterParams.Change)?.preview?.let {
                itemName = it.name
                itemColor = it.color

                updateColorPreview(it.color)
                binding.layoutChangeActivityFilterTypePreview.setCardBackgroundColor(it.color)
            }
        }
    }

    private fun updatePreview(item: ActivityFilterViewData) {
        with(binding.previewChangeActivityFilter) {
            itemName = item.name

            typeColorAnimator?.cancel()
            typeColorAnimator = animateColor(
                from = itemColor,
                to = item.color,
                doOnUpdate = { value ->
                    itemColor = value
                    updateColorPreview(value)
                },
            )
            with(binding) {
                layoutChangeActivityFilterTypePreview.setCardBackgroundColor(item.color)
            }
        }
    }

    private fun updateChooserState(
        state: ViewChooserStateDelegate.States,
    ) = with(binding) {
        ViewChooserStateDelegate.updateChooser<Color>(
            state = state,
            chooserData = rvChangeActivityFilterColor,
            chooserView = fieldChangeActivityFilterColor,
            chooserArrow = arrowChangeActivityFilterColor,
        )
        ViewChooserStateDelegate.updateChooser<Type>(
            state = state,
            chooserData = containerChangeActivityFilterActivities,
            chooserView = fieldChangeActivityFilterType,
            chooserArrow = arrowChangeActivityFilterType,
        )

        val isClosed = state.current is Closed
        inputChangeActivityFilterName.isVisible = isClosed
        btnChangeActivityFilterDelete.isVisible =
            viewModel.deleteIconVisibility.value.orFalse() && isClosed
        dividerChangeActivityFilterBottom.isVisible = !isClosed

        // Chooser fields
        fieldChangeActivityFilterColor.isVisible = isClosed || state.current is Color
        fieldChangeActivityFilterType.isVisible = isClosed || state.current is Type

        // Chooser size
        val sizeDefault = resources.getDimensionPixelSize(R.dimen.input_field_height)
        val sizeBig = resources.getDimensionPixelSize(R.dimen.input_field_height_big)
        val colorSize = if (state.current is Color) sizeDefault else sizeBig
        fieldChangeActivityFilterColor.updateLayoutParams { height = colorSize }
        val activitiesSize = if (state.current is Type) sizeDefault else sizeBig
        fieldChangeActivityFilterType.updateLayoutParams { height = activitiesSize }
    }

    private fun updateTypes(
        data: ChangeActivityFilterTypesViewData,
    ) = with(binding) {
        viewDataAdapter.replace(data.viewData)
        tvChangeActivityFilterType.text = data.typeHint
        layoutChangeActivityFilterTypePreview.isVisible = data.selectedCount > 0
        tvChangeActivityFilterTypePreview.text = data.selectedCount.toString()
    }

    private fun updateColorPreview(@ColorInt color: Int) = with(binding) {
        colorPreviewGradient.colors = intArrayOf(android.graphics.Color.TRANSPARENT, color)
        layoutChangeActivityFilterColorPreview.setCardBackgroundColor(color)
        viewChangeActivityFilterColorPreviewLong.background = colorPreviewGradient
    }

    companion object {
        fun createBundle(data: ChangeActivityFilterParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}