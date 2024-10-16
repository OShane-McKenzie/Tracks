package com.litecodez.tracksc.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

@Composable
fun BackgroundDetector(
    lifecycleOwner: LifecycleOwner,
    onForeground: () -> Unit = {},
    onBackground: () -> Unit = {}
) {

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> onBackground()
                Lifecycle.Event.ON_START -> onForeground()
                else -> {} // Do nothing for other events
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
