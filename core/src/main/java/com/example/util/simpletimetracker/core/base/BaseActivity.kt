package com.example.util.simpletimetracker.core.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.viewbinding.ViewBinding
import com.example.util.simpletimetracker.core.extension.allowDiskWrite
import com.example.util.simpletimetracker.core.manager.ThemeManager
import com.example.util.simpletimetracker.core.provider.ContextProvider
import com.example.util.simpletimetracker.core.utils.applyStatusBarInsets

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {

    abstract val inflater: (LayoutInflater) -> T
    abstract val themeManager: ThemeManager
    abstract val contextProvider: ContextProvider
    protected val binding: T get() = _binding!!
    private var _binding: T? = null

    override fun attachBaseContext(newBase: Context?) {
        // Suppress strictMode for per app language prefs read for stored locale (see autoStoreLocales).
        // Only for api lower than 33.
        allowDiskWrite { super.attachBaseContext(newBase) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contextProvider.attach(this)
        themeManager.setTheme(this)
        _binding = inflater(layoutInflater)
        setContentView(binding.root)
        binding.root.applyStatusBarInsets()
        initUi()
        initUx()
        initViewModel()
    }

    open fun initUi() {
        // Override in subclasses
    }

    open fun initUx() {
        // Override in subclasses
    }

    open fun initViewModel() {
        // Override in subclasses
    }

    inline fun <T> LiveData<T>.observe(
        crossinline onChanged: (T) -> Unit,
    ) {
        observe(this@BaseActivity) { onChanged(it) }
    }
}