package com.example.util.simpletimetracker.feature_dialogs.recordTagSelection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.descendants
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.feature_dialogs.api.OnTagSelectedListener
import com.example.util.simpletimetracker.core.extension.blockContentScrollByPosition
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.manager.KeyboardVisibilityManager
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.navigation.ScreenFactory
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_dialogs.databinding.RecordTagSelectionDialogFragmentBinding as Binding

@AndroidEntryPoint
class RecordTagSelectionDialogFragment :
    BaseBottomSheetFragment<Binding>(),
    OnTagSelectedListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var screenFactory: ScreenFactory

    private val params: RecordTagSelectionParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = RecordTagSelectionParams.Empty,
    )

    override fun onTagSelected() {
        dismiss()
    }

    override fun initDialog() {
        setSkipCollapsed()
        // Usual blockContentScroll doesn't work,
        // also need to block here, because long comment input wouldn't scroll otherwise
        // and would close the sheet instead.
        binding.containerRecordTagSelection.post {
            binding.root.descendants
                .filterIsInstance<RecyclerView>().firstOrNull()
                ?.let(::blockContentScrollByPosition)
        }
    }

    override fun initUi() {
        screenFactory.getFragment(params)?.let {
            childFragmentManager.commit {
                replace(R.id.containerRecordTagSelection, it)
            }
        }
    }

    override fun initUx() {
        KeyboardVisibilityManager.addObserver(this@RecordTagSelectionDialogFragment, ::onKeyboardVisible)
    }

    override fun onDestroy() {
        KeyboardVisibilityManager.removeObserver(this@RecordTagSelectionDialogFragment)
        super.onDestroy()
    }

    private fun onKeyboardVisible(isVisible: Boolean) {
        setFullScreen(isFullScreen = isVisible)
    }

    companion object {
        fun createBundle(data: RecordTagSelectionParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}