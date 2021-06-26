package com.example.util.simpletimetracker.navigation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.example.util.simpletimetracker.navigation.params.FileChooserParams
import com.example.util.simpletimetracker.navigation.params.OpenMarketParams
import com.example.util.simpletimetracker.navigation.params.SendEmailParams
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ActionResolverImpl @Inject constructor() : ActionResolver {

    override fun execute(activity: Activity?, action: Action, data: Any?) {
        when (action) {
            Action.OPEN_MARKET -> {
                openMarket(activity, data)
            }
            Action.SEND_EMAIL -> {
                sendEmail(activity, data)
            }
            Action.CREATE_FILE -> {
                val timeString = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(Date())
                val fileName = "stt_$timeString.backup"

                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "application/x-binary"
                intent.putExtra(Intent.EXTRA_TITLE, fileName)

                if (activity?.packageManager?.let(intent::resolveActivity) != null) {
                    activity.startActivityForResult(
                        intent,
                        RequestCode.REQUEST_CODE_CREATE_FILE
                    )
                } else {
                    (data as? FileChooserParams)?.notHandledCallback?.invoke()
                }
            }
            Action.OPEN_FILE -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "application/*"

                if (activity?.packageManager?.let(intent::resolveActivity) != null) {
                    activity.startActivityForResult(
                        intent,
                        RequestCode.REQUEST_CODE_OPEN_FILE
                    )
                } else {
                    (data as? FileChooserParams)?.notHandledCallback?.invoke()
                }
            }
            Action.CREATE_CSV_FILE -> {
                val timeString = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(Date())
                val fileName = "stt_records_$timeString.csv"

                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "text/csv"
                intent.putExtra(Intent.EXTRA_TITLE, fileName)

                if (activity?.packageManager?.let(intent::resolveActivity) != null) {
                    activity.startActivityForResult(
                        intent,
                        RequestCode.REQUEST_CODE_CREATE_CSV_FILE
                    )
                } else {
                    (data as? FileChooserParams)?.notHandledCallback?.invoke()
                }
            }
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

    companion object {
        private const val MARKET_INTENT = "market://details?id="
        private const val MARKET_LINK = "http://play.google.com/store/apps/details?id="
        private const val EMAIL_URI = "mailto:"
    }
}