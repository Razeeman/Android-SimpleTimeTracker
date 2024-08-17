package com.example.util.simpletimetracker.ui

import android.os.Bundle
import androidx.activity.viewModels
import com.example.util.simpletimetracker.core.base.BaseActivity
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.manager.ThemeManager
import com.example.util.simpletimetracker.core.provider.ContextProvider
import com.example.util.simpletimetracker.core.sharedViewModel.BackupViewModel
import com.example.util.simpletimetracker.core.utils.applySystemBarInsets
import com.example.util.simpletimetracker.databinding.MainActivityBinding
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.Router
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var contextProvider: ContextProvider

    @Inject
    lateinit var backupViewModelFactory: BaseViewModelFactory<BackupViewModel>

    private val backupViewModel: BackupViewModel by viewModels(
        factoryProducer = { backupViewModelFactory },
    )
    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contextProvider.attach(this)
        initUi()
        initViewModel()
    }

    override fun onResume() {
        super.onResume()
        router.bind(this)
        backupViewModel.onVisible()
    }

    private fun initUi() {
        themeManager.setTheme(this)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()
        router.bind(this)
        router.onCreate(this)
    }

    private fun initViewModel() {
        backupViewModel.progressVisibility.observe {
            binding.mainProgress.visible = it
            // TODO here to check that if automatic update finishes with error while app is opened.
            //  probably can be moved to VM because progress cen be shown for other reasons.
            backupViewModel.onFileWork()
        }
    }
}
