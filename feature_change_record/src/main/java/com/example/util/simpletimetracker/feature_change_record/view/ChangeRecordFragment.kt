package com.example.util.simpletimetracker.feature_change_record.view

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.base.BaseFragment
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

class ChangeRecordFragment : BaseFragment(), DateTimeListener {

    override val layoutId: Int = R.layout.change_record_fragment

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

    override fun initDi() {
        val component = (activity?.application as ChangeRecordComponentProvider)
            .changeRecordComponent

        component?.inject(viewModel)
    }

    override fun initUi() {
        rvChangeRecordType.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = typesAdapter
        }
    }

    override fun initUx() {
        fieldChangeRecordType.setOnClick(viewModel::onTypeChooserClick)
        fieldChangeRecordTimeStarted.setOnClick(viewModel::onTimeStartedClick)
        fieldChangeRecordTimeEnded.setOnClick(viewModel::onTimeEndedClick)
        btnChangeRecordSave.setOnClick(viewModel::onSaveClick)
        btnChangeRecordDelete.setOnClick(viewModel::onDeleteClick)
    }

    override fun initViewModel() = with(viewModel) {
        deleteIconVisibility.observeOnce(viewLifecycleOwner, btnChangeRecordDelete::visible::set)
        record.observe(viewLifecycleOwner, ::updatePreview)
        types.observe(viewLifecycleOwner, typesAdapter::replace)
        saveButtonEnabled.observe(viewLifecycleOwner, btnChangeRecordSave::setEnabled)
        deleteButtonEnabled.observe(viewLifecycleOwner, btnChangeRecordDelete::setEnabled)
        flipTypesChooser.observe(viewLifecycleOwner) { opened ->
            rvChangeRecordType.visible = opened
            arrowChangeRecordType.apply {
                if (opened) rotateDown() else rotateUp()
            }
        }
        Unit
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