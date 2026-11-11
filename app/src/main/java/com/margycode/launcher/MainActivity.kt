package com.margycode.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.margycode.launcher.ui.HomeScreen
import com.margycode.launcher.ui.theme.AppTheme

/**
 * Точка входа лаунчера.
 * Применяет AppTheme, внутри которой на Android 12+ используется dynamicColorScheme()
 * (цвета системы подстраиваются под обои), на старых версиях — тёмная кастомная палитра.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
    }
}
