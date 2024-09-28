package com.litecodez.tracksc.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import sh.calvin.autolinktext.AutoLinkText






@Composable
fun Hyperlink(text: String, modifier: Modifier = Modifier) {
    AutoLinkText(
        modifier = modifier,
        text = text,
        style = LocalTextStyle.current.copy(
            color = LocalContentColor.current,
            textAlign = TextAlign.Center
        ),
    )
}