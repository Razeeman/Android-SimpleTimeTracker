package com.example.util.simpletimetracker.feature_widget.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.commit
import com.example.util.simpletimetracker.core.base.BaseActivity
import com.example.util.simpletimetracker.core.dialog.OnTagSelectedListener
import com.example.util.simpletimetracker.core.manager.ThemeManager
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.ScreenFactory
import com.example.util.simpletimetracker.navigation.params.RecordTagSelectionParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WidgetTagSelectionActivity :
    BaseActivity(),
    OnTagSelectedListener {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var screenFactory: ScreenFactory

    private val params: RecordTagSelectionParams by lazy {
        intent?.getParcelableExtra(ARGS_PARAMS) ?: RecordTagSelectionParams()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        themeManager.setTheme(this)
        setContentView(R.layout.widget_tag_selection_activity)

        screenFactory.getFragment(
            screen = Screen.RECORD_TAG_SELECTION,
            data = params
        )?.let {
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
            data: RecordTagSelectionParams
        ): Intent {
            return Intent(context, WidgetTagSelectionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(ARGS_PARAMS, data)
            }
        }
    }
}
