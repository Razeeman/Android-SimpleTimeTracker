package com.example.util.simpletimetracker.navigation

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.util.simpletimetracker.navigation.params.action.ActionParams
import com.example.util.simpletimetracker.navigation.params.notification.NotificationParams
import com.example.util.simpletimetracker.navigation.params.screen.ScreenParams
import com.example.util.simpletimetracker.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouterImpl @Inject constructor(
    private val screenResolver: ScreenResolver,
    private val actionResolver: ActionResolver,
    private val notificationResolver: NotificationResolver,
    private val resultContainer: ResultContainer,
    @ApplicationContext private val context: Context,
) : Router {

    private var navController: NavController? = null
    private var activity: Activity? = null
    private var dialog: Dialog? = null

    override fun onCreate(activity: ComponentActivity) {
        actionResolver.registerResultListeners(activity)
    }

    override fun bind(activity: Activity) {
        this.navController = activity.findNavController(R.id.container)
        this.activity = activity
    }

    override fun bindDialog(dialog: Dialog?) {
        this.dialog = dialog
    }

    override fun unbindDialog() {
        this.dialog = null
    }

    override fun navigate(data: ScreenParams, sharedElements: Map<Any, String>?) {
        screenResolver.navigate(navController, data, sharedElements)
    }

    override fun execute(data: ActionParams) {
        actionResolver.execute(activity, data)
    }

    override fun show(data: NotificationParams, anchor: Any?) {
        notificationResolver.show(activity, dialog, data, anchor)
    }

    override fun setResultListener(key: String, listener: ResultListener) {
        resultContainer.setResultListener(key, listener)
    }

    override fun sendResult(key: String, data: Any?) {
        resultContainer.sendResult(key, data)
    }

    override fun back() {
        navController?.navigateUp()
    }

    override fun restartApp() {
        activity?.finish()
        activity?.startActivity(getMainStartIntent())
    }

    override fun startApp() {
        val intent = getMainStartIntent().apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    override fun getMainStartIntent(): Intent {
        return Intent(context, MainActivity::class.java)
    }
}