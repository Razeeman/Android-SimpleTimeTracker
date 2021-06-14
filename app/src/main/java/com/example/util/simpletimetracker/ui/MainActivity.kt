package com.example.util.simpletimetracker.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.manager.ThemeManager
import com.example.util.simpletimetracker.navigation.Router
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        themeManager.setTheme(this)
        setContentView(R.layout.main_activity)
        router.bind(this)
    }

    override fun onResume() {
        super.onResume()
        router.bind(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("onActivityResult $requestCode $resultCode ${data?.data}")
        super.onActivityResult(requestCode, resultCode, data)
        getAllFragments().forEach {
            it.onActivityResult(requestCode, resultCode, data)
        }
    }
}
