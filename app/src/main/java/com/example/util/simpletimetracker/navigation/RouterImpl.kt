package com.example.util.simpletimetracker.navigation

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.navigation.NavController
import com.example.util.simpletimetracker.R
import androidx.navigation.findNavController
import com.example.util.simpletimetracker.domain.di.AppContext
import com.example.util.simpletimetracker.feature_change_record.view.ChangeRecordFragment
import com.example.util.simpletimetracker.feature_change_record_type.view.ChangeRecordTypeFragment
import com.example.util.simpletimetracker.feature_dialogs.DateTimeDialogFragment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouterImpl @Inject constructor(
    @AppContext private val context: Context
): Router() {

    private var navController: NavController? = null

    override fun bind(activity: Activity) {
        navController = activity.findNavController(R.id.container)
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
            Screen.DATE_TIME_DIALOG ->
                navController?.navigate(
                    R.id.dateTimeDialog,
                    DateTimeDialogFragment.createBundle(data)
                )
        }
    }

    override fun back() {
        navController?.popBackStack()
    }

    override fun showSystemMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}