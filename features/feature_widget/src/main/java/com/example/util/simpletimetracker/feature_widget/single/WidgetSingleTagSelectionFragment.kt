package com.example.util.simpletimetracker.feature_widget.single

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.commit
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.feature_dialogs.api.OnTagSelectedListener
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.navigation.ScreenFactory
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_widget.databinding.WidgetSingleTagSelectionFragmentBinding as Binding

@AndroidEntryPoint
class WidgetSingleTagSelectionFragment :
    BaseFragment<Binding>(),
    OnTagSelectedListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.root }

    @Inject
    lateinit var screenFactory: ScreenFactory

    override fun initUi() {
        // TODO check insets on all widget activities.
        val params = activity?.intent?.extras
            ?.getParcelable<RecordTagSelectionParams>(ARGS_PARAMS)
            ?: return
        screenFactory.getFragment(params)?.let {
            childFragmentManager.commit {
                replace(R.id.containerWidgetRecordTagSelection, it)
            }
        }
    }

    override fun onTagSelected() {
        activity?.finish()
    }
}
