package com.margycode.launcher.ui

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableStateFlow
import androidx.lifecycle.asStateFlow
import androidx.lifecycle.combine
import androidx.lifecycle.stateIn
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.margycode.launcher.model.AppItem

/**
 * ViewModel лаунчера.
 * Загружает все установленные приложения через PackageManager,
 * делит на рабочий стол (избранное, первые 24 шт. под сетку 4x6)
 * и полный список для шторки приложений (App Drawer).
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val pm: PackageManager = application.packageManager

    /// Приложения для рабочего стола (избранное)
    private val _desktopApps = MutableStateFlow(emptyList<AppItem>())
    val desktopApps = _desktopApps.asStateFlow()

    /// Все приложения для шторки
    private val _drawerApps = MutableStateFlow(emptyList<AppItem>())
    val drawerApps = _drawerApps.asStateFlow()

    /// Текущий запрос поиска в шторке
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    /// Список шторки с учётом фильтра поиска
    val filteredApps = drawerApps.combine(query) { apps, text ->
        val q = text.trim().lowercase()
        if (q.isEmpty()) apps
        else apps.filter { it.label.lowercase().contains(q) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadApps()
    }

    fun setQuery(text: String) {
        _query.value = text
    }

    fun loadApps() {
        viewModelScope.launch {
            val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val apps = mutableListOf<AppItem>()

            for (info in installed) {
                if (!info.enabled) continue

                val label = info.loadLabel(pm)?.toString() ?: info.packageName

                val intent = pm.getLaunchIntentForPackage(info.packageName)
                    ?: Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        setPackage(info.packageName)
                    }

                // Берём только те приложения, которые реально можно запустить
                if (pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) == null) continue

                val icon = toBitmap(info.loadIcon(pm))
                apps.add(
                    AppItem(
                        label = label,
                        packageName = info.packageName,
                        intent = intent,
                        icon = icon
                    )
                )
            }

            apps.sortBy { it.label.lowercase() }

            _drawerApps.value = apps
            // Рабочий стол: первые 24 приложения под сетку 4x6 (одна страница iOS)
            _desktopApps.value = apps.take(24)
        }
    }

    /**
     * Переводит Drawable иконки в androidx.compose.ui.graphics.ImageBitmap.
     */
    private fun toBitmap(drawable: Drawable?): ImageBitmap? {
        drawable ?: return null

        val bitmap = if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            val w = drawable.intrinsicWidth.coerceAtLeast(1)
            val h = drawable.intrinsicHeight.coerceAtLeast(1)
            Bitmap.createBitmap(w, h, Config.ARGB_8888).also { bm ->
                Canvas(bm).apply {
                    drawable.setBounds(0, 0, w, h)
                    drawable.draw(this)
                }
            }
        }

        return bitmap.toImageBitmap()
    }
}
