package com.example.util.simpletimetracker.di

import android.app.Activity
import android.content.ContentResolver
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.test.platform.app.InstrumentationRegistry
import com.example.util.simpletimetracker.navigation.ActionResolver
import com.example.util.simpletimetracker.navigation.RequestCode
import com.example.util.simpletimetracker.navigation.ResultContainer
import com.example.util.simpletimetracker.navigation.params.action.ActionParams
import com.example.util.simpletimetracker.navigation.params.action.OpenFileParams
import javax.inject.Inject
import com.example.util.simpletimetracker.test.R as testR

class TestActionResolverImpl @Inject constructor(
    private val resultContainer: ResultContainer,
) : ActionResolver {

    override fun registerResultListeners(activity: ComponentActivity) {
        // Do nothing.
    }

    override fun execute(activity: Activity?, data: ActionParams) {
        when (data) {
            is OpenFileParams -> openFile()
        }
    }

    private fun openFile() {
        resultContainer.sendResult(
            RequestCode.REQUEST_CODE_OPEN_FILE,
            resourceToUri(testR.raw.db_version_23).toString(),
        )
    }

    @Suppress("SameParameterValue")
    private fun resourceToUri(resID: Int): Uri {
        val context = InstrumentationRegistry.getInstrumentation().context
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.resources.getResourcePackageName(resID) + '/' +
                context.resources.getResourceTypeName(resID) + '/' +
                context.resources.getResourceEntryName(resID),
        )
    }
}