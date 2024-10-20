package com.example.util.simpletimetracker.feature_widget.single

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.fragment.app.commit
import com.example.util.simpletimetracker.core.base.BaseActivity
import com.example.util.simpletimetracker.core.dialog.OnTagSelectedListener
import com.example.util.simpletimetracker.core.manager.ThemeManager
import com.example.util.simpletimetracker.core.provider.ContextProvider
import com.example.util.simpletimetracker.core.utils.activityArgumentDelegate
import com.example.util.simpletimetracker.core.utils.applyNavBarInsets
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.navigation.ScreenFactory
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_widget.databinding.WidgetTagSelectionActivityBinding as Binding

@AndroidEntryPoint
class WidgetSingleTagSelectionActivity :
    BaseActivity<Binding>(),
    OnTagSelectedListener {

    override val inflater: (LayoutInflater) -> Binding = Binding::inflate

    @Inject
    override lateinit var themeManager: ThemeManager

    @Inject
    override lateinit var contextProvider: ContextProvider

    @Inject
    lateinit var screenFactory: ScreenFactory

    private val params: RecordTagSelectionParams by activityArgumentDelegate(
        key = ARGS_PARAMS, default = RecordTagSelectionParams.Empty,
    )

    override fun initUi() {
        binding.root.applyNavBarInsets()
        screenFactory.getFragment(params)?.let {
            supportFragmentManager.commit {
                replace(R.id.containerWidgetRecordTagSelection, it)
            }
        }
    }

    override fun onTagSelected() {
        finish()
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun getStartIntent(
            context: Context,
            data: RecordTagSelectionParams,
        ): Intent {
            return Intent(context, WidgetSingleTagSelectionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(ARGS_PARAMS, data)
            }
        }
    }
}
