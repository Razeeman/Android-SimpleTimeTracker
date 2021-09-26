package com.example.util.simpletimetracker.navigation

import android.view.View
import androidx.navigation.NavController
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import com.example.util.simpletimetracker.navigation.params.NavigationData
import javax.inject.Inject

class ScreenResolverImpl @Inject constructor(
    private val navigationDataMap: Map<Screen, NavigationData>,
) : ScreenResolver {

    override fun navigate(
        navController: NavController?,
        screen: Screen,
        data: Any?,
        sharedElements: Map<Any, String>?,
    ) {
        val navExtras = toNavExtras(sharedElements)

        navigationDataMap[screen]
            ?.let {
                navController?.navigate(
                    it.navId,
                    it.bundleProvider?.invoke(data),
                    null,
                    navExtras
                )
            }
            ?: run {
                if (BuildConfig.DEBUG) error("Navigation error, unknown screen: $screen")
            }
    }

    private fun toNavExtras(sharedElements: Map<Any, String>?): Navigator.Extras {
        return FragmentNavigator.Extras.Builder().apply {
            sharedElements?.forEach { (key, value) ->
                (key as? View)?.let { view ->
                    addSharedElement(view, value)
                }
            }
        }.build()
    }
}