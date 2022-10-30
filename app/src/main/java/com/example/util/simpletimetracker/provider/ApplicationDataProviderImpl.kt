package com.example.util.simpletimetracker.provider

import com.example.util.simpletimetracker.BuildConfig
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.core.provider.ApplicationDataProvider
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import javax.inject.Inject

class ApplicationDataProviderImpl @Inject constructor(
    private val resourceRepo: ResourceRepo,
) : ApplicationDataProvider {

    override fun getPackageName(): String {
        return BuildConfig.APPLICATION_ID
    }

    override fun getAppName(): String {
        return resourceRepo.getString(R.string.app_name)
    }
}