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
import com.example.util.simpletimetracker.core.utils.setChooserColor
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
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
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createEmptyAdapterDelegate()
        )
    }
    private val extra: ChangeRecordParams by lazy {
        arguments?.getParcelable<ChangeRecordParams>(ARGS_PARAMS) ?: ChangeRecordParams.New()
    }

    override fun initUi(): Unit = with(binding) {
        setPreview()

        val transitionName: String = when (extra) {
            is ChangeRecordParams.Tracked -> (extra as? ChangeRecordParams.Tracked)?.transitionName.orEmpty()
            is ChangeRecordParams.Untracked -> (extra as? ChangeRecordParams.Untracked)?.transitionName.orEmpty()
            else -> ""
        }
        setSharedTransitions(
            additionalCondition = { extra !is ChangeRecordParams.New },
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
    }

    override fun initUx() = with(binding) {
        etChangeRecordComment.doAfterTextChanged { viewModel.onCommentChange(it.toString()) }
        fieldChangeRecordType.setOnClick(viewModel::onTypeChooserClick)
        fieldChangeRecordCategory.setOnClick(viewModel::onCategoryChooserClick)
        fieldChangeRecordTimeStarted.setOnClick(viewModel::onTimeStartedClick)
        fieldChangeRecordTimeEnded.setOnClick(viewModel::onTimeEndedClick)
        btnChangeRecordSave.setOnClick(viewModel::onSaveClick)
        btnChangeRecordDelete.setOnClick {
            viewModel.onDeleteClick()
            removeRecordViewModel.onDeleteClick(
                (extra as? ChangeRecordParams.Tracked)?.from
            )
        }
    }

    override fun initViewModel() = with(binding) {
        with(viewModel) {
            extra = this@ChangeRecordFragment.extra
            record.observeOnce(viewLifecycleOwner, ::updateUi)
            record.observe(::updatePreview)
            types.observe(typesAdapter::replace)
            categories.observe(categoriesAdapter::replace)
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
            keyboardVisibility.observe { visible ->
                if (visible) showKeyboard(etChangeRecordComment) else hideKeyboard()
            }
        }
        with(removeRecordViewModel) {
            prepare((extra as? ChangeRecordParams.Tracked)?.id.orZero())
            deleteButtonEnabled.observe(btnChangeRecordDelete::setEnabled)
            deleteIconVisibility.observe(btnChangeRecordDelete::visible::set)
        }
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    private fun updateUi(item: ChangeRecordViewData) = with(binding) {
        etChangeRecordComment.setText(item.comment)
        etChangeRecordComment.setSelection(item.comment.length)
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