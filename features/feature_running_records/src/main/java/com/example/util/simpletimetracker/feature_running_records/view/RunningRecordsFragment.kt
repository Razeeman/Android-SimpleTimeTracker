package com.example.util.simpletimetracker.feature_running_records.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.OnTagSelectedListener
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.core.sharedViewModel.MainTabsViewModel
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.utils.doOnApplyWindowInsetsListener
import com.example.util.simpletimetracker.core.utils.getNavBarInsets
import com.example.util.simpletimetracker.core.utils.updateRunningRecordPreview
import com.example.util.simpletimetracker.core.viewData.RecordTypeSuggestionType
import com.example.util.simpletimetracker.domain.record.interactor.UpdateRunningRecordsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.createActivityFilterAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.createActivityFilterAddAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.commentField.createCommentFieldAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.createEmptySpaceAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.createHintBigAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordShortcut.createRecordShortcutAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSpecial.createRunningRecordTypeSpecialAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSuggestion.createRecordTypeSuggestionAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordWithHint.createRecordWithHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.createRunningRecordAdapterDelegate
import com.example.util.simpletimetracker.feature_running_records.viewModel.RunningRecordsViewModel
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.feature_views.extension.addOnScrollListenerAdapter
import com.example.util.simpletimetracker.feature_views.extension.pxToDp
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_running_records.databinding.RunningRecordsFragmentBinding as Binding

@AndroidEntryPoint
class RunningRecordsFragment :
    BaseFragment<Binding>(),
    OnTagSelectedListener,
    StandardDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.DoNotApply

    @Inject
    lateinit var mainTabsViewModelFactory: BaseViewModelFactory<MainTabsViewModel>

    private val viewModel: RunningRecordsViewModel by viewModels()
    private val mainTabsViewModel: MainTabsViewModel by activityViewModels(
        factoryProducer = { mainTabsViewModelFactory },
    )

    private val runningRecordsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createEmptySpaceAdapterDelegate(),
            createLoaderAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createHintAdapterDelegate(),
            createHintBigAdapterDelegate(),
            createDividerAdapterDelegate(),
            createRunningRecordAdapterDelegate(
                transitionNamePrefix = TransitionNames.RUNNING_RECORD_FROM_MAIN,
                onItemClick = viewModel::onRunningRecordClick,
                onItemLongClick = viewModel::onRunningRecordLongClick,
            ),
            createRecordTypeAdapterDelegate(
                onItemClickWithData = viewModel::onRecordTypeClick,
                onItemLongClick = viewModel::onRecordTypeLongClick,
                withTransition = true,
            ),
            createRecordTypeSuggestionAdapterDelegate(
                type = RecordTypeSuggestionType,
                onItemClickWithData = viewModel::onRecordTypeClick,
                onItemLongClick = viewModel::onRecordTypeLongClick,
            ),
            createRecordWithHintAdapterDelegate(
                onItemClick = throttle(viewModel::onRecordClick),
                onItemLongClick = throttle(viewModel::onRecordLongClick),
            ),
            createRecordShortcutAdapterDelegate(
                onItemClick = viewModel::onShortcutClick,
                onItemLongClick = viewModel::onShortcutLongClick,
            ),
            createRunningRecordTypeSpecialAdapterDelegate(
                onItemClick = throttle(viewModel::onSpecialRecordTypeClick),
            ),
            createActivityFilterAdapterDelegate(
                onClick = viewModel::onActivityFilterClick,
                onLongClick = viewModel::onActivityFilterLongClick,
            ),
            createActivityFilterAddAdapterDelegate(
                onItemClick = throttle(viewModel::onActivityFilterSpecialClick),
            ),
            createCommentFieldAdapterDelegate(
                afterTextChange = viewModel::onSearchTextChange,
            ),
        )
    }

    override fun initUi(): Unit = with(binding) {
        parentFragment?.postponeEnterTransition()

        rvRunningRecordsList.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = runningRecordsAdapter
            setHasFixedSize(false)
        }

        view?.doOnApplyWindowInsetsListener {
            viewModel.onChangeInsets(navBarHeight = it.getNavBarInsets().bottom.pxToDp())
        }

        setOnPreDrawListener {
            parentFragment?.startPostponedEnterTransition()
        }
    }

    override fun initUx() = with(binding) {
        // Problem. There are some problems with flexbox manager and long list of items,
        // one of which is changing (ex. timer on running record).
        // setHasFixedSize(false) causes items to lag on scroll.
        // setHasFixedSize(true) causes items disappear on scrolling to bottom,
        // opening another screen and returning back.
        // Solution. Set to true only on scroll, return back to false on scroll stop
        // and onPause (navigation).
        rvRunningRecordsList.addOnScrollListenerAdapter { _, newState ->
            rvRunningRecordsList.setHasFixedSize(newState != RecyclerView.SCROLL_STATE_IDLE)
        }
    }

    override fun initViewModel() {
        with(viewModel) {
            runningRecords.observe(runningRecordsAdapter::replace)
            resetScreen.observe { resetScreen() }
            previewUpdate.observe(::onPreviewUpdate)
        }
        with(mainTabsViewModel) {
            tabReselected.observe(viewModel::onTabReselected)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    override fun onPause() {
        super.onPause()
        binding.rvRunningRecordsList.setHasFixedSize(false)
        viewModel.onHidden()
    }

    override fun onTagSelected() {
        viewModel.onTagSelected()
    }

    override fun onPositiveClick(tag: String?, data: Any?) {
        viewModel.onPositiveClick(tag, data)
    }

    private fun resetScreen() = with(binding) {
        rvRunningRecordsList.smoothScrollToPosition(0)
        mainTabsViewModel.onHandled()
    }

    private fun onPreviewUpdate(update: UpdateRunningRecordsInteractor.Update) {
        updateRunningRecordPreview(
            currentList = runningRecordsAdapter.currentList,
            recyclerView = binding.rvRunningRecordsList,
            update = update,
        )
    }

    companion object {
        fun newInstance() = RunningRecordsFragment()
    }
}
