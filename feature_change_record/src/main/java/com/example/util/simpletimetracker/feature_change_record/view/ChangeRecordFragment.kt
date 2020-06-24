package com.example.util.simpletimetracker.feature_change_record.view

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.transition.TransitionInflater
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.rotateDown
import com.example.util.simpletimetracker.core.extension.rotateUp
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.core.utils.BuildVersions
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordAdapter
import com.example.util.simpletimetracker.feature_change_record.di.ChangeRecordComponentProvider
import com.example.util.simpletimetracker.feature_change_record.extra.ChangeRecordExtra
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordViewModel
import com.example.util.simpletimetracker.navigation.params.ChangeRecordParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.change_record_fragment.*
import javax.inject.Inject

class ChangeRecordFragment : BaseFragment(R.layout.change_record_fragment),
    DateTimeDialogListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: ChangeRecordViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val typesAdapter: ChangeRecordAdapter by lazy {
        ChangeRecordAdapter(viewModel::onTypeClick)
    }
    private val recordId: Long by lazy { arguments?.getLong(ARGS_RECORD_ID).orZero() }

    override fun initDi() {
        (activity?.application as ChangeRecordComponentProvider)
            .changeRecordComponent
            ?.inject(this)
    }

    override fun initUi() {
        if (BuildVersions.isLollipopOrHigher()) {
            sharedElementEnterTransition = TransitionInflater.from(context)
                .inflateTransition(android.R.transition.move)
        }

        ViewCompat.setTransitionName(
            previewChangeRecord,
            TransitionNames.RECORD + recordId
        )

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

    override fun initViewModel(): Unit = with(viewModel) {
        extra = ChangeRecordExtra(
            id = recordId,
            daysFromToday = arguments?.getInt(ARGS_DAYS_FROM_TODAY).orZero()
        )
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
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    private fun updatePreview(item: ChangeRecordViewData) {
        with(previewChangeRecord) {
            itemName = item.name
            itemIcon = item.iconId
            itemColor = item.color
            itemTimeStarted = item.timeStarted
            itemTimeEnded = item.timeFinished
            itemDuration = item.duration
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