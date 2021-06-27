package com.example.util.simpletimetracker.core.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

inline fun <reified T : ViewHolderType, B : ViewBinding> createRecyclerBindingAdapterDelegate(
    noinline inflater: (LayoutInflater, ViewGroup?, Boolean) -> B,
    noinline onBind: (B, ViewHolderType, List<Any>) -> Unit
) = object : RecyclerAdapterDelegate {

    override fun isForValidType(check: ViewHolderType): Boolean {
        return check is T
    }

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerBindingViewHolder<B> =
        BaseRecyclerBindingViewHolder(
            binding = inflater(LayoutInflater.from(parent.context), parent, false),
            onBind = onBind
        )

    override fun getViewHolderTypeName(): String = T::class.java.simpleName
}

class BaseRecyclerBindingViewHolder<B : ViewBinding>(
    private val binding: B,
    private val onBind: (B, ViewHolderType, List<Any>) -> Unit
) : BaseRecyclerViewHolder(binding.root) {

    override fun bind(item: ViewHolderType, payloads: List<Any>) =
        onBind(binding, item, payloads)
}