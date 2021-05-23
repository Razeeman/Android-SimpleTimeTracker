package com.example.util.simpletimetracker.feature_widget.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.util.simpletimetracker.core.manager.ThemeManager
import com.example.util.simpletimetracker.core.recordTagSelection.RecordTagSelectionFragment
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.di.WidgetComponentProvider
import com.example.util.simpletimetracker.navigation.params.RecordTagSelectionParams
import javax.inject.Inject

class WidgetTagSelectionActivity : AppCompatActivity(),
    RecordTagSelectionFragment.OnTagSelectedListener {

    @Inject
    lateinit var themeManager: ThemeManager

    private val params: RecordTagSelectionParams by lazy {
        intent?.getParcelableExtra(ARGS_PARAMS) ?: RecordTagSelectionParams()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as WidgetComponentProvider).widgetComponent?.inject(this)
        themeManager.setTheme(this)
        setContentView(R.layout.widget_tag_selection_activity)

        supportFragmentManager.commit {
            replace(
                R.id.containerWidgetRecordTagSelection,
                RecordTagSelectionFragment.newInstance(params)
                    .apply { listener = this@WidgetTagSelectionActivity }
            )
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
