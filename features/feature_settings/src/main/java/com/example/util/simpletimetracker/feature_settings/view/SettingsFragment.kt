package com.example.util.simpletimetracker.feature_settings.view

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DataExportSettingsDialogListener
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.core.sharedViewModel.BackupViewModel
import com.example.util.simpletimetracker.core.sharedViewModel.MainTabsViewModel
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsBottomAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsCheckboxAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsCheckboxWithRangeAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsCollapseAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsHintAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsRangeAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsSelectorAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsSelectorWithButtonAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsSpinnerAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsSpinnerEvenAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsSpinnerNotCheckableAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsSpinnerWithButtonAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsTextAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsTextWithButtonAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsTopAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsTranslatorAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.SettingsViewModel
import com.example.util.simpletimetracker.feature_views.extension.rotateDown
import com.example.util.simpletimetracker.feature_views.extension.rotateUp
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingsResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_settings.databinding.SettingsFragmentBinding as Binding

@AndroidEntryPoint
class SettingsFragment :
    BaseFragment<Binding>(),
    StandardDialogListener,
    DurationDialogListener,
    DateTimeDialogListener,
    DataExportSettingsDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var backupViewModelFactory: BaseViewModelFactory<BackupViewModel>

    @Inject
    lateinit var mainTabsViewModelFactory: BaseViewModelFactory<MainTabsViewModel>

    private val viewModel: SettingsViewModel by viewModels()
    private val backupViewModel: BackupViewModel by activityViewModels { backupViewModelFactory }
    private val mainTabsViewModel: MainTabsViewModel by activityViewModels { mainTabsViewModelFactory }

    private val translatorsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createSettingsTranslatorAdapterDelegate(),
        )
    }
    private val contentAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createSettingsTopAdapterDelegate(),
            createSettingsBottomAdapterDelegate(),
            createSettingsTextAdapterDelegate(throttle(::onTextClicked)),
            createSettingsTextWithButtonAdapterDelegate(viewModel::onButtonClicked),
            createSettingsCheckboxAdapterDelegate(::onCheckboxClicked),
            createSettingsSpinnerAdapterDelegate(viewModel::onSpinnerPositionSelected),
            createSettingsSpinnerEvenAdapterDelegate(viewModel::onSpinnerPositionSelected),
            createSettingsSpinnerNotCheckableAdapterDelegate(viewModel::onSpinnerPositionSelected),
            createSettingsCollapseAdapterDelegate(viewModel::onCollapseClicked),
            createSettingsSelectorAdapterDelegate(viewModel::onSelectorClicked),
            createSettingsHintAdapterDelegate(),
            createSettingsRangeAdapterDelegate(
                onStartClick = viewModel::onRangeStartClicked,
                onEndClick = viewModel::onRangeEndClicked,
            ),
            createSettingsSpinnerWithButtonAdapterDelegate(
                viewModel::onSpinnerPositionSelected,
                viewModel::onButtonClicked,
            ),
            createSettingsSelectorWithButtonAdapterDelegate(
                viewModel::onSelectorClicked,
                viewModel::onButtonClicked,
            ),
            createSettingsCheckboxWithRangeAdapterDelegate(
                onClick = ::onCheckboxClicked,
                onStartClick = viewModel::onRangeStartClicked,
                onEndClick = viewModel::onRangeEndClicked,
            ),
        )
    }

    override fun initUi() = with(binding) {
        rvSettingsContent.adapter = contentAdapter
        rvSettingsContent.itemAnimator = null
        layoutSettingsTranslators.rvSettingsTranslators.adapter = translatorsAdapter
    }

    override fun initUx() = with(binding) {
        with(layoutSettingsExportImport) {
            layoutSettingsExportImportTitle.setOnClick(viewModel::onSettingsExportImportClick)
            layoutSettingsExportCsv.setOnClick(backupViewModel::onExportCsvClick)
            layoutSettingsImportCsv.setOnClick(backupViewModel::onImportCsvClick)
            btnSettingsImportCsvHelp.setOnClick(backupViewModel::onImportCsvHelpClick)
            layoutSettingsExportIcs.setOnClick(backupViewModel::onExportIcsClick)
            checkboxSettingsAutomaticExport.setOnClick(backupViewModel::onAutomaticExportClick)
        }
    }

    override fun initViewModel(): Unit = with(binding) {
        with(viewModel) {
            content.observe(contentAdapter::replaceAsNew)

            viewModel.settingsExportImportVisibility.observe { opened ->
                layoutSettingsExportImport.layoutSettingsExportImportContent.visible = opened
                layoutSettingsExportImport.arrowSettingsExportImport
                    .apply { if (opened) rotateDown() else rotateUp() }
            }
            resetScreen.observe {
                containerSettings.smoothScrollTo(0, 0)
                mainTabsViewModel.onHandled()
            }
        }

        with(viewModel.mainDelegate) {
            themeChanged.observe(::changeTheme)
        }

        with(viewModel.displayDelegate) {
            keepScreenOnCheckbox.observe(::setKeepScreenOn)
        }

        with(viewModel.translatorsDelegate) {
            translatorsViewData.observe(translatorsAdapter::replaceAsNew)
        }

        with(backupViewModel) {
            requestScreenUpdate.observe { viewModel.onRequestUpdate() }
            with(layoutSettingsExportImport) {
                automaticExportCheckbox.observe(checkboxSettingsAutomaticExport::setChecked)
                automaticExportLastSaveTime.observe {
                    tvSettingsAutomaticExportLastSaveTime.visible = it.isNotEmpty()
                    tvSettingsAutomaticExportLastSaveTime.text = it
                }
            }
        }

        with(mainTabsViewModel) {
            tabReselected.observe(viewModel::onTabReselected)
        }
    }

    override fun onResume() = with(binding) {
        super.onResume()
        with(layoutSettingsExportImport) {
            checkboxSettingsAutomaticExport.jumpDrawablesToCurrentState()
        }
        viewModel.onVisible()
    }

    override fun onPositiveClick(tag: String?, data: Any?) {
        backupViewModel.onPositiveDialogClick(tag)
    }

    override fun onDurationSet(duration: Long, tag: String?) {
        viewModel.onDurationSet(tag, duration)
    }

    override fun onDisable(tag: String?) {
        viewModel.onDurationDisabled(tag)
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    override fun onDataExportSettingsSelected(data: DataExportSettingsResult) {
        backupViewModel.onDataExportSettingsSelected(data)
    }

    private fun onTextClicked(block: SettingsBlock) {
        viewModel.onTextClicked(block)
        backupViewModel.onBlockClicked(block)
    }

    private fun onCheckboxClicked(block: SettingsBlock) {
        viewModel.onCheckboxClicked(block)
        backupViewModel.onBlockClicked(block)
    }

    private fun setKeepScreenOn(keepScreenOn: Boolean) {
        if (keepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun changeTheme(themeChanged: Boolean) {
        if (themeChanged) {
            activity?.recreate()
            // TODO fix fade and save scroll
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}
