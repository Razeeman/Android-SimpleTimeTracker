package com.example.util.simpletimetracker.core.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnPreDrawListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.viewbinding.ViewBinding
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.utils.applyNavBarInsets
import kotlinx.coroutines.Job

abstract class BaseFragment<T : ViewBinding> : Fragment(), Throttler {

    abstract val inflater: (LayoutInflater, ViewGroup?, Boolean) -> T
    abstract var insetConfiguration: InsetConfiguration
    override var throttleJob: Job? = null
    protected val binding: T get() = _binding!!
    private var _binding: T? = null
    private var preDrawListeners: MutableList<OnPreDrawListener> = mutableListOf()
    private var initialized: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Use previously saved view to avoid whole screen reinflating on back navigation,
        // because of android nav component using fragment.replace internally.
        // This also somehow fixes memory leaks occurring on navigation
        // from main to some edit screen and back.
        // If this ever changes - need to also fix these memory leaks.
        return _binding?.root?.also {
            initialized = true
        } ?: run {
            initialized = false
            _binding = inflater(inflater, container, false)
            binding.root
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (!initialized) {
            initInsets()
            initUi()
            initUx()
        }
        // Need to observe ViewModel again because LiveData.observe() uses fragment lifecycle,
        // meaning that the subscriptions are removed on view destroy event.
        initViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()
        preDrawListeners.forEach { view?.viewTreeObserver?.removeOnPreDrawListener(it) }
        preDrawListeners.clear()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        childFragmentManager.fragments.forEach {
            it.onActivityResult(requestCode, resultCode, data)
        }
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

    fun setOnPreDrawListener(block: () -> Unit) {
        val listener = object : OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                view?.viewTreeObserver?.removeOnPreDrawListener(this)
                block()
                return true
            }
        }
        preDrawListeners.add(listener)
        view?.viewTreeObserver?.addOnPreDrawListener(listener)
    }

    fun initInsets() {
        when (val config = insetConfiguration) {
            is InsetConfiguration.DoNotApply -> {
                // Do nothing
            }
            is InsetConfiguration.ApplyToView -> {
                config.view().applyNavBarInsets()
            }
        }
    }

    inline fun <T> LiveData<T>.observe(
        crossinline onChanged: (T) -> Unit,
    ) {
        observe(viewLifecycleOwner) { onChanged(it) }
    }
}