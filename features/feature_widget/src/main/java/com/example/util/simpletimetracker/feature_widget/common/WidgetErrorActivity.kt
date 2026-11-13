package com.example.util.simpletimetracker.feature_widget.common

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import com.example.util.simpletimetracker.core.base.BaseActivity
import com.example.util.simpletimetracker.core.manager.ThemeManager
import com.example.util.simpletimetracker.core.provider.ContextProvider
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_widget.databinding.WidgetErrorActivityBinding as Binding

@AndroidEntryPoint
class WidgetErrorActivity : BaseActivity<Binding>() {

    override val inflater: (LayoutInflater) -> Binding = Binding::inflate

    @Inject
    override lateinit var themeManager: ThemeManager

    @Inject
    override lateinit var contextProvider: ContextProvider

    override fun initUi() {
        binding.tvErrorMessage.text = intent.getStringExtra(ARGS_PARAMS)
    }

    override fun initUx() {
        binding.btnErrorClose.setOnClick(::finish)
    }

    companion object {
        fun getStartIntent(
            context: Context,
            message: String,
        ): Intent {
            return Intent(context, WidgetErrorActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(ARGS_PARAMS, message)
            }
        }
    }
}
