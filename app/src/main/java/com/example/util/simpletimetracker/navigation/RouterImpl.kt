package com.example.util.simpletimetracker.navigation

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigator
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.core.model.SnackBarMessage
import com.example.util.simpletimetracker.feature_change_record.view.ChangeRecordFragment
import com.example.util.simpletimetracker.feature_change_record_type.view.ChangeRecordTypeFragment
import com.example.util.simpletimetracker.feature_change_running_record.view.ChangeRunningRecordFragment
import com.example.util.simpletimetracker.feature_dialogs.dateTime.DateTimeDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.standard.StandardDialogFragment
import com.example.util.simpletimetracker.navigation.RequestCode.REQUEST_CODE_CREATE_FILE
import com.example.util.simpletimetracker.navigation.RequestCode.REQUEST_CODE_OPEN_FILE
import com.example.util.simpletimetracker.navigation.params.FileChooserParams
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouterImpl @Inject constructor() : Router() {

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
            Screen.CREATE_FILE -> {
                val timeString = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(Date())
                val fileName = "backup_$timeString.str"

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
}