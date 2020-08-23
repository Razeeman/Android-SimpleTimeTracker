package com.example.util.simpletimetracker.navigation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigator
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_change_record.view.ChangeRecordFragment
import com.example.util.simpletimetracker.feature_change_record_type.view.ChangeRecordTypeFragment
import com.example.util.simpletimetracker.feature_change_running_record.view.ChangeRunningRecordFragment
import com.example.util.simpletimetracker.feature_dialogs.dateTime.DateTimeDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.standard.StandardDialogFragment
import com.example.util.simpletimetracker.feature_statistics_detail.view.StatisticsDetailFragment
import com.example.util.simpletimetracker.navigation.RequestCode.REQUEST_CODE_CREATE_FILE
import com.example.util.simpletimetracker.navigation.RequestCode.REQUEST_CODE_OPEN_FILE
import com.example.util.simpletimetracker.navigation.model.SnackBarMessage
import com.example.util.simpletimetracker.navigation.params.FileChooserParams
import com.example.util.simpletimetracker.navigation.params.OpenMarketParams
import com.example.util.simpletimetracker.navigation.params.SendEmailParams
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RouterImpl @Inject constructor(
    private val resourceRepo: ResourceRepo
) : Router() {

    private var navController: NavController? = null
    private var activity: Activity? = null

    override fun bind(activity: Activity) {
        this.navController = activity.findNavController(R.id.container)
        this.activity = activity
    }

    override fun navigate(screen: Screen, data: Any?, sharedElements: Map<Any, String>?) {
        val navExtras = toNavExtras(sharedElements)

        when (screen) {
            Screen.CHANGE_RECORD_TYPE ->
                navController?.navigate(
                    R.id.action_mainFragment_to_changeRecordTypeFragment,
                    ChangeRecordTypeFragment.createBundle(data),
                    null,
                    navExtras
                )
            Screen.CHANGE_RECORD_RUNNING ->
                navController?.navigate(
                    R.id.action_mainFragment_to_changeRunningRecordFragment,
                    ChangeRunningRecordFragment.createBundle(data),
                    null,
                    navExtras
                )
            Screen.CHANGE_RECORD ->
                navController?.navigate(
                    R.id.action_mainFragment_to_changeRecordFragment,
                    ChangeRecordFragment.createBundle(data),
                    null,
                    navExtras
                )
            Screen.STATISTICS_DETAIL ->
                navController?.navigate(
                    R.id.action_mainFragment_to_statisticsDetailFragment,
                    StatisticsDetailFragment.createBundle(data),
                    null,
                    navExtras
                )
            Screen.STANDARD_DIALOG ->
                navController?.navigate(
                    R.id.standardDialogFragment,
                    StandardDialogFragment.createBundle(data),
                    null,
                    navExtras
                )
            Screen.DATE_TIME_DIALOG ->
                navController?.navigate(
                    R.id.dateTimeDialog,
                    DateTimeDialogFragment.createBundle(data),
                    null,
                    navExtras
                )
            Screen.CHART_FILTER_DIALOG ->
                navController?.navigate(
                    R.id.chartFilerDialogFragment,
                    null,
                    null,
                    navExtras
                )
            Screen.CARD_SIZE_DIALOG ->
                navController?.navigate(
                    R.id.cardSizeDialogFragment,
                    null,
                    null,
                    navExtras
                )
            // TODO move to Action
            Screen.CREATE_FILE -> {
                val timeString = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(Date())
                val fileName = "stt_$timeString.backup"

                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "application/x-binary"
                intent.putExtra(Intent.EXTRA_TITLE, fileName)

                if (activity?.packageManager?.let(intent::resolveActivity) != null) {
                    activity?.startActivityForResult(intent, REQUEST_CODE_CREATE_FILE)
                } else {
                    (data as? FileChooserParams)?.notHandledCallback?.invoke()
                }
            }
            Screen.OPEN_FILE -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "application/*"

                if (activity?.packageManager?.let(intent::resolveActivity) != null) {
                    activity?.startActivityForResult(intent, REQUEST_CODE_OPEN_FILE)
                } else {
                    (data as? FileChooserParams)?.notHandledCallback?.invoke()
                }
            }
        }
    }

    override fun execute(action: Action, data: Any?) {
        when (action) {
            Action.OPEN_MARKET -> {
                openMarket(data)
            }
            Action.SEND_EMAIL -> {
                sendEmail(data)
            }
        }
    }

    override fun back() {
        navController?.navigateUp()
    }

    override fun showSystemMessage(message: String) {
        Toast.makeText(activity?.applicationContext, message, Toast.LENGTH_LONG).show()
    }

    override fun showSnackBar(
        view: View,
        snackBarMessage: SnackBarMessage
    ) = with(snackBarMessage) {
        val snackBar = Snackbar.make(view, message, 5000)

        if (anchorToView) {
            snackBar.anchorView = view
        }

        snackBar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                if (event != DISMISS_EVENT_ACTION) {
                    dismissedListener?.invoke()
                }
            }
        })

        if (actionText.isNotEmpty()) {
            actionListener?.let {
                snackBar.setAction(actionText) { it() }
            }
        }

        snackBar.show()
    }

    private fun toNavExtras(sharedElements: Map<Any, String>?): Navigator.Extras {
        return FragmentNavigator.Extras
            .Builder()
            .apply {
                sharedElements?.forEach { (key, value) ->
                    (key as? View)?.let { view ->
                        addSharedElement(view, value)
                    }
                }
            }
            .build()
    }

    private fun openMarket(params: Any?) {
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

    private fun sendEmail(params: Any?) {
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
            resourceRepo.getString(R.string.message_app_not_found).let(::showSystemMessage)
        }
    }

    companion object {
        private const val MARKET_INTENT = "market://details?id="
        private const val MARKET_LINK = "http://play.google.com/store/apps/details?id="
        private const val EMAIL_URI = "mailto:"
    }
}