package com.example.util.simpletimetracker.feature_change_record_type.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.extension.*
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.adapter.ChangeRecordTypeAdapter
import com.example.util.simpletimetracker.feature_change_record_type.di.ChangeRecordTypeComponentProvider
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewModel.ChangeRecordTypeViewModel
import com.example.util.simpletimetracker.feature_change_record_type.viewModel.ChangeRecordTypeViewModelFactory
import com.example.util.simpletimetracker.navigation.params.ChangeRecordTypeParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.change_record_type_fragment.*

class ChangeRecordTypeFragment : Fragment() {

    private val viewModel: ChangeRecordTypeViewModel by viewModels(
        factoryProducer = {
            ChangeRecordTypeViewModelFactory(
                arguments?.getLong(ARGS_RECORD_ID).orZero()
            )
        }
    )
    private val colorsAdapter: ChangeRecordTypeAdapter by lazy {
        ChangeRecordTypeAdapter(viewModel::onColorClick, viewModel::onIconClick)
    }
    private val iconsAdapter: ChangeRecordTypeAdapter by lazy {
        ChangeRecordTypeAdapter(viewModel::onColorClick, viewModel::onIconClick)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.change_record_type_fragment,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity?.application as ChangeRecordTypeComponentProvider)
            .changeRecordTypeComponent?.inject(viewModel)

        rvChangeRecordTypeColor.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = colorsAdapter
        }

        rvChangeRecordTypeIcon.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = iconsAdapter
        }

        viewModel.deleteIconVisibility
            .observeOnce(viewLifecycleOwner, btnChangeRecordTypeDelete::visible::set)
        viewModel.saveButtonEnabled
            .observe(viewLifecycleOwner, btnChangeRecordTypeSave::setEnabled)
        viewModel.deleteButtonEnabled
            .observe(viewLifecycleOwner, btnChangeRecordTypeDelete::setEnabled)
        viewModel.recordType
            .observeOnce(viewLifecycleOwner, ::updateUi)
        viewModel.recordType
            .observe(viewLifecycleOwner, ::updatePreview)
        viewModel.colors
            .observe(viewLifecycleOwner, colorsAdapter::replace)
        viewModel.icons
            .observe(viewLifecycleOwner, iconsAdapter::replace)
        viewModel.flipColorChooser
            .observe(viewLifecycleOwner) { opened ->
                rvChangeRecordTypeColor.visible = opened
                arrowChangeRecordTypeColor.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
        viewModel.flipIconChooser
            .observe(viewLifecycleOwner) { opened ->
                rvChangeRecordTypeIcon.visible = opened
                arrowChangeRecordTypeIcon.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
        viewModel.keyboardVisibility
            .observe(viewLifecycleOwner) { visible ->
                if (visible) showKeyboard(etChangeRecordTypeName) else hideKeyboard()
            }

        etChangeRecordTypeName.doAfterTextChanged {
            viewModel.onNameChange(it.toString())
        }

        fieldChangeRecordTypeColor.setOnClick(viewModel::onColorChooserClick)
        fieldChangeRecordTypeIcon.setOnClick(viewModel::onIconChooserClick)
        btnChangeRecordTypeSave.setOnClick(viewModel::onSaveClick)
        btnChangeRecordTypeDelete.setOnClick(viewModel::onDeleteClick)
    }

    private fun updateUi(item: ChangeRecordTypeViewData) {
        etChangeRecordTypeName.setText(item.name)
        etChangeRecordTypeName.setSelection(item.name.length)
    }

    private fun updatePreview(item: ChangeRecordTypeViewData) {
        with(previewChangeRecordType) {
            name = item.name
            icon = item.icon
            color = item.color
        }
    }

    companion object {
        private const val ARGS_RECORD_ID = "record_id"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is ChangeRecordTypeParams -> putLong(ARGS_RECORD_ID, data.id)
            }
        }
    }
}