package com.example.util.simpletimetracker.feature_dialogs.debugMenu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_dialogs.databinding.DebugMenuDialogFragmentBinding as Binding

@AndroidEntryPoint
class DebugMenuDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: DebugMenuViewModel by viewModels()

    override fun initDialog() {
        setSkipCollapsed()
        setFullScreen()
    }

    override fun initUx(): Unit = with(binding) {
        tvDebugMenuResetHideDefaultTypes.setOnClick(viewModel::onResetHideDefaultTypesClick)
    }
}