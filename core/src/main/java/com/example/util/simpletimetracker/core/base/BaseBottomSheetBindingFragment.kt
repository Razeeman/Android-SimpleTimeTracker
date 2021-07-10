package com.example.util.simpletimetracker.core.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

abstract class BaseBottomSheetBindingFragment<T : ViewBinding> : BaseBottomSheetDialogFragment() {

    abstract val inflater: (LayoutInflater, ViewGroup?, Boolean) -> T
    protected val binding: T get() = _binding!!
    private var _binding: T? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflater(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initDialog()
        initUi()
        initUx()
        initViewModel()
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}