package com.example.util.simpletimetracker.feature_settings.customizeOptionsMenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_settings.views.getSettingsAdapterDelegates
import com.example.util.simpletimetracker.navigation.params.screen.CustomizeOptionsMenuDialogParams
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_settings.databinding.CustomizeOptionsMenuDialogFragmentBinding as Binding

@AndroidEntryPoint
class CustomizeOptionsMenuDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: CustomizeOptionsMenuViewModel by viewModels()

    private val contentAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            *getSettingsAdapterDelegates(
                onBlockClicked = viewModel::onBlockClicked,
                onSpinnerPositionSelected = { _, _ -> },
            ).toTypedArray(),
        )
    }
    private val extra: CustomizeOptionsMenuDialogParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = CustomizeOptionsMenuDialogParams.Empty,
    )

    override fun initDialog() {
        setSkipCollapsed()
    }

    override fun initUi() = with(binding) {
        rvCustomizeOptionsMenu.adapter = contentAdapter
        rvCustomizeOptionsMenu.itemAnimator = null
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = this@CustomizeOptionsMenuDialogFragment.extra
        viewModel.content.observe(contentAdapter::replaceAsNew)
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: CustomizeOptionsMenuDialogParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}