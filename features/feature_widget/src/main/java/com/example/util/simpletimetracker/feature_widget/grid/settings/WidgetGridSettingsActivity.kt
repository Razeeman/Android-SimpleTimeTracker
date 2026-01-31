package com.example.util.simpletimetracker.feature_widget.grid.settings

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.commit
import com.example.util.simpletimetracker.core.base.BaseActivity
import com.example.util.simpletimetracker.core.manager.ThemeManager
import com.example.util.simpletimetracker.core.provider.ContextProvider
import com.example.util.simpletimetracker.feature_widget.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_widget.databinding.WidgetGridSettingsActivityBinding as Binding

@AndroidEntryPoint
class WidgetGridSettingsActivity : BaseActivity<Binding>() {

    override val inflater: (LayoutInflater) -> Binding = Binding::inflate

    @Inject
    override lateinit var themeManager: ThemeManager

    @Inject
    override lateinit var contextProvider: ContextProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED)

        if (savedInstanceState == null) {
            val fragment = WidgetGridSettingsFragment()
            supportFragmentManager.commit {
                replace(R.id.containerWidgetGridSettings, fragment)
            }
        }
    }

    fun exit(widgetId: Int) {
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}
