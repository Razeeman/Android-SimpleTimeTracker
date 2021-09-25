package com.example.util.simpletimetracker.navigation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.example.util.simpletimetracker.navigation.params.FileChooserParams
import com.example.util.simpletimetracker.navigation.params.OpenMarketParams
import com.example.util.simpletimetracker.navigation.params.SendEmailParams
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ActionResolverImpl @Inject constructor(
    private val resultContainer: ResultContainer,
) : ActionResolver {

    private var createFileResultLauncher: ActivityResultLauncher<Intent>? = null
    private var openFileResultLauncher: ActivityResultLauncher<Intent>? = null
    private var createCsvFileResultLauncher: ActivityResultLauncher<Intent>? = null

    override fun registerResultListeners(activity: ComponentActivity) {
        createFileResultLauncher = activity.registerForActivityResult(RequestCode.REQUEST_CODE_CREATE_FILE)
        openFileResultLauncher = activity.registerForActivityResult(RequestCode.REQUEST_CODE_OPEN_FILE)
        createCsvFileResultLauncher = activity.registerForActivityResult(RequestCode.REQUEST_CODE_CREATE_CSV_FILE)
    }

    override fun execute(activity: Activity?, action: Action, data: Any?) {
        when (action) {
            Action.OPEN_MARKET -> openMarket(activity, data)
            Action.SEND_EMAIL -> sendEmail(activity, data)
            Action.CREATE_FILE -> createFile(activity, data)
            Action.OPEN_FILE -> openFile(activity, data)
            Action.CREATE_CSV_FILE -> createCsvFile(activity, data)
        }
    }

    private fun openMarket(activity: Activity?, params: Any?) {
        if (params !is OpenMarketParams) return

        val uri = Uri.parse(MARKET_INTENT + params.packageName)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

        try {
            activity?.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            activity?.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(MARKET_LINK + params.packageName)
                )
            )
        }
    }

    private fun sendEmail(activity: Activity?, params: Any?) {
        if (params !is SendEmailParams) return

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

    private fun createCsvFile(activity: Activity?, data: Any?) {
        val timeString = getFileNameTimeStamp()
        val fileName = "stt_records_$timeString.csv"

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("text/csv")
            .putExtra(Intent.EXTRA_TITLE, fileName)

        if (activity?.packageManager?.let(intent::resolveActivity) != null) {
            createCsvFileResultLauncher?.launch(intent)
        } else {
            (data as? FileChooserParams)?.notHandledCallback?.invoke()
        }
    }

    private fun openFile(activity: Activity?, data: Any?) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("application/*")

        if (activity.checkIfIntentResolves(intent)) {
            openFileResultLauncher?.launch(intent)
        } else {
            (data as? FileChooserParams)?.notHandledCallback?.invoke()
        }
    }

    private fun createFile(activity: Activity?, data: Any?) {
        val timeString = getFileNameTimeStamp()
        val fileName = "stt_$timeString.backup"

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("application/x-binary")
            .putExtra(Intent.EXTRA_TITLE, fileName)

        if (activity.checkIfIntentResolves(intent)) {
            createFileResultLauncher?.launch(intent)
        } else {
            (data as? FileChooserParams)?.notHandledCallback?.invoke()
        }
    }

    private fun ComponentActivity.registerForActivityResult(key: String): ActivityResultLauncher<Intent> {
        return registerForActivityResult(StartActivityForResult()) { result ->
            val intent = result.data
            val uri = intent?.data?.toString().takeIf { result.resultCode == Activity.RESULT_OK }

            resultContainer.sendResult(key, uri)
        }
    }

    private fun getFileNameTimeStamp(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    }

    private fun Activity?.checkIfIntentResolves(intent: Intent): Boolean {
        if (this == null) return false
        return packageManager?.let(intent::resolveActivity) != null
    }

    companion object {
        private const val MARKET_INTENT = "market://details?id="
        private const val MARKET_LINK = "http://play.google.com/store/apps/details?id="
        private const val EMAIL_URI = "mailto:"
    }
}