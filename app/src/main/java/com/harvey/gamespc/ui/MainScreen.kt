package com.harvey.gamespc.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.harvey.gamespc.R
import com.harvey.gamespc.SharedViewModel
import com.harvey.gamespc.ui.components.ShareAppDialog
import com.harvey.gamespc.ui.navigation.NavigationGraph

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(sharedViewModel: SharedViewModel) {
    val navController = rememberNavController()
    var showShareDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        // For frequent UI sounds
        SoundManager.loadSounds(context)

        // For one-shot startup sound
        val startupSoundPlayer = android.media.MediaPlayer.create(context, R.raw.app_launcher)
        startupSoundPlayer?.start()

        onDispose {
            SoundManager.release()
            startupSoundPlayer?.release()
        }
    }

    // Notification permission launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("Permission", "POST_NOTIFICATIONS permission granted")
        } else {
            Log.d("Permission", "POST_NOTIFICATIONS permission denied")
        }
    }

    LaunchedEffect(Unit) {
        // Request POST_NOTIFICATIONS permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Check if share dialog has been shown before
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val hasShownShareDialog = prefs.getBoolean("has_shown_share_dialog", false)
        if (!hasShownShareDialog) {
            showShareDialog = true
        }
    }

    val onItemClick: (itemId: String, categoryName: String) -> Unit = { itemId, categoryName ->
        navController.navigate("detail/$itemId/$categoryName")
    }

    Scaffold(
        topBar = {
            TopBar(
                navController = navController,
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchActiveChange = { isActive -> isSearchActive = isActive },
                onSearchQueryChange = { query ->
                    searchQuery = query
                    sharedViewModel.searchItems(query)
                }
            )
        },
        bottomBar = {
            com.harvey.gamespc.ads.BannerAdView()
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavigationGraph(
                navController = navController,
                sharedViewModel = sharedViewModel,
                searchQuery = searchQuery,
                onItemClick = onItemClick
            )

            if (showShareDialog) {
                ShareAppDialog(onDismiss = {
                    showShareDialog = false
                    context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit {
                        putBoolean(
                            "has_shown_share_dialog",
                            true
                        )
                    }
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavHostController,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchActiveChange: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val focusManager = LocalFocusManager.current

    val topBarItems = listOf(
        TopBarNavItem.Home,
        TopBarNavItem.Chat
    )

    Column {
        TopAppBar(
            title = {
                if (isSearchActive) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Buscar juegos o programas...",
                                        style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                } else {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1877F2),
                            fontSize = MaterialTheme.typography.titleLarge.fontSize
                        )
                    )
                }
            },
            actions = {
                if (isSearchActive) {
                    IconButton(onClick = {
                        onSearchActiveChange(false)
                        onSearchQueryChange("")
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close Search")
                    }
                } else {
                    val context = LocalContext.current
                    IconButton(onClick = { onSearchActiveChange(true) }) {
                        Icon(painter = painterResource(id = R.drawable.ic_buscar), contentDescription = "Search", tint = Color.Unspecified)
                    }
                    IconButton(onClick = {
                        val sendIntent: android.content.Intent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, "Descarga la app para disfrutar de todo el contenido: https://play.google.com/store/apps/details?id=" + context.packageName)
                            type = "text/plain"
                        }
                        val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }) {
                        Icon(painter = painterResource(id = R.drawable.ic_compartido), contentDescription = "Share App", tint = Color.Unspecified)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        )
        if (!isSearchActive) {
            TabRow(
                selectedTabIndex = topBarItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                topBarItems.forEach { item ->
                    Tab(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        },
                        icon = {
                            val tint = if (currentRoute == item.route) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            item.iconVector?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = item.route,
                                    tint = tint
                                )
                            }
                            item.iconDrawable?.let {
                                Icon(
                                    painter = painterResource(id = it),
                                    contentDescription = item.route,
                                    tint = Color.Unspecified
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

sealed class TopBarNavItem(
    val route: String,
    val iconVector: ImageVector? = null,
    @DrawableRes val iconDrawable: Int? = null
) {
    object Home : TopBarNavItem("home", iconDrawable = R.drawable.ic_mando)
    object Chat : TopBarNavItem("chat", iconDrawable = R.drawable.ic_chat)
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    // In a real scenario, you would create a mock or a fake of SharedViewModel
    // Since SharedViewModel requires Application, it's hard to instantiate here without a factory.
    // For the sake of this fix, we are removing the default 'viewModel()' call which causes the crash in Preview.
    // To make the preview work, you'd ideally pass a mocked version or refactor MainScreen to take state.
}