package com.example.util.simpletimetracker.feature_change_shortcut.view

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.feature_dialogs.api.OnTagValueSelectedListener
import com.example.util.simpletimetracker.feature_dialogs.api.StandardDialogListener
import com.example.util.simpletimetracker.core.extension.addOnBackPressedListener
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.setSharedTransitions
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.core.view.LinearLayoutManagerWithExtraLayoutSpace
import com.example.util.simpletimetracker.core.view.ViewChooserStateDelegate
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.R
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.createEmptySpaceAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_change_shortcut.adapter.createChangeShortcutSettingActionAdapterDelegate
import com.example.util.simpletimetracker.feature_change_shortcut.databinding.FragmentChangeShortcutBinding
import com.example.util.simpletimetracker.feature_change_shortcut.viewData.ChangeShortcutChooserState.Activity
import com.example.util.simpletimetracker.feature_change_shortcut.viewData.ChangeShortcutChooserState.Closed
import com.example.util.simpletimetracker.feature_change_shortcut.viewData.ChangeShortcutChooserState.Comment
import com.example.util.simpletimetracker.feature_change_shortcut.viewData.ChangeShortcutChooserState.SettingAction
import com.example.util.simpletimetracker.feature_change_shortcut.viewData.ChangeShortcutChooserState.Tag
import com.example.util.simpletimetracker.feature_change_shortcut.viewData.ChangeShortcutViewData
import com.example.util.simpletimetracker.feature_change_shortcut.viewModel.ChangeShortcutViewModel
import com.example.util.simpletimetracker.feature_comment_selection.api.CommentSelectionViewDelegateProvider
import com.example.util.simpletimetracker.feature_views.extension.animateColor
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.ChangeShortcutParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagValueSelectionParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangeShortcutFragment :
    BaseFragment<FragmentChangeShortcutBinding>(),
    OnTagValueSelectedListener,
    StandardDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentChangeShortcutBinding =
        FragmentChangeShortcutBinding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.root }

    @Inject
    lateinit var commentDelegateProvider: CommentSelectionViewDelegateProvider

    private val viewModel: ChangeShortcutViewModel by viewModels()

    private val typesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createEmptyAdapterDelegate(),
            createRecordTypeAdapterDelegate(viewModel::onTypeClick),
        )
    }
    private val tagsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createHintAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createEmptySpaceAdapterDelegate(),
            createCategoryAdapterDelegate(
                onClick = viewModel::onTagClick,
            ),
        )
    }
    private val settingActionAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createChangeShortcutSettingActionAdapterDelegate(
                onClick = viewModel::onSettingActionClick,
            ),
        )
    }
    private val commentsDelegate by lazy {
        commentDelegateProvider.provide(viewModel)
    }
    private var typeColorAnimator: ValueAnimator? = null
    private val params: ChangeShortcutParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS,
        default = ChangeShortcutParams.New,
    )

    override fun initUi(): Unit = with(binding) {
        postponeEnterTransition()

        setPreview()

        setSharedTransitions(
            additionalCondition = { params is ChangeShortcutParams.Change },
            transitionName = (params as? ChangeShortcutParams.Change)?.transitionName.orEmpty(),
            sharedView = previewChangeShortcut,
        )

        setOnPreDrawListener {
            startPostponedEnterTransition()
        }

        rvChangeShortcutType.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = typesAdapter
        }

        rvChangeShortcutTags.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = tagsAdapter
        }

        commentsDelegate.initUi(rvChangeShortcutComments)

        rvChangeShortcutSettingAction.apply {
            layoutManager = LinearLayoutManagerWithExtraLayoutSpace(context)
            adapter = settingActionAdapter
        }
    }

    override fun initUx(): Unit = with(binding) {
        fieldChangeShortcutType.setOnClick(viewModel::onTypeChooserClick)
        fieldChangeShortcutTag.setOnClick(viewModel::onTagChooserClick)
        fieldChangeShortcutComment.setOnClick(viewModel::onCommentChooserClick)
        fieldChangeShortcutSettingAction.setOnClick(viewModel::onSettingActionChooserClick)
        buttonsChangeShortcutTarget.listener = viewModel::onButtonsRowClick
        btnChangeShortcutSave.setOnClick(throttle(viewModel::onSaveClick))
        btnChangeShortcutDelete.setOnClick(throttle(viewModel::onDeleteClick))
        addOnBackPressedListener(action = viewModel::onBack)
    }

    override fun initViewModel() = with(binding) {
        with(viewModel) {
            viewModel.extra = params
            deleteIconVisibility.observeOnce(btnChangeShortcutDelete::isVisible::set)
            saveButtonEnabled.observe(btnChangeShortcutSave::setEnabled)
            deleteButtonEnabled.observe(btnChangeShortcutDelete::setEnabled)
            viewData.observe(::setViewData)
            types.observe(typesAdapter::replace)
            tags.observe(tagsAdapter::replace)
            settingActions.observe(settingActionAdapter::replace)
            chooserState.observe(::setChooserState)
            keyboardVisibility.observe(::setKeyboardVisibility)
            commentsDelegate.initViewModel(this@ChangeShortcutFragment)
            viewModel.initialize()
        }
    }

    override fun onPositiveClick(tag: String?, data: Any?) {
        viewModel.onPositiveClick(tag)
    }

    override fun onTagValueSelected(
        params: RecordTagValueSelectionParams,
        data: Double,
    ) {
        viewModel.onTagValueSelected(params, data)
    }

    private fun setViewData(data: ChangeShortcutViewData) = with(binding) {
        updatePreview(data.shortcutPreview.data)

        buttonsChangeShortcutTarget.replace(data.targetModes)
        etChangeShortcutSettingAction.text = data.actionPreview

        layoutChangeShortcutTagsPreview.isVisible = data.recordTagsPreview.count > 0
        tvChangeShortcutTagPreview.text = data.recordTagsPreview.count.toString()

        groupChangeShortcutRecordTarget.isVisible = data.showRecordTarget
        groupChangeShortcutSettingTarget.isVisible = data.showSettingTarget
    }

    private fun updatePreview(data: CategoryViewData.Record) {
        with(binding.previewChangeShortcut) {
            itemName = data.name
            data.icon?.let { itemIcon = it }
            itemIconVisible = data.icon != null

            typeColorAnimator?.cancel()
            typeColorAnimator = animateColor(
                from = itemColor,
                to = data.color,
                doOnUpdate = { value ->
                    itemColor = value
                },
            )
        }
        with(binding) {
            layoutChangeShortcutTypePreview.setCardBackgroundColor(data.color)
            data.icon?.let { iconChangeShortcutTypePreview.itemIcon = it }
            layoutChangeShortcutTagsPreview.setCardBackgroundColor(data.color)
        }
    }

    private fun setPreview() = with(binding.previewChangeShortcut) {
        (params as? ChangeShortcutParams.Change)?.preview?.let { preview ->
            itemName = preview.name
            itemColor = preview.color
            itemIconColor = preview.iconColor
            itemIconAlpha = preview.iconAlpha
            itemIconVisible = preview.icon != null
            preview.icon?.toViewData()?.let { itemIcon = it }
        }
    }

    private fun setChooserState(
        state: ViewChooserStateDelegate.States,
    ) = with(binding) {
        ViewChooserStateDelegate.updateChooser<Activity>(
            state = state,
            chooserData = rvChangeShortcutType,
            chooserView = fieldChangeShortcutType,
            chooserArrow = arrowChangeShortcutType,
        )
        ViewChooserStateDelegate.updateChooser<Tag>(
            state = state,
            chooserData = rvChangeShortcutTags,
            chooserView = fieldChangeShortcutTag,
            chooserArrow = arrowChangeShortcutTag,
        )
        ViewChooserStateDelegate.updateChooser<Comment>(
            state = state,
            chooserData = rvChangeShortcutComments,
            chooserView = fieldChangeShortcutComment,
            chooserArrow = arrowChangeShortcutComment,
        )
        ViewChooserStateDelegate.updateChooser<SettingAction>(
            state = state,
            chooserData = rvChangeShortcutSettingAction,
            chooserView = fieldChangeShortcutSettingAction,
            chooserArrow = arrowChangeShortcutSettingAction,
        )

        val isClosed = state.current is Closed
        spaceChangeShortcutFieldsTop.isVisible = !isClosed
        buttonsChangeShortcutTarget.isVisible = isClosed
        btnChangeShortcutDelete.isVisible =
            viewModel.deleteIconVisibility.value.orFalse() && isClosed

        fieldChangeShortcutType.isVisible = isClosed || state.current is Activity
        fieldChangeShortcutTag.isVisible = isClosed || state.current is Tag
        fieldChangeShortcutComment.isVisible = isClosed || state.current is Comment
        fieldChangeShortcutSettingAction.isVisible = isClosed || state.current is SettingAction
        dividerChangeShortcutBottom.isInvisible = isClosed
    }

    private fun setKeyboardVisibility(visible: Boolean) {
        if (visible) {
            binding.rvChangeShortcutComments.postDelayed(500) {
                view?.findViewById<EditText>(R.id.etChangeRecordCommentField)
                    ?.let(::showKeyboard)
            }
        } else {
            hideKeyboard()
        }
    }

    companion object {
        fun createBundle(data: ChangeShortcutParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}
