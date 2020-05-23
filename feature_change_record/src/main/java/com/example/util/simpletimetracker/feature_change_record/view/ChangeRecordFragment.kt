package com.example.util.simpletimetracker.feature_change_record.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.extension.*
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordAdapter
import com.example.util.simpletimetracker.feature_change_record.di.ChangeRecordComponentProvider
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordViewModel
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordViewModelFactory
import com.example.util.simpletimetracker.feature_dialogs.DateTimeListener
import com.example.util.simpletimetracker.navigation.params.ChangeRecordParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.change_record_fragment.*

class ChangeRecordFragment : Fragment(), DateTimeListener {

    private val viewModel: ChangeRecordViewModel by viewModels(
        factoryProducer = {
            ChangeRecordViewModelFactory(
                id = arguments?.getLong(ARGS_RECORD_ID).orZero(),
                daysFromToday = arguments?.getInt(ARGS_DAYS_FROM_TODAY).orZero()
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

        viewModel.deleteIconVisibility
            .observeOnce(viewLifecycleOwner, btnChangeRecordDelete::visible::set)
        viewModel.record
            .observe(viewLifecycleOwner, ::updatePreview)
        viewModel.types
            .observe(viewLifecycleOwner, typesAdapter::replace)
        viewModel.saveButtonEnabled
            .observe(viewLifecycleOwner, btnChangeRecordSave::setEnabled)
        viewModel.deleteButtonEnabled
            .observe(viewLifecycleOwner, btnChangeRecordDelete::setEnabled)
        viewModel.flipTypesChooser
            .observe(viewLifecycleOwner) { opened ->
                rvChangeRecordType.visible = opened
                arrowChangeRecordType.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }

        fieldChangeRecordType.setOnClick(viewModel::onTypeChooserClick)
        fieldChangeRecordTimeStarted.setOnClick(viewModel::onTimeStartedClick)
        fieldChangeRecordTimeEnded.setOnClick(viewModel::onTimeEndedClick)
        btnChangeRecordSave.setOnClick(viewModel::onSaveClick)
        btnChangeRecordDelete.setOnClick(viewModel::onDeleteClick)
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
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
        tvChangeRecordTimeStarted.text = item.dateTimeStarted
        tvChangeRecordTimeEnded.text = item.dateTimeFinished
    }

    companion object {
        private const val ARGS_RECORD_ID = "args_record_id"
        private const val ARGS_DAYS_FROM_TODAY = "args_days_from_today"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is ChangeRecordParams -> {
                    putLong(ARGS_RECORD_ID, data.id)
                    putInt(ARGS_DAYS_FROM_TODAY, data.daysFromToday)
                }
            }
        }
    }
}