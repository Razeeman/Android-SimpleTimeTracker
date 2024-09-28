package com.example.util.simpletimetracker.core.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.viewbinding.ViewBinding
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.utils.applyNavBarInsets
import com.example.util.simpletimetracker.core.utils.doOnApplyWindowInsetsListener
import com.example.util.simpletimetracker.core.utils.getStatusBarInsets
import com.example.util.simpletimetracker.navigation.Router
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Job
import javax.inject.Inject

abstract class BaseBottomSheetFragment<T : ViewBinding> : BottomSheetDialogFragment(), Throttler {

    @Inject
    lateinit var router: Router

    abstract val inflater: (LayoutInflater, ViewGroup?, Boolean) -> T
    override var throttleJob: Job? = null
    protected val binding: T get() = _binding!!
    private var _binding: T? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return getCustomDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = inflater(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initInsets()
        initDialog()
        initUi()
        initUx()
        initViewModel()
    }

    override fun onResume() {
        super.onResume()
        router.bindDialog(dialog)
    }

    override fun onPause() {
        router.unbindDialog()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    open fun initDialog() {
        // Override in subclasses
    }

    open fun initUi() {
        // Override in subclasses
    }

    open fun initUx() {
        // Override in subclasses
    }

    open fun initViewModel() {
        // Override in subclasses
    }

    inline fun <T> LiveData<T>.observe(
        crossinline onChanged: (T) -> Unit,
    ) {
        observe(viewLifecycleOwner) { onChanged(it) }
    }

    private fun initInsets() {
        _binding?.root?.applyNavBarInsets()
    }

    private fun getCustomDialog(): Dialog {
        return object : BottomSheetDialog(requireContext(), theme) {
            override fun onAttachedToWindow() {
                super.onAttachedToWindow()

                window?.let {
                    WindowCompat.setDecorFitsSystemWindows(it, false)
                }

                findViewById<View>(com.google.android.material.R.id.container)?.apply {
                    fitsSystemWindows = false
                    doOnApplyWindowInsetsListener {
                        updatePadding(
                            top = it.getStatusBarInsets().top,
                            bottom = 0,
                        )
                    }
                }

                findViewById<View>(com.google.android.material.R.id.coordinator)?.apply {
                    fitsSystemWindows = false
                }
            }
        }
    }
}