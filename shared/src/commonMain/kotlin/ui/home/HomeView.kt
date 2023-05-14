package ui.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.*
import ui.MainActiveScreen
import ui.common.LocalWindowSize
import ui.common.WindowSize
import ui.home.HomePageActiveScreen.HOME
import ui.home.HomePageActiveScreen.SETTINGS
import ui.platform.cursorForHand
import ui.settings.SettingsState
import ui.settings.SettingsView
import kotlin.math.ceil

@Composable
fun HomePageView(state: HomePageState) {
    LaunchedEffect(state.settings.username) {
        state.connect()
        state.login()
    }

    val focusManager = LocalFocusManager.current
    Column(Modifier.pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
    ) {
        when (state.activeScreen) {
            HOME -> {
                HomeTopBar(state)
                Spacer(Modifier.size(5.dp))
                Divider()
                Spacer(Modifier.size(30.dp))
                MainPanel(state)
            }

            SETTINGS -> SettingsView(state.settings, onDismiss = { state.openHomeScreen() })
        }
    }

    DisposableEffect(Unit) {
        onDispose { state.activeScreen = HOME }
    }
}

@Composable
fun MainPanel(state: HomePageState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier.weight(0.3f),
        ) {
            JoinChannelPrompt(
                state,
                Modifier.widthIn(max = 400.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }
        Spacer(Modifier.weight(0.1f))
        Column(
            modifier = Modifier.weight(0.6f).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            AddFavoriteDialog(state)
            FavoriteChannels(state)
        }
    }
}

@Composable
fun FavoriteChannels(state: HomePageState) {
    val channels = state.settings.favoriteChannels

    val reorderState = rememberReorderableLazyGridState(dragCancelledAnimation = NoDragCancelledAnimation(),
        onMove = { from, to ->
            channels.value = channels.value.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        })

    val gridParams = rememberGridParams(channels.value.size)
    LazyVerticalGrid(
        modifier = Modifier
            .reorderable(reorderState)
            .widthIn(max = gridParams.gridMaxWidth),
        state = reorderState.gridState,
        columns = GridCells.Fixed(gridParams.cellsPerRow),
        contentPadding = PaddingValues(vertical = 30.dp)
    ) {
        items(
            items = channels.value,
            key = { it },
        ) { item ->
            FavoriteChannelItem(state, item, reorderState) { removedItem ->
                channels.value = channels.value.minus(removedItem)
            }
        }
    }
}

