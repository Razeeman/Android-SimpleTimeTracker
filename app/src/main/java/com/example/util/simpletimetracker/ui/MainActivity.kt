package com.example.util.simpletimetracker.ui

import android.os.Bundle
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.core.base.BaseActivity
import com.example.util.simpletimetracker.core.manager.ThemeManager
import com.example.util.simpletimetracker.navigation.Router
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        themeManager.setTheme(this)
        setContentView(R.layout.main_activity)
        router.bind(this)
        router.onCreate(this)
    }

    override fun onResume() {
        super.onResume()
        router.bind(this)
    }
}
