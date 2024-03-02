package com.example.util.simpletimetracker.presentation.remember

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.util.simpletimetracker.presentation.data.ContextMessenger
import com.example.util.simpletimetracker.presentation.data.WearRPCClient

@Composable
fun rememberRPCClient(): WearRPCClient {
    val context = LocalContext.current
    return WearRPCClient(ContextMessenger(context))
}