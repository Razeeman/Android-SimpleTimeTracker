package com.example.util.simpletimetracker.navigation

import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import com.example.util.simpletimetracker.BuildConfig
import com.example.util.simpletimetracker.navigation.params.screen.ScreenParams
import javax.inject.Inject

class ScreenResolverImpl @Inject constructor(
    private val navigationDataMap: Map<Class<out ScreenParams>, NavigationData>,
) : ScreenResolver {

    override fun navigate(
        navController: NavController?,
        data: ScreenParams,
        sharedElements: Map<Any, String>?,
    ) {
        val navExtras = toNavExtras(sharedElements)

        navigationDataMap[data::class.java]
            ?.let {
                navController?.navigate(
                    it.navId,
                    it.bundleCreator.createBundle(data),
                    getBuilderWithAdditionalNavOptions(),
                    navExtras,
                )
            }
            ?: run {
                if (BuildConfig.DEBUG) error("Navigation error, unknown screen data: $data")
            }
    }

    private fun getBuilderWithAdditionalNavOptions(): NavOptions? {
        return if (ScreenResolver.disableAnimationsForTest) {
            NavOptions.Builder()
                .setEnterAnim(0)
                .setExitAnim(0)
                .setPopEnterAnim(0)
                .setPopExitAnim(0)
                .build()
        } else {
            null
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