package com.margycode.launcher.model

import android.content.Intent
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Один элемент приложения: ярлык для рабочего стола или шторки.
 */
data class AppItem(
    val label: String,                 // название, которое показываем под иконкой
    val packageName: String,           // имя пакета приложения
    val intent: Intent,                // намерение для запуска
    val icon: ImageBitmap?,            // bitmap иконки (или null, если не удалось загрузить)
    val isSystem: Boolean = false      // системное приложение или нет
)
