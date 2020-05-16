package com.example.util.simpletimetracker.feature_change_record.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.rotateDown
import com.example.util.simpletimetracker.core.extension.rotateUp
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordAdapter
import com.example.util.simpletimetracker.feature_change_record.di.ChangeRecordComponentProvider
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordViewModel
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordViewModelFactory
import com.example.util.simpletimetracker.navigation.params.ChangeRecordParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.change_record_fragment.*

class ChangeRecordFragment : Fragment() {

    private val viewModel: ChangeRecordViewModel by viewModels(
        factoryProducer = {
            ChangeRecordViewModelFactory(
                arguments?.getLong(ARGS_RECORD_ID).orZero()
            )
        }
    )
    private val typesAdapter: ChangeRecordAdapter by lazy {
        ChangeRecordAdapter(viewModel::onTypeClick)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.change_record_fragment,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity?.application as ChangeRecordComponentProvider)
            .changeRecordComponent?.inject(viewModel)

        rvChangeRecordType.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = typesAdapter
        }

        viewModel.deleteIconVisibility.observeOnce(viewLifecycleOwner) {
            btnChangeRecordDelete.visible = it
        }

        viewModel.record.observe(viewLifecycleOwner) {
            updatePreview(it)
        }

        viewModel.types.observe(viewLifecycleOwner) {
            typesAdapter.replace(it)
        }

        viewModel.flipTypesChooser.observe(viewLifecycleOwner) { opened ->
            rvChangeRecordType.visible = opened
            arrowChangeRecordType.apply {
                if (opened) rotateDown() else rotateUp()
            }
        }

        viewModel.saveButtonEnabled.observe(viewLifecycleOwner) { enabled ->
            btnChangeRecordSave.isEnabled = enabled
        }

        fieldChangeRecordType.setOnClickListener {
            viewModel.onTypeChooserClick()
        }

        btnChangeRecordDelete.setOnClickListener {
            it.isEnabled = false
            viewModel.onDeleteClick()
        }

        btnChangeRecordSave.setOnClickListener {
            viewModel.onSaveClick()
        }
    }

    private fun updatePreview(item: ChangeRecordViewData) {
        with(previewChangeRecord) {
            name = item.name
            icon = item.iconId
            color = item.color
            timeStarted = item.timeStarted
            timeEnded = item.timeFinished
            duration = item.duration
        }
    }

    companion object {
        private const val ARGS_RECORD_ID = "record_id"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is ChangeRecordParams -> putLong(ARGS_RECORD_ID, data.id)
            }
        }
    }
}