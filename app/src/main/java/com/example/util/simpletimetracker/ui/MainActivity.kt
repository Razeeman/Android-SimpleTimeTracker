package com.example.util.simpletimetracker.ui

import android.view.LayoutInflater
import androidx.activity.viewModels
import com.example.util.simpletimetracker.core.base.BaseActivity
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.manager.ThemeManager
import com.example.util.simpletimetracker.core.provider.ContextProvider
import com.example.util.simpletimetracker.core.sharedViewModel.BackupViewModel
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.Router
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.databinding.MainActivityBinding as Binding

@AndroidEntryPoint
class MainActivity : BaseActivity<Binding>() {

    override val inflater: (LayoutInflater) -> Binding = Binding::inflate

    @Inject
    override lateinit var themeManager: ThemeManager

    @Inject
    override lateinit var contextProvider: ContextProvider

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var backupViewModelFactory: BaseViewModelFactory<BackupViewModel>

    private val backupViewModel: BackupViewModel by viewModels(
        factoryProducer = { backupViewModelFactory },
    )

    override fun onResume() {
        super.onResume()
        router.bind(this)
        backupViewModel.onVisible()
    }

    override fun initUi() {
        router.bind(this)
        router.onCreate(this)
    }

    override fun initViewModel() {
        backupViewModel.progressVisibility.observe {
            binding.mainProgress.visible = it
            // TODO here to check that if automatic update finishes with error while app is opened.
            //  probably can be moved to VM because progress cen be shown for other reasons.
            backupViewModel.onFileWork()
        }
    }
}
