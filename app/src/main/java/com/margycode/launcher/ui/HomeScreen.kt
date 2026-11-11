package com.margycode.launcher.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.BitmapPainter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.margycode.launcher.model.AppItem
import com.margycode.launcher.ui.theme.AppTheme

/**
 * Главный экран лаунчера в стиле iOS:
 * - сетка приложений 4×6 (LazyVerticalGrid)
 * - squircle-иконки, белый текст с тенью
 * - док-бар со стеклянным фоном и 4 фиксированных приложения
 * - точки-индикатор страниц над док-баром
 * - свайп вверх открывает шторку приложений (AppDrawer)
 */
@Composable
fun HomeScreen(viewModel: MainViewModel = viewModel()) {
    val drawerOpen by remember { mutableStateOf(false) }
    val page by remember { mutableIntStateOf(0) }

    val desktopApps by viewModel.desktopApps.collectAsState()

    val perPage = 24 // 4 колонки × 6 строк
    val pageCount = (desktopApps.size / perPage).coerceAtLeast(1)

    val currentPageItems: List<AppItem> = if (desktopApps.isEmpty()) {
        emptyList()
    } else {
        val from = (page * perPage).coerceIn(0, desktopApps.size - 1)
        desktopApps.subList(from, (from + perPage).coerceAtMost(desktopApps.size))
    }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Свайп вверх по рабочему столу открывает шторку
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { _, amount ->
                        if (
                            !drawerOpen &&
                            amount.y < -60f &&
                            amount.y.absoluteValue > amount.x.absoluteValue
                        ) {
                            drawerOpen = true
                        }
                    }
                )
            }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                Spacer(Modifier.weight(0.72f))

                // Сетка приложений текущей страницы (стиль iOS, 4×6)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.weight(1f).padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(currentPageItems) { app ->
                        AppIcon(
                            app = app,
                            isDock = false,
                            onClick = {
                                context.startActivity(
                                    app.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        )
                    }
                }

                // Индикатор страниц (точки) над док-баром
                Row(
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pageCount) { i ->
                        PageDot(isSelected = i == page) { page = i }
                    }
                }

                // Док-бар: стеклянный фон, скругленные углы, 4 фиксированных приложения
                DockBar(
                    apps = desktopApps.take(4),
                    onAppClick = { app ->
                        context.startActivity(
                            app.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                )

                Spacer(Modifier.height(24.dp))
            }
        }

        // Шторка приложений выезжает снизу (стиль OxygenOS)
        AnimatedVisibility(
            visible = drawerOpen,
            enter = expandVertically(expandFrom = Alignment.Bottom),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            AppDrawer(
                isOpen = drawerOpen,
                onClose = { drawerOpen = false }
            )
        }
    }
}

/**
 * Один элемент приложения: иконка + подпись под ней.
 */
@Composable
fun AppIcon(app: AppItem, isDock: Boolean = false, onClick: () -> Unit) {
    val iconSize = if (isDock) 48.dp else 60.dp
    val cellSize = if (isDock) 56.dp else 84.dp
    val textSize = if (isDock) 11.sp else 13.sp

    Column(
        modifier = Modifier.size(cellSize).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Иконка со скругленными углами (squircle-стиль)
        Image(
            painter = if (app.icon != null) BitmapPainter(app.icon!!) else ColorPainter(Color.LightGray),
            contentDescription = null,
            modifier = Modifier
                .size(iconSize)
                .clip(SquircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
        )

        // Подпись белым цветом с тенью (двойной текст со смещением)
        TextWithShadow(text = app.label, fontSize = textSize, maxLines = 2)
    }
}

/**
 * Текст с тенью: чёрная копия со смещением + белый текст сверху.
 */
@Composable
fun TextWithShadow(text: String, fontSize: TextUnit = 14.sp, maxLines: Int = 1) {
    Column(Modifier.matchParentSize()) {
        Text(
            text = text,
            color = Color.Black,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            maxLines = maxLines,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.offset(x = 1.dp, y = 1.dp)
                .alpha(0.6f)
        )
        Text(
            text = text,
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            maxLines = maxLines,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/**
 * Точка индикатора страницы.
 */
@Composable
fun PageDot(isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) Color.White else Color.White

    Box(
        modifier = Modifier
            .size(if (isSelected) 22.dp else 9.dp, 9.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .alpha(if (isSelected) 1f else 0.4f)
            .clickable(onClick = onClick)
    )
}

/**
 * Док-бар снизу экрана: стеклянный (полупрозрачный) фон со скругленными углами.
 */
@Composable
fun DockBar(apps: List<AppItem>, onAppClick: (AppItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(32.dp))
            .alpha(0.65f)
            .border(1.dp, Color.White, RoundedCornerShape(32.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clipToBounds(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        apps.take(4).forEach { app ->
            AppIcon(app = app, isDock = true) {
                onAppClick(app)
            }
        }
        Spacer(Modifier.weight(1f))
    }
}

/**
 * Squircle-форма (скругленные углы в стиле iOS).
 */
class SquircleShape : androidx.compose.foundation.shape.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.layout.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.drawscope.Outline {
        val radius = min(size.width, size.height) * 0.30f
        return androidx.compose.ui.graphics.drawscope.Outline.RoundRect(
            rect = androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height),
            cornerRadius = radius
        )
    }
}