@Composable
private fun LazyGridItemScope.FavoriteChannelItem(
    state: HomePageState,
    item: String,
    reorderState: ReorderableLazyGridState,
    onChannelRemove: (item: String) -> Unit
) {
    ReorderableItem(reorderState, key = item) { isDragging ->
        val elevation = animateDpAsState(if (isDragging) 15.dp else 5.dp)
        var dropdownOptionsExpanded by remember { mutableStateOf(false) }
        Card(
            modifier = Modifier
                .padding(PaddingValues(5.dp))
                .widthIn(max = 300.dp)
                .cursorForHand(),
            shape = RoundedCornerShape(10.dp),
            elevation = elevation.value,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(60.dp)
                    .clickable {
                        state.settings.channel = item
                        state.settings.activeScreen = MainActiveScreen.CHANNEL
                    }
            ) {
                Icon(
                    Icons.Filled.Menu,
                    contentDescription = null,
                    modifier = Modifier.padding(5.dp, 0.dp, 5.dp, 0.dp).detectReorder(reorderState)
                )
                Text(item, modifier = Modifier.weight(1.0f).padding(15.dp))

                Box(
                    modifier = Modifier.fillMaxHeight()
                        .clickable { dropdownOptionsExpanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(5.dp, 0.dp, 0.dp, 0.dp)
                            .cursorForHand()
                    )
                    DropdownMenu(
                        expanded = dropdownOptionsExpanded,
                        onDismissRequest = { dropdownOptionsExpanded = false },
                    ) {
                        DropdownMenuItem(onClick = {
                            onChannelRemove(item)
                            dropdownOptionsExpanded = false
                        }) {
                            Text(text = "Remove")
                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun rememberGridParams(totalElements: Int): GridParams {
    val currentSize = LocalWindowSize.current
    return remember(totalElements, currentSize) {
        val cellsPerRow = when (currentSize) {
            WindowSize.COMPACT, WindowSize.MEDIUM -> cellsPerRow(2, totalElements)
            WindowSize.EXPANDED -> cellsPerRow(3, totalElements)
            WindowSize.FULL -> cellsPerRow(4, totalElements)
        }

        GridParams((cellsPerRow * 300).dp, cellsPerRow)
    }
}

private data class GridParams(
    val gridMaxWidth: Dp,
    val cellsPerRow: Int,
)

private fun cellsPerRow(maxRows: Int, totalElements: Int): Int {
    return when {
        totalElements == 0 -> maxRows
        totalElements <= maxRows -> totalElements
        maxRows * 2 > totalElements -> ceil(totalElements.toDouble() / 2).toInt()
        else -> maxRows
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun AddFavoriteDialog(state: HomePageState) {
    val coroutineScope = rememberCoroutineScope()
    var openDialog by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.padding(5.dp)
            .cursorForHand()
            .widthIn(max = 150.dp),
        shape = RoundedCornerShape(10.dp),
        elevation = 5.dp,
    ) {
        Column(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
                .clickable { openDialog = true }
                .onPreviewKeyEvent {
                    when (it.key) {
                        Key.Enter -> true
                        else -> false
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                Icons.Default.Add,
                contentDescription = null,
            )
            Text("Add Bookmark")
        }
    }

    if (openDialog) {
        AlertDialog(
            onDismissRequest = { openDialog = false },
            title = { Text("Add New Channel") },
            text = {
                OutlinedTextField(
                    value = text,
                    label = { Text(text = "Name") },
                    onValueChange = { text = it.trim() },
                    modifier = Modifier.onPreviewKeyEvent {
                        when {
                            it.key == Key.Enter && it.type == KeyEventType.KeyDown -> {
                                coroutineScope.launch {
                                    state.settings.addFavoriteChannel(text)
                                    state.settings.save()
                                    text = ""
                                    openDialog = false
                                }
                                true
                            }

                            else -> false
                        }
                    },
                )
            },
            confirmButton = {
                Button(
                    modifier = Modifier.cursorForHand(),
                    onClick = {
                        state.settings.addFavoriteChannel(text)
                        text = ""
                        openDialog = false
                    }) {
                    Text("Done")
                }
            },
            dismissButton = {
                Button(
                    modifier = Modifier.cursorForHand(),
                    onClick = {
                        text = ""
                        openDialog = false
                    }) {
                    Text("Cancel")
                }
            }
        )
    }

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun JoinChannelPrompt(state: HomePageState, modifier: Modifier) {
    val coroutineScope = rememberCoroutineScope()

    var text by remember { mutableStateOf("") }
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                label = { Text(text = "Join Channel") },
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .onPreviewKeyEvent {
                        when {
                            it.key == Key.Enter && it.type == KeyEventType.KeyDown -> {
                                coroutineScope.launch {
                                    state.settings.channel = text
                                    state.disconnect()
                                    state.settings.activeScreen = MainActiveScreen.CHANNEL
                                }

                                true
                            }

                            else -> false
                        }
                    },
            )
            Spacer(Modifier.size(10.dp))
        }
    }
}

@Composable
fun HomeTopBar(state: HomePageState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.clickable { state.openSettingsScreen() },
            shape = RoundedCornerShape(10.dp),
            elevation = 5.dp,
        ) {
            Text("Settings", modifier = Modifier.padding(10.dp))
        }

        Spacer(Modifier.weight(1.0f))
        CurrentUserButton(state)
    }
}

@Composable
fun CurrentUserButton(state: HomePageState) {
    val currentUser = state.settings.username ?: state.settings.connectionStatus.currentUser
    val currentUserText = currentUser?.let { "Logged in as: $it" }
        ?: "Not logged in"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(currentUserText)
        Spacer(Modifier.size(10.dp))
        if (currentUser != null) {
            Button(
                shape = RoundedCornerShape(10.dp),
                onClick = { state.settings.logout() }
            ) {
                Text("Logout")
            }
        } else {
            Button(
                shape = RoundedCornerShape(10.dp),
                onClick = {
                    state.settings.currentTab = SettingsState.CurrentTab.ACCOUNT
                    state.openSettingsScreen()
                }
            ) {
                Text("Login")
            }
        }
    }
}