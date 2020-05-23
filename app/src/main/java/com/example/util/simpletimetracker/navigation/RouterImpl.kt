package com.example.util.simpletimetracker.navigation

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.feature_change_record.view.ChangeRecordFragment
import com.example.util.simpletimetracker.feature_change_record_type.view.ChangeRecordTypeFragment
import com.example.util.simpletimetracker.feature_dialogs.dateTime.DateTimeDialogFragment
import com.example.util.simpletimetracker.feature_dialogs.standard.StandardDialogFragment
import com.example.util.simpletimetracker.navigation.RequestCode.REQUEST_CODE_CREATE_FILE
import com.example.util.simpletimetracker.navigation.RequestCode.REQUEST_CODE_OPEN_FILE
import com.example.util.simpletimetracker.navigation.params.FileChooserParams
import java.text.SimpleDateFormat
import java.util.*
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

    override fun navigate(screen: Screen, data: Any?) {
        when (screen) {
            Screen.CHANGE_RECORD_TYPE ->
                navController?.navigate(
                    R.id.changeRecordTypeFragment,
                    ChangeRecordTypeFragment.createBundle(data)
                )
            Screen.CHANGE_RECORD ->
                navController?.navigate(
                    R.id.changeRecordFragment,
                    ChangeRecordFragment.createBundle(data)
                )
            Screen.STANDARD_DIALOG ->
                navController?.navigate(
                    R.id.standardDialogFragment,
                    StandardDialogFragment.createBundle(data)
                )
            Screen.DATE_TIME_DIALOG ->
                navController?.navigate(
                    R.id.dateTimeDialog,
                    DateTimeDialogFragment.createBundle(data)
                )
            Screen.CHART_FILTER_DIALOG ->
                navController?.navigate(
                    R.id.chartFilerDialogFragment
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
        navController?.popBackStack()
    }

    override fun showSystemMessage(message: String) {
        Toast.makeText(activity?.applicationContext, message, Toast.LENGTH_LONG).show()
    }
}