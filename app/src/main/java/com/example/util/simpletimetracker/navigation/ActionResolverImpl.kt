package com.example.util.simpletimetracker.navigation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.example.util.simpletimetracker.core.provider.ApplicationDataProvider
import com.example.util.simpletimetracker.navigation.params.action.ActionParams
import com.example.util.simpletimetracker.navigation.params.action.CreateFileParams
import com.example.util.simpletimetracker.navigation.params.action.OpenFileParams
import com.example.util.simpletimetracker.navigation.params.action.OpenLinkParams
import com.example.util.simpletimetracker.navigation.params.action.OpenMarketParams
import com.example.util.simpletimetracker.navigation.params.action.OpenSystemSettings
import com.example.util.simpletimetracker.navigation.params.action.RequestPermissionParams
import com.example.util.simpletimetracker.navigation.params.action.SendEmailParams
import com.example.util.simpletimetracker.navigation.params.action.ShareImageParams
import javax.inject.Inject

class ActionResolverImpl @Inject constructor(
    private val resultContainer: ResultContainer,
    private val applicationDataProvider: ApplicationDataProvider,
) : ActionResolver {

    private var createFileResultLauncher: ActivityResultLauncher<Intent>? = null
    private var openFileResultLauncher: ActivityResultLauncher<Intent>? = null
    private var requestPermissionLauncher: ActivityResultLauncher<String>? = null

    override fun registerResultListeners(activity: ComponentActivity) {
        createFileResultLauncher = activity.registerForActivityResult(RequestCode.REQUEST_CODE_CREATE_FILE)
        openFileResultLauncher = activity.registerForActivityResult(RequestCode.REQUEST_CODE_OPEN_FILE)
        requestPermissionLauncher = activity.registerForRequestPermission(RequestCode.REQUEST_PERMISSION)
    }

    override fun execute(activity: Activity?, data: ActionParams) {
        when (data) {
            is OpenMarketParams -> openMarket(activity, data)
            is SendEmailParams -> sendEmail(activity, data)
            is CreateFileParams -> createFile(activity, data)
            is OpenFileParams -> openFile(activity, data)
            is OpenSystemSettings -> openSystemSettings(activity, data)
            is ShareImageParams -> shareImage(activity, data)
            is RequestPermissionParams -> requestPermission(data)
            is OpenLinkParams -> openLink(activity, data)
        }
    }

    private fun openMarket(activity: Activity?, params: OpenMarketParams) {
        val uri = Uri.parse(MARKET_INTENT + params.packageName)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage(MARKET_PACKAGE)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

        try {
            activity?.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(MARKET_LINK + params.packageName),
            ).apply {
                intent.setPackage(MARKET_PACKAGE)
            }.let {
                activity?.startActivity(it)
            }
        }
    }

    private fun openLink(activity: Activity?, params: OpenLinkParams) {
        activity?.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(params.link),
            ),
        )
    }

    private fun sendEmail(activity: Activity?, params: SendEmailParams) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse(EMAIL_URI)
            params.email?.let { putExtra(Intent.EXTRA_EMAIL, arrayOf(it)) }
            params.subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
            params.body?.let { putExtra(Intent.EXTRA_TEXT, it) }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            activity?.startActivity(Intent.createChooser(intent, params.chooserTitle))
        } catch (e: ActivityNotFoundException) {
            params.notHandledCallback?.invoke()
        }
    }

    private fun openFile(activity: Activity?, data: OpenFileParams) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType(data.type)

        if (activity.checkIfIntentResolves(intent)) {
            openFileResultLauncher?.launch(intent)
        } else {
            data.notHandledCallback()
        }
    }

    private fun createFile(activity: Activity?, data: CreateFileParams) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType(data.type)
            .putExtra(Intent.EXTRA_TITLE, data.fileName)

        if (activity.checkIfIntentResolves(intent)) {
            createFileResultLauncher?.launch(intent)
        } else {
            data.notHandledCallback()
        }
    }

    private fun openSystemSettings(activity: Activity?, data: OpenSystemSettings) {
        val packageName by lazy { applicationDataProvider.getPackageName() }

        // TODO refactor
        when (data) {
            is OpenSystemSettings.ExactAlarms -> runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.data = Uri.parse("package:$packageName")
                    activity?.startActivity(intent)
                }
            }

            is OpenSystemSettings.Notifications -> runCatching {
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .apply { putExtra(Settings.EXTRA_APP_PACKAGE, packageName) }
                } else {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .apply { this.data = Uri.parse("package:$packageName") }
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity?.startActivity(intent)
            }
        }
    }

    private fun shareImage(activity: Activity?, data: ShareImageParams) {
        try {
            val uri = Uri.parse(data.uriString)
            val intent = Intent(Intent.ACTION_SEND).apply {
                setDataAndType(uri, activity?.contentResolver?.getType(uri))
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            activity?.startActivity(Intent.createChooser(intent, null))
        } catch (e: ActivityNotFoundException) {
            data.notHandledCallback.invoke()
        }
    }

    private fun requestPermission(data: RequestPermissionParams) {
        requestPermissionLauncher?.launch(data.permissionId)
    }

    private fun ComponentActivity.registerForActivityResult(key: String): ActivityResultLauncher<Intent> {
        return registerForActivityResult(StartActivityForResult()) { result ->
            val intent = result.data
            val uri = intent?.data?.toString().takeIf { result.resultCode == Activity.RESULT_OK }

            resultContainer.sendResult(key, uri)
        }
    }

    private fun ComponentActivity.registerForRequestPermission(key: String): ActivityResultLauncher<String> {
        return registerForActivityResult(RequestPermission()) { result ->
            resultContainer.sendResult(key, result)
        }
    }

    private fun Activity?.checkIfIntentResolves(intent: Intent): Boolean {
        if (this == null) return false
        return packageManager?.let(intent::resolveActivity) != null
    }

    companion object {
        private const val MARKET_INTENT = "market://details?id="
        private const val MARKET_LINK = "http://play.google.com/store/apps/details?id="
        private const val MARKET_PACKAGE = "com.android.vending"
        private const val EMAIL_URI = "mailto:"
    }
}