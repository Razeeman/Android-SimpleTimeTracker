package com.example.util.simpletimetracker.feature_settings.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.di.SettingsComponentProvider
import com.example.util.simpletimetracker.feature_settings.viewModel.SettingsViewModel
import com.example.util.simpletimetracker.navigation.RequestCode.REQUEST_CODE_CREATE_FILE
import com.example.util.simpletimetracker.navigation.RequestCode.REQUEST_CODE_OPEN_FILE
import kotlinx.android.synthetic.main.settings_fragment.*

class SettingsFragment : BaseFragment(),
    StandardDialogListener {

    override val layoutId: Int = R.layout.settings_fragment

    private val viewModel: SettingsViewModel by viewModels()

    override fun initDi() {
        val component = (activity?.application as SettingsComponentProvider)
            .settingsComponent

        component?.inject(viewModel)
    }

    override fun initUx() {
        tvSettingsSaveBackup.setOnClick(viewModel::onSaveClick)
        tvSettingsRestoreBackup.setOnClick(viewModel::onRestoreClick)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null && resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_CREATE_FILE -> {
                    data.data
                        ?.let(Uri::toString)
                        ?.let(viewModel::onSaveBackup)
                }
                REQUEST_CODE_OPEN_FILE -> {
                    data.data
                        ?.let(Uri::toString)
                        ?.let(viewModel::onRestoreBackup)
                }
            }
        }
    }

    override fun onPositiveClick(tag: String?) {
        viewModel.onPositiveDialogClick(tag)
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}
