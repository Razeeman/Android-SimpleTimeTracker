package com.example.util.simpletimetracker.feature_change_running_record.view

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.transition.TransitionInflater
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.rotateDown
import com.example.util.simpletimetracker.core.extension.rotateUp
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.core.utils.BuildVersions
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.feature_change_running_record.R
import com.example.util.simpletimetracker.feature_change_running_record.di.ChangeRunningRecordComponentProvider
import com.example.util.simpletimetracker.feature_change_running_record.viewData.ChangeRunningRecordViewData
import com.example.util.simpletimetracker.feature_change_running_record.viewModel.ChangeRunningRecordViewModel
import com.example.util.simpletimetracker.navigation.params.ChangeRunningRecordParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.change_running_record_fragment.arrowChangeRunningRecordType
import kotlinx.android.synthetic.main.change_running_record_fragment.btnChangeRunningRecordDelete
import kotlinx.android.synthetic.main.change_running_record_fragment.btnChangeRunningRecordSave
import kotlinx.android.synthetic.main.change_running_record_fragment.etChangeRunningRecordComment
import kotlinx.android.synthetic.main.change_running_record_fragment.fieldChangeRunningRecordTimeStarted
import kotlinx.android.synthetic.main.change_running_record_fragment.fieldChangeRunningRecordType
import kotlinx.android.synthetic.main.change_running_record_fragment.previewChangeRunningRecord
import kotlinx.android.synthetic.main.change_running_record_fragment.rvChangeRunningRecordType
import kotlinx.android.synthetic.main.change_running_record_fragment.tvChangeRunningRecordTimeStarted
import javax.inject.Inject

class ChangeRunningRecordFragment : BaseFragment(R.layout.change_running_record_fragment),
    DateTimeDialogListener {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<ChangeRunningRecordViewModel>

    private val viewModel: ChangeRunningRecordViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val typesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onTypeClick)
        )
    }
    private val params: ChangeRunningRecordParams by lazy {
        arguments?.getParcelable(ARGS_PARAMS) ?: ChangeRunningRecordParams()
    }

    override fun initDi() {
        (activity?.application as ChangeRunningRecordComponentProvider)
            .changeRunningRecordComponent
            ?.inject(this)
    }

    override fun initUi() {
        setPreview()

        if (BuildVersions.isLollipopOrHigher()) {
            sharedElementEnterTransition = TransitionInflater.from(context)
                .inflateTransition(android.R.transition.move)
        }

        ViewCompat.setTransitionName(
            previewChangeRunningRecord,
            TransitionNames.RECORD_RUNNING + params.id
        )

        rvChangeRunningRecordType.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = typesAdapter
        }
    }

    override fun initUx() {
        etChangeRunningRecordComment.doAfterTextChanged { viewModel.onCommentChange(it.toString()) }
        fieldChangeRunningRecordType.setOnClick(viewModel::onTypeChooserClick)
        fieldChangeRunningRecordTimeStarted.setOnClick(viewModel::onTimeStartedClick)
        btnChangeRunningRecordSave.setOnClick(viewModel::onSaveClick)
        btnChangeRunningRecordDelete.setOnClick(viewModel::onDeleteClick)
    }

    override fun initViewModel() {
        with(viewModel) {
            extra = params
            record.observeOnce(viewLifecycleOwner, ::updateUi)
            record.observe(viewLifecycleOwner, ::updatePreview)
            types.observe(viewLifecycleOwner, typesAdapter::replace)
            deleteButtonEnabled.observe(
                viewLifecycleOwner, btnChangeRunningRecordDelete::setEnabled
            )
            saveButtonEnabled.observe(
                viewLifecycleOwner, btnChangeRunningRecordSave::setEnabled
            )
            flipTypesChooser.observe(viewLifecycleOwner) { opened ->
                rvChangeRunningRecordType.visible = opened
                arrowChangeRunningRecordType.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            keyboardVisibility.observe(viewLifecycleOwner) { visible ->
                if (visible) showKeyboard(etChangeRunningRecordComment) else hideKeyboard()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onHidden()
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    private fun updateUi(item: ChangeRunningRecordViewData) {
        etChangeRunningRecordComment.setText(item.comment)
        etChangeRunningRecordComment.setSelection(item.comment.length)
    }

    private fun setPreview() = params.preview?.run {
        ChangeRunningRecordViewData(
            name = name,
            timeStarted = timeStarted,
            dateTimeStarted = "",
            duration = duration,
            goalTime = goalTime,
            iconId = iconId,
            color = color,
            comment = comment
        ).let(::updatePreview)
    }

    private fun updatePreview(item: ChangeRunningRecordViewData) {
        with(previewChangeRunningRecord) {
            itemName = item.name
            itemIcon = item.iconId
            itemColor = item.color
            itemTimeStarted = item.timeStarted
            itemTimer = item.duration
            itemGoalTime = item.goalTime
            itemComment = item.comment
        }
        tvChangeRunningRecordTimeStarted.text = item.dateTimeStarted
    }

    companion object {
        private const val ARGS_PARAMS = "args_running_record_params"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is ChangeRunningRecordParams -> putParcelable(ARGS_PARAMS, data)
            }
        }
    }
}