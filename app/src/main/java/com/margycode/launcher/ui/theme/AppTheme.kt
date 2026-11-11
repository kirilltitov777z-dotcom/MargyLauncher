package com.margycode.launcher.ui.theme

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Тема приложения.
 * Android 12+: dynamicColorScheme — цвета подстраиваются под обои (Material You).
 * Старые версии: тёмная кастомная палитра (фолбэк).
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme = remember(context) { dynamicColorScheme(context) }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(4.dp),
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(20.dp),
            extraLarge = RoundedCornerShape(28.dp)
        )
    ) {
        content()
    }
}

/**
 * Dynamic Color (Material You): берём средний пиксель обоев и строим палитру.
 */
private fun dynamicColorScheme(context: Context): ColorScheme {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        try {
            val drawable = WallpaperManager.getInstance(context).wallpaper
            val bitmap = (drawable as? BitmapDrawable)?.bitmap
                ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

            val pixel = bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
            val r = android.graphics.Color.red(pixel) / 255f
            val g = android.graphics.Color.green(pixel) / 255f
            val b = android.graphics.Color.blue(pixel) / 255f
            val luminance = 0.2126f * r + 0.7152f * g + 0.0722f * b
            val base = Color(red = r, green = g, blue = b, alpha = 1f)

            return if (luminance < 0.5f) {
                darkColorScheme(primary = base)
            } else {
                lightColorScheme(primary = base)
            }
        } catch (_: Exception) {
            return darkColorScheme()
        }
    } else {
        darkColorScheme()
    }
}
