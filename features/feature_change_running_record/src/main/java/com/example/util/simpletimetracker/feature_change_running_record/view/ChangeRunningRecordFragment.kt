package com.example.util.simpletimetracker.feature_change_running_record.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
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
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.core.utils.setChooserColor
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAddAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_change_running_record.viewData.ChangeRunningRecordViewData
import com.example.util.simpletimetracker.feature_change_running_record.viewModel.ChangeRunningRecordViewModel
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.feature_views.extension.rotateDown
import com.example.util.simpletimetracker.feature_views.extension.rotateUp
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_change_running_record.databinding.ChangeRunningRecordFragmentBinding as Binding

@AndroidEntryPoint
class ChangeRunningRecordFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<ChangeRunningRecordViewModel>

    @Inject
    lateinit var router: Router

    private val viewModel: ChangeRunningRecordViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val typesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onTypeClick)
        )
    }
    private val categoriesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createCategoryAdapterDelegate(
                onClick = viewModel::onCategoryClick,
                onLongClickWithTransition = viewModel::onCategoryLongClick,
            ),
            createCategoryAddAdapterDelegate { viewModel.onAddCategoryClick() },
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createHintAdapterDelegate(),
            createEmptyAdapterDelegate()
        )
    }
    private val params: ChangeRunningRecordParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = ChangeRunningRecordParams()
    )

    override fun initUi(): Unit = with(binding) {
        postponeEnterTransition()

        setPreview()

        setSharedTransitions(
            transitionName = TransitionNames.RECORD_RUNNING + params.id,
            sharedView = previewChangeRunningRecord,
        )

        rvChangeRunningRecordType.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = typesAdapter
        }

        rvChangeRunningRecordCategories.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = categoriesAdapter
        }

        root.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }
    }

    override fun initUx() = with(binding) {
        etChangeRunningRecordComment.doAfterTextChanged { viewModel.onCommentChange(it.toString()) }
        fieldChangeRunningRecordType.setOnClick(viewModel::onTypeChooserClick)
        fieldChangeRunningRecordCategory.setOnClick(viewModel::onCategoryChooserClick)
        fieldChangeRunningRecordTimeStarted.setOnClick(viewModel::onTimeStartedClick)
        btnChangeRunningRecordSave.setOnClick(viewModel::onSaveClick)
        btnChangeRunningRecordDelete.setOnClick(viewModel::onDeleteClick)
        btnChangeRunningRecordTimeStartedAdjust.setOnClick(viewModel::onAdjustTimeStartedClick)
        containerChangeRunningRecordTimeAdjust.listener = viewModel::onAdjustTimeItemClick
    }

    override fun initViewModel() = with(binding) {
        with(viewModel) {
            extra = params
            record.observeOnce(viewLifecycleOwner, ::updateUi)
            record.observe(::updatePreview)
            types.observe(typesAdapter::replace)
            categories.observe(categoriesAdapter::replace)
            deleteButtonEnabled.observe(btnChangeRunningRecordDelete::setEnabled)
            saveButtonEnabled.observe(btnChangeRunningRecordSave::setEnabled)
            flipTypesChooser.observe { opened ->
                rvChangeRunningRecordType.visible = opened
                fieldChangeRunningRecordType.setChooserColor(opened)
                arrowChangeRunningRecordType.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            flipCategoryChooser.observe { opened ->
                rvChangeRunningRecordCategories.visible = opened
                fieldChangeRunningRecordCategory.setChooserColor(opened)
                arrowChangeRunningRecordCategory.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
            keyboardVisibility.observe { visible ->
                if (visible) showKeyboard(etChangeRunningRecordComment) else hideKeyboard()
            }
            timeAdjustmentState.observe { isVisible ->
                containerChangeRunningRecordTimeAdjust.visible = isVisible
                btnChangeRunningRecordTimeStartedAdjust.setChooserColor(isVisible)
            }
            timeAdjustmentItems.observe(containerChangeRunningRecordTimeAdjust.adapter::replace)
            message.observe(::showMessage)
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

    private fun updateUi(item: ChangeRunningRecordViewData) = with(binding) {
        etChangeRunningRecordComment.setText(item.comment)
        etChangeRunningRecordComment.setSelection(item.comment.length)
    }

    private fun setPreview() = params.preview?.run {
        ChangeRunningRecordViewData(
            name = name,
            tagName = tagName,
            timeStarted = timeStarted,
            dateTimeStarted = "",
            duration = duration,
            goalTime = goalTime,
            iconId = iconId.toViewData(),
            color = color,
            comment = comment
        ).let(::updatePreview)
    }

    private fun updatePreview(item: ChangeRunningRecordViewData) = with(binding) {
        with(previewChangeRunningRecord) {
            itemName = item.name
            itemTagName = item.tagName
            itemIcon = item.iconId
            itemColor = item.color
            itemTimeStarted = item.timeStarted
            itemTimer = item.duration
            itemGoalTime = item.goalTime
            itemComment = item.comment
        }
        tvChangeRunningRecordTimeStarted.text = item.dateTimeStarted
    }

    private fun showMessage(message: SnackBarParams?) {
        if (message != null) {
            router.show(message, binding.btnChangeRunningRecordSave)
            viewModel.onMessageShown()
        }
    }

    companion object {
        private const val ARGS_PARAMS = "args_running_record_params"

        fun createBundle(data: ChangeRunningRecordParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}