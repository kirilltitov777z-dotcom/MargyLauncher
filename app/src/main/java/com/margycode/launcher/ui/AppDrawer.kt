package com.margycode.launcher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusObserver
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.collectAsState
import com.margycode.launcher.model.AppItem

@Composable
fun AppDrawer(
    isOpen: Boolean,
    onClose: () -> Unit,
    viewModel: MainViewModel = viewModel(),
    onAppClick: (AppItem) -> Unit = {}
) {
    val apps by viewModel.filteredApps.collectAsState()
    val query by viewModel.query.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background, contentAlpha = 0.88f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, top = 16.dp, bottom = 92.dp)
            ) {
                val grouped = apps.groupBy {
                    it.label.firstOrDefault()?.uppercase() ?: "#"
                }
                val flat = grouped.entries.flatMap { (letter, list) ->
                    listOf(DrawerEntry.Header(letter)) + list.map { DrawerEntry.App(it) }
                }

                items(flat) { entry ->
                    when (entry) {
                        is DrawerEntry.Header -> {
                            Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                                Text(
                                    text = entry.letter,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .wrapContentWidth(Alignment.Start)
                                }
                            }
                        }
                        is DrawerEntry.App -> {
                            AppDrawerRow(app = entry.app, onAppClick = onAppClick)
                        }
                    }
                }
            }

            SearchBar(
                query = query,
                onQueryChange = { viewModel.setQuery(it) },
                keyboardController = keyboardController,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}

private sealed interface DrawerEntry {
    data class Header(val letter: String) : DrawerEntry
    data class App(val app: AppItem) : DrawerEntry
}

@Composable
private fun AppDrawerRow(app: AppItem, onAppClick: (AppItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onAppClick(app) }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = if (app.icon != null) BitmapPainter(app.icon!!) else ColorPainter(Color.Gray),
            contentDescription = null,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = app.label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.withAlpha(0.5f)
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    keyboardController: SoftwareKeyboardController?,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }

    LaunchedEffect(focused) {
        if (focused) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
            .focusRequester(focusRequester)
            .focusObserver { focused = it.isFocused },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
        },
        placeholder = { Text("Поиск приложений...") },
        keyboardOptions = KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        singleLine = true,
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = MaterialTheme.colorScheme.surface,
            focusedBackgroundColor = MaterialTheme.colorScheme.surface,
            unfocusedBackgroundColor = MaterialTheme.colorScheme.surface
        )
    )
}
