package com.litecodez.tracksc

import androidx.compose.ui.graphics.Color
import com.litecodez.tracksc.objects.TCDataTypes
import kotlin.reflect.KProperty1

const val appName: String = "Tracks"
const val login: String = "login"
const val splash: String = "splash"
const val profile:String = "profile"
const val home:String = "home"
const val chatContainer:String = "chatContainer"


const val baseApi:String = "https://oshane-mckenzie.github.io/TracksRepo/app"
val lightPurple = Color(0xFFE0D1F8)
val lightPink = Color(0xFFFFF4F7)
val markdownContent = """  
## Overview
Welcome to **IdleOS**!
IdleOS is a simulated desktop environment built using Kotlin/WASM and Jetpack Compose.
For more information on Kotlin/WASM,
check out the [Kotlin/WASM](https://kotlinlang.org/docs/wasm-overview.html).
## Features

- **Panel**: A versatile panel to manage your tasks and applications.
- **Dock**: Quick access to your favorite and most-used applications.
- **Terminal**: A fully functional terminal for command-line operations.
- **File Manager**: Manage your files and directories with ease.
- **Settings App**: Customize and configure your desktop environment.
- **Control Center**: Access system controls and settings quickly.
- **Info Center**: Get detailed information about your system.
```
var name = "John"
```

"""

val reactionsList = listOf(
    TCDataTypes.Reactions.HAPPY,
    TCDataTypes.Reactions.HEART,
    TCDataTypes.Reactions.KISS,
    TCDataTypes.Reactions.TONGUE,
    TCDataTypes.Reactions.KOOL,
    TCDataTypes.Reactions.SAD,
    TCDataTypes.Reactions.ANGRY,
    TCDataTypes.Reactions.THUMBS_UP
)