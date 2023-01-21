package com.example.util.simpletimetracker.ui

import android.os.Bundle
import androidx.activity.viewModels
import com.example.util.simpletimetracker.core.base.BaseActivity
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.extension.combineLiveData
import com.example.util.simpletimetracker.core.manager.ThemeManager
import com.example.util.simpletimetracker.core.sharedViewModel.BackupViewModel
import com.example.util.simpletimetracker.databinding.MainActivityBinding
import com.example.util.simpletimetracker.domain.extension.orFalse
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
    lateinit var backupViewModelFactory: BaseViewModelFactory<BackupViewModel>

    private val backupViewModel: BackupViewModel by viewModels(
        factoryProducer = { backupViewModelFactory }
    )
    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        router.bind(this)
        router.onCreate(this)
    }

    private fun initViewModel() {
        combineLiveData(
            backupViewModel.progressVisibility,
            backupViewModel.automaticBackupProgress,
            backupViewModel.automaticExportProgress,
        ).observe {
            binding.mainProgress.visible = it.first.orFalse() || it.second.orFalse() || it.third.orFalse()
            backupViewModel.onFileWork()
        }
    }
}
