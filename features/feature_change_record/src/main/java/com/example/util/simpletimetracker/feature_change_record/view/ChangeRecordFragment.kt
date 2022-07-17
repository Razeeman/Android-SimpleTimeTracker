package com.example.util.simpletimetracker.feature_change_record.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setSharedTransitions
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.sharedViewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.core.utils.setChooserColor
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAddAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.adapter.createChangeRecordCommentAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import com.example.util.simpletimetracker.feature_change_record.viewData.TimeAdjustmentState
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordViewModel
import com.example.util.simpletimetracker.feature_views.extension.rotateDown
import com.example.util.simpletimetracker.feature_views.extension.rotateUp
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordsFromScreen
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordFragmentBinding as Binding

@AndroidEntryPoint
class ChangeRecordFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<ChangeRecordViewModel>

    @Inject
    lateinit var removeRecordViewModelFactory: BaseViewModelFactory<RemoveRecordViewModel>

    private val viewModel: ChangeRecordViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val removeRecordViewModel: RemoveRecordViewModel by viewModels(
        ownerProducer = { activity as AppCompatActivity },
        factoryProducer = { removeRecordViewModelFactory }
    )
    private val typesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createEmptyAdapterDelegate(),
            createRecordTypeAdapterDelegate(viewModel::onTypeClick)
        )
    }
    private val categoriesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createCategoryAdapterDelegate(viewModel::onCategoryClick),
            createCategoryAddAdapterDelegate { viewModel.onAddCategoryClick() },
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createHintAdapterDelegate(),
            createEmptyAdapterDelegate()
        )
    }
    private val commentsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createChangeRecordCommentAdapterDelegate(viewModel::onCommentClick),
        )
    }
    private val extra: ChangeRecordParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = ChangeRecordParams.New()
    )

    override fun initUi(): Unit = with(binding) {
        setPreview()

        val transitionName: String = when (extra) {
            is ChangeRecordParams.Tracked -> (extra as? ChangeRecordParams.Tracked)?.transitionName.orEmpty()
            is ChangeRecordParams.Untracked -> (extra as? ChangeRecordParams.Untracked)?.transitionName.orEmpty()
            else -> ""
        }
        setSharedTransitions(
            additionalCondition = { transitionName.isNotEmpty() },
            transitionName = transitionName,
            sharedView = previewChangeRecord,
        )

        rvChangeRecordType.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = typesAdapter
        }

        rvChangeRecordCategories.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = categoriesAdapter
        }

        rvChangeRecordLastComments.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = commentsAdapter
        }
    }

    override fun initUx() = with(binding) {
        etChangeRecordComment.doAfterTextChanged { viewModel.onCommentChange(it.toString()) }
        fieldChangeRecordType.setOnClick(viewModel::onTypeChooserClick)
        fieldChangeRecordCategory.setOnClick(viewModel::onCategoryChooserClick)
        fieldChangeRecordTimeStarted.setOnClick(viewModel::onTimeStartedClick)
        fieldChangeRecordTimeEnded.setOnClick(viewModel::onTimeEndedClick)
        fieldChangeRecordLastComments.setOnClick(viewModel::onLastCommentsChooserClick)
        btnChangeRecordSave.setOnClick(viewModel::onSaveClick)
        btnChangeRecordDelete.setOnClick {
            viewModel.onDeleteClick()
            removeRecordViewModel.onDeleteClick(
                (extra as? ChangeRecordParams.Tracked)?.from
            )
        }
        btnChangeRecordTimeStartedAdjust.setOnClick(viewModel::onAdjustTimeStartedClick)
        btnChangeRecordTimeEndedAdjust.setOnClick(viewModel::onAdjustTimeEndedClick)
        containerChangeRecordTimeAdjust.listener = viewModel::onAdjustTimeItemClick
    }

    override fun initViewModel() = with(binding) {
        with(viewModel) {
            extra = this@ChangeRecordFragment.extra
            record.observeOnce(viewLifecycleOwner) { updateUi(it.comment) }
            record.observe(::updatePreview)
            types.observe(typesAdapter::replace)
            categories.observe(categoriesAdapter::replace)
            lastComments.observe { data ->
                fieldChangeRecordLastComments.visible = data.isNotEmpty()
                commentsAdapter.replace(data)
            }
            comment.observe(::updateUi)
            saveButtonEnabled.observe(btnChangeRecordSave::setEnabled)
            flipTypesChooser.observe { opened ->
                rvChangeRecordType.visible = opened
                fieldChangeRecordType.setChooserColor(opened)
                arrowChangeRecordType.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            flipCategoryChooser.observe { opened ->
                rvChangeRecordCategories.visible = opened
                fieldChangeRecordCategory.setChooserColor(opened)
                arrowChangeRecordCategory.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            flipLastCommentsChooser.observe { opened ->
                rvChangeRecordLastComments.visible = opened
                fieldChangeRecordLastComments.setChooserColor(opened)
                arrowChangeRecordLastComment.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            keyboardVisibility.observe { visible ->
                if (visible) showKeyboard(etChangeRecordComment) else hideKeyboard()
            }
            timeAdjustmentState.observe { state ->
                containerChangeRecordTimeAdjust.visible = state != TimeAdjustmentState.HIDDEN
                btnChangeRecordTimeStartedAdjust.setChooserColor(state == TimeAdjustmentState.TIME_STARTED)
                btnChangeRecordTimeEndedAdjust.setChooserColor(state == TimeAdjustmentState.TIME_ENDED)
            }
            timeAdjustmentItems.observe(containerChangeRecordTimeAdjust.adapter::replace)
        }
        with(removeRecordViewModel) {
            prepare((extra as? ChangeRecordParams.Tracked)?.id.orZero())
            deleteButtonEnabled.observe(btnChangeRecordDelete::setEnabled)
            deleteIconVisibility.observe(btnChangeRecordDelete::visible::set)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    private fun updateUi(comment: String) = with(binding) {
        etChangeRecordComment.setText(comment)
        etChangeRecordComment.setSelection(comment.length)
    }

    private fun setPreview() = when (extra) {
        is ChangeRecordParams.Tracked -> (extra as? ChangeRecordParams.Tracked)?.preview
        is ChangeRecordParams.Untracked -> (extra as? ChangeRecordParams.Untracked)?.preview
        else -> null
    }?.let { preview ->
        ChangeRecordViewData(
            name = preview.name,
            tagName = preview.tagName,
            timeStarted = preview.timeStarted,
            timeFinished = preview.timeFinished,
            dateTimeStarted = "",
            dateTimeFinished = "",
            duration = preview.duration,
            iconId = preview.iconId.toViewData(),
            color = preview.color,
            comment = preview.comment
        ).let(::updatePreview)
    }

    private fun updatePreview(item: ChangeRecordViewData) = with(binding) {
        with(previewChangeRecord) {
            itemName = item.name
            itemTagName = item.tagName
            itemIcon = item.iconId
            itemColor = item.color
            itemTimeStarted = item.timeStarted
            itemTimeEnded = item.timeFinished
            itemDuration = item.duration
            itemComment = item.comment
        }
        tvChangeRecordTimeStarted.text = item.dateTimeStarted
        tvChangeRecordTimeEnded.text = item.dateTimeFinished
    }

    companion object {
        private const val ARGS_PARAMS = "args_change_record_params"

        fun createBundle(data: ChangeRecordsFromScreen): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data.params)
        }
    }
}