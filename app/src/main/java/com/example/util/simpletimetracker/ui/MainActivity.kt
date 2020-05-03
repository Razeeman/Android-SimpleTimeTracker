package com.example.util.simpletimetracker.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.feature_main.MainFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }
}
