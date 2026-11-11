package com.margycode.launcher.model

import android.content.Intent
import androidx.compose.ui.graphics.ImageBitmap

/** Один элемент приложения (ярлык на рабочем столе или в шторке). */
data class AppItem(
    val label: String,
    val packageName: String,
    val intent: Intent,
    val icon: ImageBitmap?,
    val isSystem: Boolean = false
)
